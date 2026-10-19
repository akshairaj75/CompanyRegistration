package com.backend.companyapp.service;

import com.backend.companyapp.dto.scraper.ScrapedProductDto;
import com.backend.companyapp.dto.scraper.ScrapeProductsResponseDto;
import com.backend.companyapp.entity.Product;
import com.backend.companyapp.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URI;
import java.util.*;

@Service
public class WebScraperService {

    private static final Logger log = LoggerFactory.getLogger(WebScraperService.class);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";
    private static final int MAX_REDIRECTS = 5;

    @Value("${scraper.max-pages:30}")
    private int maxPages = 30;

    @Value("${scraper.max-depth:3}")
    private int maxDepth = 3;

    @Value("${scraper.timeout-ms:8000}")
    private int timeoutMs = 8000;

    @Value("${scraper.max-products:100}")
    private int maxProducts = 100;

    @Value("${scraper.crawl-delay-ms:100}")
    private int crawlDelayMs = 100;

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebScraperService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Setters for test mockability
    public void setMaxPages(int maxPages) {
        this.maxPages = maxPages;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public void setMaxProducts(int maxProducts) {
        this.maxProducts = maxProducts;
    }

    public ScrapeProductsResponseDto scrapeProducts(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return new ScrapeProductsResponseDto(rawUrl, rawUrl, Collections.emptyList(), "NONE",
                    "Website URL is required.");
        }

        String normalizedUrl = normalizeUrl(rawUrl.trim());

        // Validate URL & Guard against SSRF
        try {
            validateUrlSafety(normalizedUrl);
        } catch (Exception e) {
            log.warn("Blocked unsafe URL: {} - Reason: {}", normalizedUrl, e.getMessage());
            return new ScrapeProductsResponseDto(rawUrl, normalizedUrl, Collections.emptyList(), "BLOCKED",
                    "URL rejected: " + e.getMessage());
        }

        String initialHost = URI.create(normalizedUrl).getHost();
        Set<String> strategiesUsed = new LinkedHashSet<>();
        List<ScrapedProductDto> allProducts = new ArrayList<>();
        Set<String> visitedUrls = new HashSet<>();

        // Priority Queue for crawl tasks: Priority 1 (Product pages/feeds), Priority 2
        // (Category/Collection), Priority 3 (General/Pagination)
        PriorityQueue<CrawlTask> queue = new PriorityQueue<>(Comparator.comparingInt(CrawlTask::getPriority));

        // Step 1: Discover via Public Storefront Feeds (e.g. Shopify public
        // /products.json)
        tryShopifyProductsJson(normalizedUrl, allProducts, strategiesUsed);

        // Step 2: Discover via Sitemaps (/robots.txt and conventional /sitemap.xml
        // locations)
        if (allProducts.size() < maxProducts) {
            discoverFromSitemaps(normalizedUrl, initialHost, queue, strategiesUsed);
        }

        // Add root URL as ultra-priority crawl seed (-1) to guarantee it is visited
        // first
        queue.add(new CrawlTask(normalizedUrl, 0, -1));

        int pagesVisited = 0;

        while (!queue.isEmpty() && pagesVisited < maxPages && allProducts.size() < maxProducts) {
            CrawlTask currentTask = queue.poll();
            String currentUrl = currentTask.url;

            if (visitedUrls.contains(currentUrl)) {
                continue;
            }
            visitedUrls.add(currentUrl);
            pagesVisited++;

            try {
                if (crawlDelayMs > 0 && pagesVisited > 1) {
                    Thread.sleep(crawlDelayMs);
                }

                Document doc = fetchSafely(currentUrl, initialHost);
                if (doc == null) {
                    continue;
                }

                // Extraction A: JSON-LD Structured Data
                List<ScrapedProductDto> jsonLdProds = extractJsonLdProducts(doc, currentUrl);
                if (!jsonLdProds.isEmpty()) {
                    allProducts.addAll(jsonLdProds);
                    strategiesUsed.add("JSON-LD");
                }

                // Extraction B: OpenGraph Metadata
                ScrapedProductDto ogProd = extractOpenGraphProduct(doc, currentUrl);
                if (ogProd != null) {
                    allProducts.add(ogProd);
                    strategiesUsed.add("OpenGraph");
                }

                // Extraction C: DOM Product Cards
                List<ScrapedProductDto> domProds = extractDomProducts(doc, currentUrl);
                if (!domProds.isEmpty()) {
                    allProducts.addAll(domProds);
                    strategiesUsed.add("HTML DOM");
                }

                // Discovery: Extract navigation menu tabs with top priority (0) and relevant
                // internal links
                if (currentTask.depth < maxDepth && allProducts.size() < maxProducts) {
                    discoverNavigationMenuLinks(doc, currentUrl, initialHost, queue, visitedUrls);
                    discoverInternalLinks(doc, currentUrl, initialHost, currentTask.depth + 1, queue, visitedUrls);
                }

            } catch (Exception e) {
                log.debug("Error crawling page {}: {}", currentUrl, e.getMessage());
            }
        }

        // Deduplicate discovered products by title
        List<ScrapedProductDto> uniqueProducts = deduplicateByName(allProducts);

        // Cap to maxProducts limit
        if (uniqueProducts.size() > maxProducts) {
            uniqueProducts = new ArrayList<>(uniqueProducts.subList(0, maxProducts));
        }

        // Match with existing catalog products in database
        enrichWithCatalogMatches(uniqueProducts);

        String strategySummary = strategiesUsed.isEmpty() ? "None" : String.join(", ", strategiesUsed);
        String message = uniqueProducts.isEmpty()
                ? "Scanned " + pagesVisited + " page(s). No eCommerce products detected."
                : "Scanned " + pagesVisited + " page(s). Discovered " + uniqueProducts.size() + " product(s).";

        return new ScrapeProductsResponseDto(rawUrl, normalizedUrl, uniqueProducts, strategySummary, message);
    }

