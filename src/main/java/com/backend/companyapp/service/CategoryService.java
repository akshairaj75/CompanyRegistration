package com.backend.companyapp.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.backend.companyapp.dto.category.CategoryRequestDto;
import com.backend.companyapp.dto.category.CategoryResponseDto;

public interface CategoryService {

    CategoryResponseDto createCategory(MultipartFile categoryImage, CategoryRequestDto categoryRequestDto);

    List<CategoryResponseDto> getAllCategories();

    CategoryResponseDto updateCategory(Long id, MultipartFile file, CategoryRequestDto categoryRequestDto);

    CategoryResponseDto getCategoryById(Long id);

    void deleteCategory(Long id);

}
