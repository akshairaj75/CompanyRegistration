package com.backend.companyapp.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.backend.companyapp.entity.Brand;
import com.backend.companyapp.entity.Company;
import com.backend.companyapp.entity.Product;
import com.backend.companyapp.repository.CompanyRepository;

@Service
public class CompanyExcelExportService {

    private final CompanyRepository companyRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CompanyExcelExportService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public ByteArrayInputStream exportCompaniesToExcel() throws IOException {
        List<Company> companies = companyRepository.findAll();

        String[] headers = {
            "ID",
            "Company Name",
            "Status",
            "Email",
            "Landline",
            "Address",
            "City",
            "Country",
            "Website",
            "Contact Person",
            "Designation",
            "Contact Mobile",
            "Contact Email",
            "Brands Dealt With",
            "Products Portfolio",
            "Description",
            "Created Date"
        };

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Companies Directory");

            // 1. Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            byte[] emeraldRgb = new byte[]{(byte) 5, (byte) 150, (byte) 105}; // #059669
            headerStyle.setFillForegroundColor(new XSSFColor(emeraldRgb, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setFontName("Calibri");
            headerStyle.setFont(headerFont);
            setBorders(headerStyle, BorderStyle.MEDIUM, IndexedColors.GREY_40_PERCENT.getIndex());

            // 2. Regular Data Row Styles
            CellStyle normalStyle = workbook.createCellStyle();
            normalStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorders(normalStyle, BorderStyle.THIN, IndexedColors.GREY_25_PERCENT.getIndex());

            CellStyle alternateStyle = workbook.createCellStyle();
            byte[] zebraRgb = new byte[]{(byte) 248, (byte) 250, (byte) 252}; // #F8FAFC
            alternateStyle.setFillForegroundColor(new XSSFColor(zebraRgb, null));
            alternateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            alternateStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorders(alternateStyle, BorderStyle.THIN, IndexedColors.GREY_25_PERCENT.getIndex());

            // Centered style for ID and Status
            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.cloneStyleFrom(normalStyle);
            centerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle centerAltStyle = workbook.createCellStyle();
            centerAltStyle.cloneStyleFrom(alternateStyle);
            centerAltStyle.setAlignment(HorizontalAlignment.CENTER);

            // 3. Create Header Row
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(28);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 4. Fill Data Rows
            int rowIndex = 1;
            for (Company company : companies) {
                Row row = sheet.createRow(rowIndex);
                row.setHeightInPoints(22);
                boolean isAlt = (rowIndex % 2 == 0);
                CellStyle rowStyle = isAlt ? alternateStyle : normalStyle;
                CellStyle rowCenterStyle = isAlt ? centerAltStyle : centerStyle;

                // Col 0: ID
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(company.getId() != null ? company.getId() : 0);
                cell0.setCellStyle(rowCenterStyle);

                // Col 1: Company Name
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(company.getCompanyName() != null ? company.getCompanyName() : "");
                cell1.setCellStyle(rowStyle);

                // Col 2: Status
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(company.getStatus() != null ? company.getStatus() : "");
                cell2.setCellStyle(rowCenterStyle);

                // Col 3: Email
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(company.getEmail() != null ? company.getEmail() : "");
                cell3.setCellStyle(rowStyle);

                // Col 4: Landline
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(company.getLandline() != null ? company.getLandline() : "");
                cell4.setCellStyle(rowStyle);

                // Col 5: Address
                Cell cell5 = row.createCell(5);
                cell5.setCellValue(company.getAddress() != null ? company.getAddress() : "");
                cell5.setCellStyle(rowStyle);

                // Col 6: City
                Cell cell6 = row.createCell(6);
                cell6.setCellValue(company.getCity() != null ? company.getCity() : "");
                cell6.setCellStyle(rowStyle);

                // Col 7: Country
                Cell cell7 = row.createCell(7);
                cell7.setCellValue(company.getCountry() != null ? company.getCountry() : "");
                cell7.setCellStyle(rowStyle);

                // Col 8: Website
                Cell cell8 = row.createCell(8);
                cell8.setCellValue(company.getWebsite() != null ? company.getWebsite() : "");
                cell8.setCellStyle(rowStyle);

                // Col 9: Contact Person
                Cell cell9 = row.createCell(9);
                cell9.setCellValue(company.getContactName() != null ? company.getContactName() : "");
                cell9.setCellStyle(rowStyle);

                // Col 10: Designation
                Cell cell10 = row.createCell(10);
                cell10.setCellValue(company.getContactDesignation() != null ? company.getContactDesignation() : "");
                cell10.setCellStyle(rowStyle);

                // Col 11: Contact Mobile
                Cell cell11 = row.createCell(11);
                cell11.setCellValue(company.getContactMobileNumber() != null ? company.getContactMobileNumber() : "");
                cell11.setCellStyle(rowStyle);

                // Col 12: Contact Email
                Cell cell12 = row.createCell(12);
                cell12.setCellValue(company.getContactEmail() != null ? company.getContactEmail() : "");
                cell12.setCellStyle(rowStyle);

                // Col 13: Brands Dealt With
                Cell cell13 = row.createCell(13);
                String brandNames = (company.getBrands() != null && !company.getBrands().isEmpty())
                    ? company.getBrands().stream().map(Brand::getBrandName).collect(Collectors.joining(", "))
                    : "None";
                cell13.setCellValue(brandNames);
                cell13.setCellStyle(rowStyle);

                // Col 14: Products Portfolio
                Cell cell14 = row.createCell(14);
                String productNames = (company.getProducts() != null && !company.getProducts().isEmpty())
                    ? company.getProducts().stream().map(Product::getName).collect(Collectors.joining(", "))
                    : "None";
                cell14.setCellValue(productNames);
                cell14.setCellStyle(rowStyle);

                // Col 15: Description
                Cell cell15 = row.createCell(15);
                cell15.setCellValue(company.getDescription() != null ? company.getDescription() : "");
                cell15.setCellStyle(rowStyle);

                // Col 16: Created Date
                Cell cell16 = row.createCell(16);
                String createdDate = company.getCreatedAt() != null ? company.getCreatedAt().format(DATE_FORMATTER) : "N/A";
                cell16.setCellValue(createdDate);
                cell16.setCellStyle(rowCenterStyle);

                rowIndex++;
            }

            // Auto-size columns with minimum padding
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                // Extra padding for breathing room
                sheet.setColumnWidth(i, Math.min(Math.max(currentWidth + 1200, 3200), 12000));
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private void setBorders(CellStyle style, BorderStyle borderStyle, short colorIndex) {
        style.setBorderTop(borderStyle);
        style.setTopBorderColor(colorIndex);
        style.setBorderBottom(borderStyle);
        style.setBottomBorderColor(colorIndex);
        style.setBorderLeft(borderStyle);
        style.setLeftBorderColor(colorIndex);
        style.setBorderRight(borderStyle);
        style.setRightBorderColor(colorIndex);
    }
}