    /**
     * Safely executes an HTTP request, strictly validating any redirects against
     * SSRF.
     */
    public Document fetchSafely(String url, String allowedHost) {
        String currentUrl = url;
        int redirectCount = 0;

        while (redirectCount <= MAX_REDIRECTS) {
            try {
                validateUrlSafety(currentUrl);

                // Origin restriction check: must match domain or subdomain of allowedHost
                URI uri = URI.create(currentUrl);
                String host = uri.getHost();
                if (host == null || (!host.equalsIgnoreCase(allowedHost) && !host.endsWith("." + allowedHost))) {
                    log.debug("Skipping out-of-domain URL: {}", currentUrl);
                    return null;
                }

                Connection.Response response = Jsoup.connect(currentUrl)
                        .userAgent(USER_AGENT)
                        .timeout(timeoutMs)
                        .followRedirects(false)
                        .ignoreHttpErrors(true)
                        .maxBodySize(3 * 1024 * 1024) // 3MB limit
                        .execute();

                int status = response.statusCode();

                // Handle Redirects manually to prevent SSRF bypass
                if (status >= 300 && status < 400) {
                    String redirectLocation = response.header("Location");
                    if (redirectLocation == null || redirectLocation.trim().isEmpty()) {
                        return null;
                    }
                    currentUrl = resolveAbsoluteUrl(redirectLocation, currentUrl);
                    redirectCount++;
                    continue;
                }

                if (status >= 200 && status < 300) {
                    String contentType = response.contentType();
                    if (contentType != null && !contentType.contains("text/html")
                            && !contentType.contains("application/xhtml") && !contentType.contains("xml")) {
                        return null; // Don't parse PDFs, images, binaries
                    }
                    return response.parse();
                }

                return null;
            } catch (Exception e) {
                log.debug("Fetch failed for {}: {}", currentUrl, e.getMessage());
                return null;
            }
        }
        return null;
    }

