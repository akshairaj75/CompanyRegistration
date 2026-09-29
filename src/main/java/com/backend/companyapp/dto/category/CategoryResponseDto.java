package com.backend.companyapp.dto.category;

import com.backend.companyapp.entity.Category;

public class CategoryResponseDto {

    private Long id;
    private String name;
    private String categoryImage;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryImage() {
        return categoryImage;
    }

    public void setCategoryImage(String categoryImage) {
        this.categoryImage = categoryImage;
    }

    public static CategoryResponseDto fromEntity(Category category) {
        if (category == null) {
            return null;
        }
        CategoryResponseDto response = new CategoryResponseDto();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setCategoryImage(category.getCategoryImage());
        return response;
    }
}
