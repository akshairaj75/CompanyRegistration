package com.backend.companyapp.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.backend.companyapp.dto.brand.BrandRequestDto;
import com.backend.companyapp.dto.brand.BrandResponseDto;

public interface BrandService {

    List<BrandResponseDto> getAllBrands();

    BrandResponseDto getBrandById(Long id);

    BrandResponseDto createBrand(MultipartFile file, BrandRequestDto brandRequestDto);

    BrandResponseDto updateBrand(Long id, MultipartFile file, BrandRequestDto brandRequestDto);

    void deleteBrand(Long id);

}
