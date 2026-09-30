package com.backend.companyapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.backend.companyapp.dto.brand.BrandRequestDto;
import com.backend.companyapp.dto.brand.BrandResponseDto;
import com.backend.companyapp.service.BrandService;

@RestController
@RequestMapping("/api/company-app/brands")
public class BrandController {

    private final BrandService brandService;

    BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<BrandResponseDto>> getAllBrands() {
        try {
            List<BrandResponseDto> brandResponseDtos = brandService.getAllBrands();
            return ResponseEntity.status(HttpStatus.OK).body(brandResponseDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandResponseDto> getBrandById(@PathVariable Long id) {
        BrandResponseDto brandResponseDto = brandService.getBrandById(id);
        return ResponseEntity.status(HttpStatus.OK).body(brandResponseDto);
    }

    @PostMapping("/create")
    public ResponseEntity<BrandResponseDto> createBrand(
            @RequestPart(name = "file", required = false) MultipartFile file,
            @RequestPart(required = true) BrandRequestDto brandRequestDto) {
        try {
            BrandResponseDto brandResponseDto = brandService.createBrand(file, brandRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(brandResponseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<BrandResponseDto> updateBrand(
            @PathVariable Long id,
            @RequestPart(name = "file", required = false) MultipartFile file,
            @RequestPart(required = true) BrandRequestDto brandRequestDto) {
        try {
            BrandResponseDto brandResponseDto = brandService.updateBrand(id, file, brandRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body(brandResponseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
