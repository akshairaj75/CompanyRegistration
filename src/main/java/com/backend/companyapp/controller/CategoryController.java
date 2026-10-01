package com.backend.companyapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.backend.companyapp.dto.category.CategoryRequestDto;
import com.backend.companyapp.dto.category.CategoryResponseDto;
import com.backend.companyapp.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/company-app/categories")
public class CategoryController {

    private final CategoryService categoryService;

    CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/create")
    public ResponseEntity<CategoryResponseDto> createCategory(
            @RequestPart(name = "file", required = false) MultipartFile file,
            @RequestPart(name = "data", required = false) CategoryRequestDto dataDto,
            @RequestPart(name = "categoryRequestDto", required = false) CategoryRequestDto categoryRequestDto) {
        CategoryRequestDto dto = dataDto != null ? dataDto : categoryRequestDto;
        try {
            CategoryResponseDto categoryResponseDto = categoryService.createCategory(file, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(categoryResponseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/create/bulk")
    public ResponseEntity<List<CategoryResponseDto>> createBulkCategories(
            @RequestBody List<CategoryRequestDto> categoryRequestDtos) {
        List<CategoryResponseDto> categoryResponseDtos = categoryRequestDtos.stream()
                .map(categoryRequestDto -> categoryService.createCategory(null, categoryRequestDto))
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryResponseDtos);
    }

    @GetMapping({"", "/get-all"})
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        List<CategoryResponseDto> categoryResponseDtos = categoryService.getAllCategories();
        return ResponseEntity.status(HttpStatus.OK).body(categoryResponseDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable Long id) {
        CategoryResponseDto categoryResponseDto = categoryService.getCategoryById(id);
        return ResponseEntity.status(HttpStatus.OK).body(categoryResponseDto);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(
            @PathVariable Long id,
            @RequestPart(name = "file", required = false) MultipartFile file,
            @RequestPart(name = "data", required = false) CategoryRequestDto dataDto,
            @RequestPart(name = "categoryRequestDto", required = false) CategoryRequestDto categoryRequestDto) {
        CategoryRequestDto dto = dataDto != null ? dataDto : categoryRequestDto;
        try {
            CategoryResponseDto categoryResponseDto = categoryService.updateCategory(id, file, dto);
            return ResponseEntity.status(HttpStatus.OK).body(categoryResponseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
