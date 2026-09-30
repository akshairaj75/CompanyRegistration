package com.backend.companyapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.backend.companyapp.dto.product.ProductRequestDto;
import com.backend.companyapp.dto.product.ProductResponseDto;
import com.backend.companyapp.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/company-app/products")
public class ProductController {

    private final ProductService productService;

    ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        try {
            List<ProductResponseDto> productResponseDtos = productService.getAllProducts();
            return ResponseEntity.status(HttpStatus.OK).body(productResponseDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        try {
            ProductResponseDto productResponseDto = productService.getProductById(id);
            return ResponseEntity.status(HttpStatus.OK).body(productResponseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @PostMapping("/create")
    public ResponseEntity<ProductResponseDto> createProduct(
            @RequestPart(name = "file", required = false) MultipartFile file,
            @RequestPart(name = "data", required = false) ProductRequestDto dataDto,
            @RequestPart(name = "productRequestDto", required = false) ProductRequestDto productRequestDto) {
        ProductRequestDto dto = dataDto != null ? dataDto : productRequestDto;
        try {
            ProductResponseDto productResponseDto = productService.createProduct(file, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(productResponseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @RequestPart(name = "file", required = false) MultipartFile file,
            @RequestPart(name = "data", required = false) ProductRequestDto dataDto,
            @RequestPart(name = "productRequestDto", required = false) ProductRequestDto productRequestDto) {
        ProductRequestDto dto = dataDto != null ? dataDto : productRequestDto;
        try {
            ProductResponseDto productResponseDto = productService.updateProduct(id, file, dto);
            return ResponseEntity.status(HttpStatus.OK).body(productResponseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
