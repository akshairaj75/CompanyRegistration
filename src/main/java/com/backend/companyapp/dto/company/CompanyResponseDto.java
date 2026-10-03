package com.backend.companyapp.dto.company;

import com.backend.companyapp.dto.brand.BrandResponseDto;
import com.backend.companyapp.dto.product.ProductResponseDto;
import com.backend.companyapp.entity.Brand;
import com.backend.companyapp.entity.Company;
import com.backend.companyapp.entity.Product;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CompanyResponseDto {

    private Long id;
    private String companyName;
    private String email;
    private String landline;
    private String address;
    private String city;
    private String country;
    private String website;
    private String description;
    private String status;
    private String businessCard;
    private String contactName;
    private String contactDesignation;
    private String contactEmail;
    private String contactMobileNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Long> brandIds = new ArrayList<>();
    private List<BrandResponseDto> brands = new ArrayList<>();
    private List<Long> productIds = new ArrayList<>();
    private List<ProductResponseDto> products = new ArrayList<>();
    private List<String> businessCards = new ArrayList<>();

    // Getters and Setters
    public List<Long> getBrandIds() {
        return brandIds;
    }

    public void setBrandIds(List<Long> brandIds) {
        this.brandIds = brandIds;
    }

    public List<BrandResponseDto> getBrands() {
        return brands;
    }

    public void setBrands(List<BrandResponseDto> brands) {
        this.brands = brands;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }

    public List<String> getBusinessCards() {
        return businessCards;
    }

    public void setBusinessCards(List<String> businessCards) {
        this.businessCards = businessCards != null ? businessCards : new ArrayList<>();
    }

    public List<ProductResponseDto> getProducts() {
        return products;
    }

    public void setProducts(List<ProductResponseDto> products) {
        this.products = products;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLandline() {
        return landline;
    }

    public void setLandline(String landline) {
        this.landline = landline;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBusinessCard() {
        return businessCard;
    }

    public void setBusinessCard(String businessCard) {
        this.businessCard = businessCard;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactDesignation() {
        return contactDesignation;
    }

    public void setContactDesignation(String contactDesignation) {
        this.contactDesignation = contactDesignation;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactMobileNumber() {
        return contactMobileNumber;
    }

    public void setContactMobileNumber(String contactMobileNumber) {
        this.contactMobileNumber = contactMobileNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static CompanyResponseDto fromEntity(Company company) {
        if (company == null) {
            return null;
        }
        CompanyResponseDto response = new CompanyResponseDto();
        response.setId(company.getId());
        response.setCompanyName(company.getCompanyName());
        response.setEmail(company.getEmail());
        response.setLandline(company.getLandline());
        response.setAddress(company.getAddress());
        response.setCity(company.getCity());
        response.setCountry(company.getCountry());
        response.setWebsite(company.getWebsite());
        response.setDescription(company.getDescription());
        response.setStatus(company.getStatus());
        response.setBusinessCard(company.getBusinessCard());
        List<String> cards = new ArrayList<>();
        if (company.getBusinessCards() != null && !company.getBusinessCards().isEmpty()) {
            cards.addAll(company.getBusinessCards());
        } else if (company.getBusinessCard() != null && !company.getBusinessCard().trim().isEmpty()) {
            cards.add(company.getBusinessCard());
        }
        response.setBusinessCards(cards);
        if (response.getBusinessCard() == null && !cards.isEmpty()) {
            response.setBusinessCard(cards.get(0));
        }
        response.setContactName(company.getContactName());
        response.setContactDesignation(company.getContactDesignation());
        response.setContactEmail(company.getContactEmail());
        response.setContactMobileNumber(company.getContactMobileNumber());
        response.setCreatedAt(company.getCreatedAt());
        response.setUpdatedAt(company.getUpdatedAt());
        if (company.getBrands() != null) {
            response.setBrandIds(company.getBrands().stream().map(Brand::getId).toList());
            response.setBrands(company.getBrands().stream().map(BrandResponseDto::fromEntity).toList());
        }
        if (company.getProducts() != null) {
            response.setProductIds(company.getProducts().stream().map(Product::getId).toList());
            response.setProducts(company.getProducts().stream().map(ProductResponseDto::fromEntity).toList());
        }
        return response;
    }
}
