package com.backend.companyapp.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.backend.companyapp.dto.company.CompanyRequestDto;
import com.backend.companyapp.dto.company.CompanyResponseDto;
import com.backend.companyapp.service.CompanyExcelExportService;
import com.backend.companyapp.service.CompanyPdfExportService;
import com.backend.companyapp.service.CompanyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/company-app/companies")
public class CompanyController {

    private final CompanyService companyService;
    private final CompanyExcelExportService companyExcelExportService;
    private final CompanyPdfExportService companyPdfExportService;

    public CompanyController(CompanyService companyService, 
                             CompanyExcelExportService companyExcelExportService,
                             CompanyPdfExportService companyPdfExportService) {
        this.companyService = companyService;
        this.companyExcelExportService = companyExcelExportService;
        this.companyPdfExportService = companyPdfExportService;
    }

    @GetMapping("/export/excel")
    public ResponseEntity<Resource> exportCompaniesToExcel() throws IOException {
        ByteArrayInputStream in = companyExcelExportService.exportCompaniesToExcel();
        InputStreamResource file = new InputStreamResource(in);
        String filename = "companies_directory_" + LocalDate.now() + ".xlsx";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(file);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<Resource> exportCompaniesToPdf() {
        ByteArrayInputStream in = companyPdfExportService.exportCompaniesToPdf();
        InputStreamResource file = new InputStreamResource(in);
        String filename = "companies_directory_" + LocalDate.now() + ".pdf";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(file);
    }

    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<Resource> exportCompanyProfilePdf(@PathVariable Long id) {
        ByteArrayInputStream in = companyPdfExportService.exportSingleCompanyPdf(id);
        InputStreamResource file = new InputStreamResource(in);
        String filename = "company_" + id + "_profile.pdf";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(file);
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

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CompanyResponseDto> createCompany(
        @RequestPart("data") CompanyRequestDto companyRequestDto,
        @RequestPart(name = "files", required = false) List<MultipartFile> files,
        @RequestPart(name = "file", required = false) MultipartFile file) {
        
        List<MultipartFile> allFiles = new java.util.ArrayList<>();
        if (files != null) {
            allFiles.addAll(files);
        }
        if (file != null && !file.isEmpty()) {
            allFiles.add(file);
        }
        CompanyResponseDto companyResponseDto = companyService.createCompany(companyRequestDto, allFiles);
        return ResponseEntity.status(HttpStatus.CREATED).body(companyResponseDto);
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CompanyResponseDto> updateCompany(
        @PathVariable Long id, 
        @RequestPart("data") CompanyRequestDto companyRequestDto,
        @RequestPart(name = "files", required = false) List<MultipartFile> files,
        @RequestPart(name = "file", required = false) MultipartFile file) {
        
        List<MultipartFile> allFiles = new java.util.ArrayList<>();
        if (files != null) {
            allFiles.addAll(files);
        }
        if (file != null && !file.isEmpty()) {
            allFiles.add(file);
        }
        CompanyResponseDto companyResponseDto = companyService.updateCompany(id, companyRequestDto, allFiles);
        return ResponseEntity.status(HttpStatus.OK).body(companyResponseDto);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    

}