    /**
     * Try extracting products from public storefront endpoints (e.g. Shopify
     * /products.json).
     */
    private void tryShopifyProductsJson(String baseUrl, List<ScrapedProductDto> products, Set<String> strategies) {
        try {
            URI uri = URI.create(baseUrl);
            String endpoint = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "")
                    + "/products.json?limit=50";
            validateUrlSafety(endpoint);

            Connection.Response res = Jsoup.connect(endpoint)
                    .userAgent(USER_AGENT)
                    .timeout(timeoutMs)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .execute();

            if (res.statusCode() == 200 && res.body().contains("\"products\"")) {
                JsonNode root = objectMapper.readTree(res.body());
                if (root.has("products") && root.get("products").isArray()) {
                    for (JsonNode item : root.get("products")) {
                        String title = item.path("title").asText(null);
                        if (title != null && !title.trim().isEmpty()) {
                            String handle = item.path("handle").asText("");
                            String productUrl = uri.getScheme() + "://" + uri.getHost() + "/products/" + handle;
                            String image = "";
                            if (item.has("images") && item.get("images").isArray() && item.get("images").size() > 0) {
                                image = item.get("images").get(0).path("src").asText("");
                            }
                            String price = "";
                            if (item.has("variants") && item.get("variants").isArray()
                                    && item.get("variants").size() > 0) {
                                price = item.get("variants").get(0).path("price").asText("");
                            }
                            String desc = item.path("body_html").asText("");
                            // Strip html tags from description
                            desc = Jsoup.parse(desc).text();

                            products.add(
                                    new ScrapedProductDto(cleanText(title), cleanText(desc), image, price, productUrl));
                        }
                    }
                    if (!products.isEmpty()) {
                        strategies.add("Storefront Feed (/products.json)");
                        log.info("Discovered {} products via Shopify /products.json feed", products.size());
                    }
                }
            }
        } catch (Exception e) {
            // Not a Shopify/products.json store, ignore
        }
    }

    /**
     * Discovers product and collection URLs from sitemaps via /robots.txt and
     * conventional locations.
     */
    private void discoverFromSitemaps(String baseUrl, String initialHost, PriorityQueue<CrawlTask> queue,
            Set<String> strategies) {
        List<String> sitemapUrls = new ArrayList<>();
        URI uri = URI.create(baseUrl);
        String rootOrigin = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "");

        // 1. Check robots.txt
        try {
            String robotsUrl = rootOrigin + "/robots.txt";
            validateUrlSafety(robotsUrl);
            Connection.Response resp = Jsoup.connect(robotsUrl)
                    .userAgent(USER_AGENT)
                    .timeout(timeoutMs)
                    .ignoreHttpErrors(true)
                    .execute();

            if (resp.statusCode() == 200) {
                String body = resp.body();
                for (String line : body.split("\\r?\\n")) {
                    if (line.trim().toLowerCase().startsWith("sitemap:")) {
                        String sUrl = line.substring(line.indexOf(':') + 1).trim();
                        if (!sUrl.isEmpty()) {
                            sitemapUrls.add(sUrl);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // 2. Add conventional sitemap candidates if robots.txt didn't provide any
        if (sitemapUrls.isEmpty()) {
            sitemapUrls.add(rootOrigin + "/sitemap.xml");
            sitemapUrls.add(rootOrigin + "/sitemap_products_1.xml");
            sitemapUrls.add(rootOrigin + "/product-sitemap.xml");
        }

        int sitemapsChecked = 0;
        for (String sitemapUrl : sitemapUrls) {
            if (sitemapsChecked >= 3)
                break;
            sitemapsChecked++;
            parseSitemap(sitemapUrl, initialHost, queue, strategies, 0);
        }
    }

    private void parseSitemap(String sitemapUrl, String initialHost, PriorityQueue<CrawlTask> queue,
            Set<String> strategies, int recurseDepth) {
        if (recurseDepth > 2)
            return; // Prevent deep sitemap recursion

        try {
            validateUrlSafety(sitemapUrl);
            Connection.Response res = Jsoup.connect(sitemapUrl)
                    .userAgent(USER_AGENT)
                    .timeout(timeoutMs)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .maxBodySize(4 * 1024 * 1024)
                    .execute();

            if (res.statusCode() != 200)
                return;

            Document xml = Jsoup.parse(res.body(), "", Parser.xmlParser());

            // Check if Sitemap Index
            Elements sitemaps = xml.select("sitemap > loc");
            if (!sitemaps.isEmpty()) {
                strategies.add("Sitemap Index");
                for (Element loc : sitemaps) {
                    String subSitemap = loc.text().trim();
                    // Prioritize product/item sitemaps
                    if (isProductUrl(subSitemap) || subSitemap.contains("product") || subSitemap.contains("shop")
                            || subSitemap.contains("catalog")) {
                        parseSitemap(subSitemap, initialHost, queue, strategies, recurseDepth + 1);
                    }
                }
                return;
            }

            // Regular Sitemap URL set
            Elements urls = xml.select("url > loc");
            int addedCount = 0;
            for (Element loc : urls) {
                String pageUrl = loc.text().trim();
                if (isProductUrl(pageUrl)) {
                    queue.add(new CrawlTask(pageUrl, 1, 2)); // Priority 2 (Sitemap Product page)
                    addedCount++;
                } else if (isCategoryUrl(pageUrl)) {
                    queue.add(new CrawlTask(pageUrl, 1, 3)); // Priority 3 (Sitemap Category/Collection page)
                    addedCount++;
                }
                if (addedCount >= 40)
                    break; // Bounded seed limit from sitemap
            }

            if (addedCount > 0) {
                strategies.add("XML Sitemap");
                log.info("Queued {} priority pages from sitemap: {}", addedCount, sitemapUrl);
            }

        } catch (Exception e) {
            log.debug("Failed parsing sitemap {}: {}", sitemapUrl, e.getMessage());
        }
    }

    /**
     * Prioritizes links in main navigation menus (<nav>, <header>, menu bars)
     * and links with anchor text matching 'Products', 'Catalogue', 'Services', etc.
     * Enqueues them as Priority 0 so the crawler visits these essential pages
     * FIRST.
     */
    public void discoverNavigationMenuLinks(Document doc, String currentUrl, String initialHost,
            PriorityQueue<CrawlTask> queue, Set<String> visitedUrls) {
        Elements menuContainers = doc.select(
                "nav, header, [role='navigation'], .navbar, .nav, .menu, #menu, .navigation, .site-header, #header");
        Elements menuLinks = !menuContainers.isEmpty() ? menuContainers.select("a[href]") : doc.select("a[href]");

        int added = 0;
        for (Element a : menuLinks) {
            if (added >= 40)
                break;

            String text = a.text().trim();
            String title = a.attr("title").trim();
            String href = a.attr("abs:href");

            if (href.isEmpty() || href.contains("#") || href.startsWith("javascript:") || href.startsWith("mailto:")
                    || href.startsWith("tel:")) {
                continue;
            }

            String cleanUrl = stripTrackingParams(href);
            if (visitedUrls.contains(cleanUrl)) {
                continue;
            }

            try {
                URI uri = URI.create(cleanUrl);
                String host = uri.getHost();
                if (host == null || (!host.equalsIgnoreCase(initialHost) && !host.endsWith("." + initialHost))) {
                    continue;
                }

                // Check if text or title matches prominent catalog / product menu labels
                if (isProductMenuText(text) || isProductMenuText(title) || isCategoryUrl(cleanUrl)) {
                    queue.add(new CrawlTask(cleanUrl, 1, 0)); // Priority 0 (HIGHEST PRIORITY - Forced Menu Tab!)
                    added++;
                }
            } catch (Exception ignored) {
            }
        }
    }

    public boolean isProductMenuText(String text) {
        if (text == null || text.trim().isEmpty())
            return false;
        String t = text.toLowerCase().trim();
        return t.equals("products") || t.equals("product") || t.equals("catalogue") || t.equals("catalog") ||
                t.equals("our products") || t.equals("all products") || t.equals("shop") || t.equals("store") ||
                t.equals("services") || t.equals("our services") || t.equals("solutions") || t.equals("our solutions")
                ||
                t.equals("equipment") || t.equals("portfolio") || t.equals("collection") || t.equals("collections") ||
                t.equals("categories") || t.equals("range") || t.equals("models") || t.equals("offerings") ||
                t.equals("hardware") || t.equals("items") || t.equals("parts") || t.equals("devices") ||
                t.startsWith("product") || t.endsWith("products") || t.startsWith("catalog")
                || t.startsWith("catalogue");
    }

    /**
     * Discovers relevant internal links (products, collections, pagination) on a
     * crawled page.
     */
    private void discoverInternalLinks(Document doc, String currentUrl, String initialHost, int nextDepth,
            PriorityQueue<CrawlTask> queue, Set<String> visitedUrls) {
        Elements links = doc.select("a[href]");
        int linksDiscovered = 0;

        for (Element a : links) {
            if (linksDiscovered >= 50)
                break;

            String absHref = a.attr("abs:href");
            if (absHref.isEmpty() || absHref.contains("#") || absHref.startsWith("javascript:")
                    || absHref.startsWith("mailto:") || absHref.startsWith("tel:")) {
                continue;
            }

            // Remove query params like tracking/session
            String cleanUrl = stripTrackingParams(absHref);

            if (visitedUrls.contains(cleanUrl)) {
                continue;
            }

            try {
                URI uri = URI.create(cleanUrl);
                String host = uri.getHost();
                if (host == null || (!host.equalsIgnoreCase(initialHost) && !host.endsWith("." + initialHost))) {
                    continue;
                }

                String aText = a.text().trim();
                boolean isMenuText = isProductMenuText(aText) || isProductMenuText(a.attr("title"));

                if (isMenuText) {
                    queue.add(new CrawlTask(cleanUrl, nextDepth, 0)); // Priority 0 (Forced Product Tab!)
                    linksDiscovered++;
                } else if (isProductUrl(cleanUrl)) {
                    queue.add(new CrawlTask(cleanUrl, nextDepth, 1));
                    linksDiscovered++;
                } else if (isCategoryUrl(cleanUrl)) {
                    queue.add(new CrawlTask(cleanUrl, nextDepth, 2));
                    linksDiscovered++;
                } else if (isPaginationUrl(cleanUrl, a.text())) {
                    queue.add(new CrawlTask(cleanUrl, nextDepth, 2));
                    linksDiscovered++;
                }
            } catch (Exception ignored) {
            }
        }
    }

    private boolean isProductUrl(String url) {
        String lower = url.toLowerCase();
        return lower.contains("/product/") || lower.contains("/products/") || lower.contains("/p/") ||
                lower.contains("/item/") || lower.contains("/items/") || lower.contains("/pd/") ||
                lower.contains("/goods/") || lower.contains("/model/") || lower.contains("/device/") ||
                lower.contains("/equipment/") || lower.contains("/detail/") || lower.contains("/details/") ||
                lower.contains("-p-") || lower.matches(".*/[a-z0-9-]+-p[0-9]+.*");
    }

    private boolean isCategoryUrl(String url) {
        String lower = url.toLowerCase();
        return lower.contains("/category/") || lower.contains("/categories/") || lower.contains("/c/") ||
                lower.contains("/collection/") || lower.contains("/collections/") ||
                lower.contains("/shop") || lower.contains("/catalog") || lower.contains("/catalogue") ||
                lower.contains("/store") || lower.contains("/products") || lower.contains("/our-products") ||
                lower.contains("/all-products") || lower.contains("/services") || lower.contains("/solutions") ||
                lower.contains("/equipment") || lower.contains("/portfolio") || lower.contains("/offerings") ||
                lower.contains("/range") || lower.contains("/models") || lower.contains("/devices") ||
                lower.contains("/hardware") || lower.contains("/brands");
    }

    private boolean isPaginationUrl(String url, String anchorText) {
        String lowerUrl = url.toLowerCase();
        String lowerText = anchorText.trim().toLowerCase();
        return lowerUrl.contains("page=") || lowerUrl.contains("p=") || lowerUrl.contains("/page/")
                || lowerText.equals("next") || lowerText.equals("»") || lowerText.equals(">");
    }

    private String stripTrackingParams(String url) {
        int qIdx = url.indexOf('?');
        if (qIdx == -1)
            return url;
        String base = url.substring(0, qIdx);
        String query = url.substring(qIdx + 1);

        String[] parts = query.split("&");
        List<String> retained = new ArrayList<>();
        for (String p : parts) {
            String lower = p.toLowerCase();
            if (!lower.startsWith("utm_") && !lower.startsWith("fbclid") && !lower.startsWith("gclid")
                    && !lower.startsWith("ref")) {
                retained.add(p);
            }
        }
        return retained.isEmpty() ? base : base + "?" + String.join("&", retained);
    }

    public String normalizeUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

    public void validateUrlSafety(String urlStr) throws Exception {
        URI uri = new URI(urlStr);
        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new SecurityException("Only HTTP and HTTPS protocols are permitted.");
        }

        String host = uri.getHost();
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid website domain.");
        }

        // Prevent SSRF: resolve host IP address
        InetAddress address = InetAddress.getByName(host);
        if (address.isLoopbackAddress() || address.isAnyLocalAddress() || address.isSiteLocalAddress()
                || address.isLinkLocalAddress()) {
            throw new SecurityException("Local/Private IP access is forbidden.");
        }

        // Check for cloud metadata service IPs (e.g. 169.254.169.254)
        String ipStr = address.getHostAddress();
        if (ipStr.startsWith("169.254.") || ipStr.startsWith("0.") || ipStr.equals("255.255.255.255")) {
            throw new SecurityException("Cloud metadata destination is forbidden.");
        }
    }

    /**
     * Extracts Schema.org Product data from <script type="application/ld+json">
     */
    public List<ScrapedProductDto> extractJsonLdProducts(Document doc, String baseUrl) {
        List<ScrapedProductDto> list = new ArrayList<>();
        Elements jsonLdScripts = doc.select("script[type=application/ld+json]");

        for (Element script : jsonLdScripts) {
            String jsonContent = script.data();
            if (jsonContent == null || jsonContent.trim().isEmpty())
                continue;

            try {
                JsonNode root = objectMapper.readTree(jsonContent);
                parseJsonLdNode(root, list, baseUrl);
            } catch (Exception ignored) {
            }
        }
        return list;
    }

    public void parseJsonLdNode(JsonNode node, List<ScrapedProductDto> list, String baseUrl) {
        if (node.isArray()) {
            for (JsonNode item : node) {
                parseJsonLdNode(item, list, baseUrl);
            }
            return;
        }

        if (!node.isObject())
            return;

        // Check @graph array
        if (node.has("@graph") && node.get("@graph").isArray()) {
            for (JsonNode graphItem : node.get("@graph")) {
                parseJsonLdNode(graphItem, list, baseUrl);
            }
        }

        // Check itemListElement
        if (node.has("itemListElement") && node.get("itemListElement").isArray()) {
            for (JsonNode elem : node.get("itemListElement")) {
                if (elem.has("item")) {
                    parseJsonLdNode(elem.get("item"), list, baseUrl);
                } else {
                    parseJsonLdNode(elem, list, baseUrl);
                }
            }
        }

        // Direct Product check
        String type = node.path("@type").asText("");
        if ("Product".equalsIgnoreCase(type) || "IndividualProduct".equalsIgnoreCase(type)) {
            String name = node.path("name").asText(null);
            if (name != null && !name.trim().isEmpty()) {
                String description = node.path("description").asText("");
                String image = "";
                if (node.has("image")) {
                    JsonNode imgNode = node.get("image");
                    if (imgNode.isTextual()) {
                        image = imgNode.asText();
                    } else if (imgNode.isArray() && imgNode.size() > 0) {
                        image = imgNode.get(0).asText();
                    } else if (imgNode.isObject() && imgNode.has("url")) {
                        image = imgNode.get("url").asText();
                    }
                }
                String price = "";
                if (node.has("offers")) {
                    JsonNode offers = node.get("offers");
                    if (offers.isArray() && offers.size() > 0) {
                        offers = offers.get(0);
                    }
                    if (offers.has("price")) {
                        String currency = offers.path("priceCurrency").asText("");
                        price = (currency.isEmpty() ? "" : currency + " ") + offers.get("price").asText();
                    }
                }

                String productUrl = node.path("url").asText("");
                list.add(new ScrapedProductDto(cleanText(name), cleanText(description),
                        resolveAbsoluteUrl(image, baseUrl), price, resolveAbsoluteUrl(productUrl, baseUrl)));
            }
        }
    }

    /**
     * Extracts OpenGraph product metadata if present.
     */
    public ScrapedProductDto extractOpenGraphProduct(Document doc, String baseUrl) {
        String ogType = getMetaTag(doc, "og:type");
        if (ogType != null && ogType.toLowerCase().contains("product")) {
            String title = getMetaTag(doc, "og:title");
            if (title == null || title.isEmpty()) {
                title = doc.title();
            }
            String desc = getMetaTag(doc, "og:description");
            String image = getMetaTag(doc, "og:image");
            String price = getMetaTag(doc, "product:price:amount");
            String currency = getMetaTag(doc, "product:price:currency");
            String formattedPrice = (price != null && !price.isEmpty())
                    ? ((currency != null ? currency + " " : "") + price)
                    : "";

            if (title != null && !title.trim().isEmpty()) {
                return new ScrapedProductDto(cleanText(title), cleanText(desc), resolveAbsoluteUrl(image, baseUrl),
                        formattedPrice, baseUrl);
            }
        }
        return null;
    }

    /**
     * Extracts products using typical eCommerce DOM selector patterns.
     */
    public List<ScrapedProductDto> extractDomProducts(Document doc, String baseUrl) {
        List<ScrapedProductDto> list = new ArrayList<>();

        String[] cardSelectors = {
                ".product-card", ".product-item", ".product-grid-item",
                "li.product", ".product", "[data-product-id]", "[data-product]",
                ".shop-item", ".catalog-item", ".card-product",
                ".grid-product", ".product-box", "article.product",
                "[data-sku]", ".product-listing-item", ".item-card",
                ".catalog-card", ".c-card", "[class*='productCard']",
                "[class*='product-card']", "[class*='ProductCard']",
                "[class*='product_item']", "[class*='product-item']",
                "[class*='product_card']", "[class*='product-grid'] > div",
                "[class*='products-grid'] > div", ".woocommerce ul.products li",
                ".grid > .group", "table.products-table tr"
        };

        Elements cards = new Elements();
        for (String selector : cardSelectors) {
            Elements found = doc.select(selector);
            if (found.size() >= 2) {
                cards = found;
                break;
            }
        }

        // Structural fallback: check for any parent container with 3+ child elements
        // where each child contains an image and a heading/link
        if (cards.isEmpty()) {
            Elements containers = doc.select(".grid, .row, ul, .catalog, .products, [class*='grid'], [class*='list']");
            for (Element container : containers) {
                Elements children = container.children();
                if (children.size() >= 3) {
                    int matchCount = 0;
                    for (Element child : children) {
                        if (child.selectFirst("img") != null && child.selectFirst("h2, h3, h4, h5, a[href]") != null) {
                            matchCount++;
                        }
                    }
                    if (matchCount >= 3) {
                        cards = children;
                        break;
                    }
                }
            }
        }

        for (Element card : cards) {
            if (list.size() >= 40)
                break;

            Element titleEl = card.selectFirst(
                    "h2, h3, h4, .product-title, .title, .woocommerce-loop-product__title, a[class*='title'], [class*='name']");
            String name = titleEl != null ? titleEl.text().trim() : "";
            if (name.isEmpty() || name.length() < 3)
                continue;

            Element imgEl = card.selectFirst("img");
            String imgUrl = "";
            if (imgEl != null) {
                imgUrl = imgEl.hasAttr("data-src") ? imgEl.attr("data-src")
                        : (imgEl.hasAttr("srcset") ? imgEl.attr("srcset").split(" ")[0] : imgEl.attr("src"));
            }

            Element priceEl = card.selectFirst(".price, .product-price, .amount, span[class*='price']");
            String price = priceEl != null ? priceEl.text().trim() : "";

            Element linkEl = card.selectFirst("a[href]");
            String productUrl = linkEl != null ? linkEl.attr("href") : "";

            list.add(new ScrapedProductDto(cleanText(name), "", resolveAbsoluteUrl(imgUrl, baseUrl), price,
                    resolveAbsoluteUrl(productUrl, baseUrl)));
        }

        return list;
    }

    /**
     * Efficiently matches scraped products with existing catalog products in batch.
     */
    public void enrichWithCatalogMatches(List<ScrapedProductDto> products) {
        if (products.isEmpty())
            return;

        // Fetch all catalog products once to avoid N+1 queries during a scan
        List<Product> catalog = productRepository.findAll();
        if (catalog.isEmpty())
            return;

        Map<String, Long> exactCatalogMap = new HashMap<>();
        for (Product p : catalog) {
            if (p.getName() != null) {
                exactCatalogMap.put(p.getName().trim().toLowerCase(), p.getId());
            }
        }

        for (ScrapedProductDto p : products) {
            if (p.getName() == null || p.getName().trim().isEmpty())
                continue;
            String lowerName = p.getName().trim().toLowerCase();

            // 1. Exact Name Match
            if (exactCatalogMap.containsKey(lowerName)) {
                p.setMatchedCatalogProductId(exactCatalogMap.get(lowerName));
            } else {
                // 2. High-Confidence Substring Match (e.g. Catalog "iPhone 15", Scraped "Apple
                // iPhone 15 128GB")
                for (Product cp : catalog) {
                    String cName = cp.getName().trim().toLowerCase();
                    if (lowerName.contains(cName) && cName.length() >= 5) {
                        p.setMatchedCatalogProductId(cp.getId());
                        break;
                    }
                }
            }
        }
    }

    public List<ScrapedProductDto> deduplicateByName(List<ScrapedProductDto> list) {
        Map<String, ScrapedProductDto> map = new LinkedHashMap<>();
        for (ScrapedProductDto p : list) {
            if (p.getName() != null && !p.getName().trim().isEmpty()) {
                String key = p.getName().trim().toLowerCase();
                if (!map.containsKey(key)) {
                    map.put(key, p);
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    private String getMetaTag(Document doc, String property) {
        Element el = doc.selectFirst("meta[property=" + property + "], meta[name=" + property + "]");
        return el != null ? el.attr("content") : null;
    }

    private String resolveAbsoluteUrl(String url, String baseUrl) {
        if (url == null || url.trim().isEmpty())
            return "";
        url = url.trim();
        if (url.startsWith("//")) {
            return "https:" + url;
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        try {
            return URI.create(baseUrl).resolve(url).toString();
        } catch (Exception e) {
            return url;
        }
    }

    private String cleanText(String text) {
        if (text == null)
            return "";
        return text.replaceAll("\\s+", " ").trim();
    }

    /**
     * Crawl task with URL, depth, and priority level.
     */
    private static class CrawlTask {
        final String url;
        final int depth;
        final int priority;

        CrawlTask(String url, int depth, int priority) {
            this.url = url;
            this.depth = depth;
            this.priority = priority;
        }

        int getPriority() {
            return priority;
        }
    }
}
