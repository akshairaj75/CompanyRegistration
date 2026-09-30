package com.backend.companyapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.backend.companyapp.dto.company.CompanyRequestDto;
import com.backend.companyapp.dto.company.CompanyResponseDto;
import com.backend.companyapp.service.CompanyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/company-app/companies")
public class CompanyController {

    private final CompanyService companyService;

    CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<CompanyResponseDto>> getAllCompanies() {
        List<CompanyResponseDto> companyResponseDtos = companyService.getAllCompanies();
        return ResponseEntity.status(HttpStatus.OK).body(companyResponseDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponseDto> getCompanyById(@PathVariable Long id) {
        CompanyResponseDto companyResponseDto = companyService.getCompanyById(id);
        return ResponseEntity.status(HttpStatus.OK).body(companyResponseDto);
    }

    @PostMapping("/create")
    public ResponseEntity<CompanyResponseDto> createCompany(
        @RequestPart("data") CompanyRequestDto companyRequestDto,
        @RequestPart(name = "file", required = false) MultipartFile file) {
        CompanyResponseDto companyResponseDto = companyService.createCompany(companyRequestDto, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(companyResponseDto);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<CompanyResponseDto> updateCompany(
        @PathVariable Long id, 
        @RequestPart(name = "file", required = false) MultipartFile file,
        @RequestPart(name = "data", required = true) CompanyRequestDto companyRequestDto) {
        CompanyResponseDto companyResponseDto = companyService.updateCompany(id, companyRequestDto, file);
        return ResponseEntity.status(HttpStatus.OK).body(companyResponseDto);
    }
    

}
