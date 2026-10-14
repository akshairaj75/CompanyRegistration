package com.backend.companyapp.dto.scraper;

import java.util.ArrayList;
import java.util.List;

public class ScrapeProductsResponseDto {
    private String websiteUrl;
    private String scannedUrl;
    private int totalDiscovered;
    private List<ScrapedProductDto> products = new ArrayList<>();
    private String strategyUsed;
    private String message;

    public ScrapeProductsResponseDto() {
    }

    public ScrapeProductsResponseDto(String websiteUrl, String scannedUrl, List<ScrapedProductDto> products, String strategyUsed, String message) {
        this.websiteUrl = websiteUrl;
        this.scannedUrl = scannedUrl;
        this.products = products != null ? products : new ArrayList<>();
        this.totalDiscovered = this.products.size();
        this.strategyUsed = strategyUsed;
        this.message = message;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getScannedUrl() {
        return scannedUrl;
    }

    public void setScannedUrl(String scannedUrl) {
        this.scannedUrl = scannedUrl;
    }

    public int getTotalDiscovered() {
        return totalDiscovered;
    }

    public void setTotalDiscovered(int totalDiscovered) {
        this.totalDiscovered = totalDiscovered;
    }

    public List<ScrapedProductDto> getProducts() {
        return products;
    }

    public void setProducts(List<ScrapedProductDto> products) {
        this.products = products;
        this.totalDiscovered = products != null ? products.size() : 0;
    }

    public String getStrategyUsed() {
        return strategyUsed;
    }

    public void setStrategyUsed(String strategyUsed) {
        this.strategyUsed = strategyUsed;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
