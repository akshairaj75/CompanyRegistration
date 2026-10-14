package com.backend.companyapp.dto.scraper;

public class ScrapedProductDto {
    private String name;
    private String description;
    private String imageUrl;
    private String price;
    private String productUrl;
    private Long matchedCatalogProductId; // Non-null if it matches an existing product in the DB

    public ScrapedProductDto() {
    }

    public ScrapedProductDto(String name, String description, String imageUrl, String price, String productUrl) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.productUrl = productUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public Long getMatchedCatalogProductId() {
        return matchedCatalogProductId;
    }

    public void setMatchedCatalogProductId(Long matchedCatalogProductId) {
        this.matchedCatalogProductId = matchedCatalogProductId;
    }
}
