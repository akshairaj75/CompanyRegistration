package com.backend.companyapp.serviceImpl;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.backend.companyapp.dto.category.CategoryRequestDto;
import com.backend.companyapp.dto.category.CategoryResponseDto;
import com.backend.companyapp.entity.Category;
import com.backend.companyapp.repository.CategoryRepository;
import com.backend.companyapp.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    CategoryServiceImpl(CategoryRepository categoryRepository, FileStorageService fileStorageService) {
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public CategoryResponseDto createCategory(MultipartFile file, CategoryRequestDto categoryRequestDto) {

        Category category = new Category();

        category.setName(categoryRequestDto.getName());
        String fileName;
        try {
            fileName = fileStorageService.storeFile(file, "categories");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to store category image", e);
        }
        category.setCategoryImage(fileName);

        Category savedCategory = categoryRepository.save(category);

        return CategoryResponseDto.fromEntity(savedCategory);
    }

    @Override
    public List<CategoryResponseDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(CategoryResponseDto::fromEntity).toList();
    }

    @Override
    public CategoryResponseDto updateCategory(Long id, MultipartFile file, CategoryRequestDto categoryRequestDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setName(categoryRequestDto.getName());
        String fileName;
        try {
            fileName = fileStorageService.storeFile(file, "categories");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to store category image", e);
        }
        category.setCategoryImage(fileName);
        Category savedCategory = categoryRepository.save(category);
        return CategoryResponseDto.fromEntity(savedCategory);
    }

    @Override
    public CategoryResponseDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return CategoryResponseDto.fromEntity(category);
    }

}
