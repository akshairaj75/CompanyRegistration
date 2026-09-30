package com.backend.companyapp.serviceImpl;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.backend.companyapp.dto.brand.BrandRequestDto;
import com.backend.companyapp.dto.brand.BrandResponseDto;
import com.backend.companyapp.entity.Brand;
import com.backend.companyapp.repository.BrandRepository;
import com.backend.companyapp.service.BrandService;

@Service 
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final FileStorageService fileStorageService;

    public BrandServiceImpl(BrandRepository brandRepository, FileStorageService fileStorageService) {
        this.brandRepository = brandRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public List<BrandResponseDto> getAllBrands() {
        List<Brand> brands = brandRepository.findAll();
        return brands.stream().map(BrandResponseDto::fromEntity).toList();
    }

    @Override
    public BrandResponseDto getBrandById(Long id) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new RuntimeException("Brand not found"));
        return BrandResponseDto.fromEntity(brand);
    }

    @Override
    public BrandResponseDto createBrand(MultipartFile file, BrandRequestDto brandRequestDto) {
        
        Brand brand = new Brand();
        brand.setBrandName(brandRequestDto.getBrandName());
        if (file != null && !file.isEmpty()) {
            String fileName;
            try {
                fileName = fileStorageService.storeFile(file, "brands");
                brand.setBrandLogo(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        brand.setFeatured(brandRequestDto.isFeatured());
        brand = brandRepository.save(brand);
        return BrandResponseDto.fromEntity(brand);
        
    }

    @Override
    public BrandResponseDto updateBrand(Long id, MultipartFile file, BrandRequestDto brandRequestDto) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new RuntimeException("Brand not found"));
        brand.setBrandName(brandRequestDto.getBrandName());
        brand.setFeatured(brandRequestDto.isFeatured());
        if (file != null && !file.isEmpty()) {
            try {
                String oldLogo = brand.getBrandLogo();
                String fileName = fileStorageService.storeFile(file, "brands");
                brand.setBrandLogo(fileName);
                if (oldLogo != null && !oldLogo.trim().isEmpty()) {
                    fileStorageService.deleteFile(oldLogo, "brands");
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to update brand logo", e);
            }
        }
        brand = brandRepository.save(brand);
        return BrandResponseDto.fromEntity(brand);
    }

    @Override
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));
        if (brand.getBrandLogo() != null) {
            fileStorageService.deleteFile(brand.getBrandLogo(), "brands");
        }
        for (com.backend.companyapp.entity.Company company : brand.getCompanies()) {
            company.getBrands().remove(brand);
        }
        brand.getCompanies().clear();
        brandRepository.delete(brand);
    }

}
