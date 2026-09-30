package com.backend.companyapp.dto.brand;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.backend.companyapp.entity.Brand;

public class BrandResponseDto {

    private Long id;
    private String brandName;
    private String brandLogo;
    private boolean isFeatured;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getBrandLogo() {
        return brandLogo;
    }

    public void setBrandLogo(String brandLogo) {
        this.brandLogo = brandLogo;
    }

    @JsonProperty("isFeatured")
    public boolean isFeatured() {
        return isFeatured;
    }

    @JsonProperty("isFeatured")
    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public void setIsFeatured(boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public static BrandResponseDto fromEntity(Brand brand) {
        if (brand == null) {
            return null;
        }
        BrandResponseDto response = new BrandResponseDto();
        response.setId(brand.getId());
        response.setBrandName(brand.getBrandName());
        response.setBrandLogo(brand.getBrandLogo());
        response.setFeatured(brand.isFeatured());
        return response;
    }

}
