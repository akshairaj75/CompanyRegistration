package com.backend.companyapp.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.backend.companyapp.dto.company.CompanyRequestDto;
import com.backend.companyapp.dto.company.CompanyResponseDto;

public interface CompanyService {

    List<CompanyResponseDto> getAllCompanies();

    CompanyResponseDto getCompanyById(Long id);

    CompanyResponseDto createCompany(CompanyRequestDto companyRequestDto, List<MultipartFile> files);

    CompanyResponseDto updateCompany(Long id, CompanyRequestDto companyRequestDto, List<MultipartFile> files);

    void deleteCompany(Long id);

}
