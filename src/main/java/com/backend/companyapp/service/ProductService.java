package com.backend.companyapp.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.backend.companyapp.dto.product.ProductRequestDto;
import com.backend.companyapp.dto.product.ProductResponseDto;

public interface ProductService {

    List<ProductResponseDto> getAllProducts();

    ProductResponseDto getProductById(Long id);

    ProductResponseDto createProduct(MultipartFile file, ProductRequestDto productRequestDto);

    ProductResponseDto updateProduct(Long id, MultipartFile file, ProductRequestDto productRequestDto);

    void deleteProduct(Long id);
}
