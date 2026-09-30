package com.backend.companyapp.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.backend.companyapp.dto.company.CompanyRequestDto;
import com.backend.companyapp.dto.company.CompanyResponseDto;

public interface CompanyService {

    List<CompanyResponseDto> getAllCompanies();

    CompanyResponseDto getCompanyById(Long id);

    CompanyResponseDto createCompany(CompanyRequestDto companyRequestDto, MultipartFile file);

    CompanyResponseDto updateCompany(Long id, CompanyRequestDto companyRequestDto, MultipartFile file);

    void deleteCompany(Long id);

}
