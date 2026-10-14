package com.backend.companyapp.service;

import com.backend.companyapp.dto.scraper.ScrapedProductDto;
import com.backend.companyapp.entity.Product;
import com.backend.companyapp.repository.ProductRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WebScraperServiceTest {

    @Mock
    private ProductRepository productRepository;

    private WebScraperService scraperService;

    @BeforeEach
    void setUp() {
        scraperService = new WebScraperService(productRepository);
        scraperService.setMaxPages(5);
        scraperService.setMaxDepth(2);
        scraperService.setTimeoutMs(3000);
        scraperService.setMaxProducts(20);
    }

    @Test
    void testExtractJsonLdProducts() {
        String html = """
                <html>
                <head>
                <script type="application/ld+json">
                {
                  "@context": "https://schema.org/",
                  "@type": "Product",
                  "name": "Acme Wireless Headphones",
                  "image": ["https://example.com/images/headphones.jpg"],
                  "description": "Premium noise cancelling headphones",
                  "offers": {
                    "@type": "Offer",
                    "priceCurrency": "USD",
                    "price": "199.99"
                  }
                }
                </script>
                </head>
                <body><h1>Welcome</h1></body>
                </html>
                """;

        Document doc = Jsoup.parse(html, "https://example.com");
        List<ScrapedProductDto> products = scraperService.extractJsonLdProducts(doc, "https://example.com");

        assertNotNull(products);
        assertEquals(1, products.size());
        ScrapedProductDto product = products.get(0);
        assertEquals("Acme Wireless Headphones", product.getName());
        assertEquals("https://example.com/images/headphones.jpg", product.getImageUrl());
        assertTrue(product.getPrice().contains("199.99"));
    }

    @Test
    void testExtractJsonLdGraphAndItemList() {
        String html = """
                <html>
                <head>
                <script type="application/ld+json">
                {
                  "@context": "https://schema.org",
                  "@graph": [
                    {
                      "@type": "Product",
                      "name": "Graph Product 1",
                      "offers": { "price": "29.99" }
                    },
                    {
                      "@type": "ItemList",
                      "itemListElement": [
                        {
                          "@type": "ListItem",
                          "item": {
                            "@type": "Product",
                            "name": "Nested List Product 2",
                            "offers": { "price": "49.00" }
                          }
                        }
                      ]
                    }
                  ]
                }
                </script>
                </head>
                <body></body>
                </html>
                """;

        Document doc = Jsoup.parse(html, "https://example.com");
        List<ScrapedProductDto> products = scraperService.extractJsonLdProducts(doc, "https://example.com");

        assertEquals(2, products.size());
        assertEquals("Graph Product 1", products.get(0).getName());
        assertEquals("Nested List Product 2", products.get(1).getName());
    }

    @Test
    void testExtractOpenGraphProduct() {
        String html = """
                <html>
                <head>
                  <meta property="og:type" content="product" />
                  <meta property="og:title" content="Organic Vitamin C Serum" />
                  <meta property="og:image" content="/images/serum.png" />
                  <meta property="product:price:amount" content="45.00" />
                  <meta property="product:price:currency" content="USD" />
                </head>
                <body></body>
                </html>
                """;

        Document doc = Jsoup.parse(html, "https://example.com/products/serum");
        ScrapedProductDto product = scraperService.extractOpenGraphProduct(doc, "https://example.com/products/serum");

        assertNotNull(product);
        assertEquals("Organic Vitamin C Serum", product.getName());
        assertEquals("https://example.com/images/serum.png", product.getImageUrl());
        assertTrue(product.getPrice().contains("45.00"));
    }

    @Test
    void testExtractDomProducts() {
        String html = """
                <html>
                <body>
                  <div class="product-grid">
                    <div class="product-card">
                      <h3 class="product-title">Mechanical Keyboard RGB</h3>
                      <img src="/media/keyboard.jpg" alt="" />
                      <span class="price">$120</span>
                      <a href="/products/mech-keyboard">View</a>
                    </div>
                    <div class="product-card">
                      <h3 class="product-title">Ergonomic Mouse</h3>
                      <img data-src="/media/mouse.jpg" alt="" />
                      <span class="price">$60</span>
                      <a href="/products/ergo-mouse">View</a>
                    </div>
                  </div>
                </body>
                </html>
                """;

        Document doc = Jsoup.parse(html, "https://shop.example.com");
        List<ScrapedProductDto> products = scraperService.extractDomProducts(doc, "https://shop.example.com");

        assertEquals(2, products.size());
        assertEquals("Mechanical Keyboard RGB", products.get(0).getName());
        assertEquals("https://shop.example.com/media/keyboard.jpg", products.get(0).getImageUrl());
        assertEquals("Ergonomic Mouse", products.get(1).getName());
        assertEquals("https://shop.example.com/media/mouse.jpg", products.get(1).getImageUrl());
    }

    @Test
    void testDeduplicateByName() {
        List<ScrapedProductDto> list = new ArrayList<>();
        list.add(new ScrapedProductDto("Product A", "desc", "img1", "10", "url1"));
        list.add(new ScrapedProductDto("product a ", "desc2", "img2", "10", "url2"));
        list.add(new ScrapedProductDto("Product B", "desc3", "img3", "20", "url3"));

        List<ScrapedProductDto> deduped = scraperService.deduplicateByName(list);
        assertEquals(2, deduped.size());
        assertEquals("Product A", deduped.get(0).getName());
        assertEquals("Product B", deduped.get(1).getName());
    }

    @Test
    void testEnrichWithCatalogMatches() {
        Product existingCatalogProduct = new Product();
        existingCatalogProduct.setId(99L);
        existingCatalogProduct.setName("Wireless Noise Cancelling Headphones");

        when(productRepository.findAll()).thenReturn(List.of(existingCatalogProduct));

        List<ScrapedProductDto> scraped = new ArrayList<>();
        scraped.add(new ScrapedProductDto("Sony Wireless Noise Cancelling Headphones Over-Ear", "desc", "img", "300", "url"));
        scraped.add(new ScrapedProductDto("Random Kitchen Blender", "desc", "img", "50", "url"));

        scraperService.enrichWithCatalogMatches(scraped);

        assertEquals(99L, scraped.get(0).getMatchedCatalogProductId());
        assertNull(scraped.get(1).getMatchedCatalogProductId());
    }

    @Test
    void testValidateUrlSafetyBlocksPrivateIPs() {
        assertThrows(SecurityException.class, () -> scraperService.validateUrlSafety("http://127.0.0.1:8080"));
        assertThrows(SecurityException.class, () -> scraperService.validateUrlSafety("http://localhost:3000"));
        assertThrows(SecurityException.class, () -> scraperService.validateUrlSafety("ftp://example.com"));
    }
}
