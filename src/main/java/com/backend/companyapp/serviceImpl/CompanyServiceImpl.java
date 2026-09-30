package com.backend.companyapp.serviceImpl;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.backend.companyapp.dto.company.CompanyRequestDto;
import com.backend.companyapp.dto.company.CompanyResponseDto;
import com.backend.companyapp.entity.Company;
import com.backend.companyapp.repository.CompanyRepository;
import com.backend.companyapp.service.CompanyService;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final FileStorageService fileStorageService;

    public CompanyServiceImpl(CompanyRepository companyRepository,
            FileStorageService fileStorageService) {
        this.companyRepository = companyRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public List<CompanyResponseDto> getAllCompanies() {
        List<Company> companies = companyRepository.findAll();
        return companies.stream().map(CompanyResponseDto::fromEntity).toList();
    }

    @Override
    public CompanyResponseDto getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        return CompanyResponseDto.fromEntity(company);
    }

    @Override
    public CompanyResponseDto createCompany(CompanyRequestDto companyRequestDto, MultipartFile file) {
        String fileName;

        Company company = new Company();
        company.setCompanyName(companyRequestDto.getCompanyName());
        company.setEmail(companyRequestDto.getEmail());
        company.setLandline(companyRequestDto.getLandline());
        company.setAddress(companyRequestDto.getAddress());
        company.setCity(companyRequestDto.getCity());
        company.setCountry(companyRequestDto.getCountry());
        company.setWebsite(companyRequestDto.getWebsite());
        company.setDescription(companyRequestDto.getDescription());
        if (companyRequestDto.getStatus() != null && !companyRequestDto.getStatus().trim().isEmpty()) {
            company.setStatus(companyRequestDto.getStatus());
        }
        company.setContactName(companyRequestDto.getContactName());
        company.setContactDesignation(companyRequestDto.getContactDesignation());
        company.setContactEmail(companyRequestDto.getContactEmail());
        company.setContactMobileNumber(companyRequestDto.getContactMobileNumber());

        if (file != null && !file.isEmpty()) {
            try {
                fileName = fileStorageService.storeFile(file, "company_logos");
                company.setBusinessCard(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Company savedCompany = companyRepository.save(company);
        return CompanyResponseDto.fromEntity(savedCompany);
    }

    @Override
    public CompanyResponseDto updateCompany(Long id, CompanyRequestDto companyRequestDto, MultipartFile file) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        company.setCompanyName(companyRequestDto.getCompanyName());
        company.setEmail(companyRequestDto.getEmail());
        company.setLandline(companyRequestDto.getLandline());
        company.setAddress(companyRequestDto.getAddress());
        company.setCity(companyRequestDto.getCity());
        company.setCountry(companyRequestDto.getCountry());
        company.setWebsite(companyRequestDto.getWebsite());
        company.setDescription(companyRequestDto.getDescription());
        if (companyRequestDto.getStatus() != null && !companyRequestDto.getStatus().trim().isEmpty()) {
            company.setStatus(companyRequestDto.getStatus());
        }
        company.setContactName(companyRequestDto.getContactName());
        company.setContactDesignation(companyRequestDto.getContactDesignation());
        company.setContactEmail(companyRequestDto.getContactEmail());
        company.setContactMobileNumber(companyRequestDto.getContactMobileNumber());

        if (file != null && !file.isEmpty()) {
            try {
                String fileName = fileStorageService.storeFile(file, "company_logos");
                company.setBusinessCard(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Company savedCompany = companyRepository.save(company);
        return CompanyResponseDto.fromEntity(savedCompany);
    }

    @Override
    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));
        if (company.getBusinessCard() != null) {
            fileStorageService.deleteFile(company.getBusinessCard(), "company_logos");
        }
        company.getBrands().clear();
        company.getProducts().clear();
        companyRepository.delete(company);
    }

}
