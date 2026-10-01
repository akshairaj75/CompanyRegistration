package com.backend.companyapp.service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.backend.companyapp.entity.Brand;
import com.backend.companyapp.entity.Company;
import com.backend.companyapp.entity.Product;
import com.backend.companyapp.repository.CompanyRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

@Service
public class CompanyPdfExportService {

    private final CompanyRepository companyRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public CompanyPdfExportService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public ByteArrayInputStream exportCompaniesToPdf() {
        List<Company> companies = companyRepository.findAll();

        Document document = new Document(PageSize.A4.rotate(), 24, 24, 28, 28);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);

            // Add Header/Footer event for page numbering
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    PdfContentByte cb = writer.getDirectContent();
                    Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(148, 163, 184));
                    
                    Phrase footerLeft = new Phrase("Official Enterprise Directory Report  •  Confidential", footerFont);
                    Phrase footerRight = new Phrase("Page " + writer.getPageNumber(), footerFont);
                    
                    ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, footerLeft, document.left(), document.bottom() - 14, 0);
                    ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, footerRight, document.right(), document.bottom() - 14, 0);
                }
            });

            document.open();

            // 1. Title Banner
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(15, 23, 42)); // #0F172A
            Paragraph title = new Paragraph("ENTERPRISE DIRECTORY & CORPORATE REGISTRY", titleFont);
            title.setSpacingAfter(3);
            document.add(title);

            // Subtitle with meta
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(71, 85, 105)); // #475569
            Paragraph subtitle = new Paragraph(
                "Exported on " + LocalDateTime.now().format(DATE_FORMATTER) +
                "  •  Total Registered Enterprises: " + companies.size(),
                subTitleFont
            );
            subtitle.setSpacingAfter(14);
            document.add(subtitle);

            // 2. Main Companies Table
            // 7 Columns: ID, Company & Status, Key Contact, Corporate Channels, Location, Brands, Products
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4.5f, 19.5f, 16.5f, 16.5f, 11.0f, 15.0f, 17.0f});
            table.setSpacingBefore(4);

            // Table Header Styles
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.WHITE);
            Color headerBg = new Color(5, 150, 105); // Emerald #059669

            String[] tableHeaders = {
                "ID",
                "Company Name & Status",
                "Key Contact Person",
                "Corporate Channels",
                "Headquarters",
                "Brands Dealt With",
                "Products Portfolio"
            };

            for (String headerText : tableHeaders) {
                PdfPCell headerCell = new PdfPCell(new Phrase(headerText, headerFont));
                headerCell.setBackgroundColor(headerBg);
                headerCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                headerCell.setPaddingTop(7);
                headerCell.setPaddingBottom(7);
                headerCell.setPaddingLeft(6);
                headerCell.setPaddingRight(6);
                headerCell.setBorderColor(new Color(4, 120, 87));
                table.addCell(headerCell);
            }

            // Data Fonts
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, new Color(15, 23, 42));
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 8.0f, new Color(30, 41, 59));
            Font mutedFont = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, new Color(100, 116, 139));

            Color altRowColor = new Color(248, 250, 252); // #F8FAFC
            Color borderColor = new Color(226, 232, 240); // #E2E8F0

            int rowIndex = 0;
            for (Company c : companies) {
                boolean isAlt = (rowIndex % 2 == 1);
                Color rowBg = isAlt ? altRowColor : Color.WHITE;

                // Col 0: ID
                PdfPCell c0 = new PdfPCell(new Phrase(String.valueOf(c.getId()), boldFont));
                c0.setHorizontalAlignment(Element.ALIGN_CENTER);
                c0.setVerticalAlignment(Element.ALIGN_MIDDLE);
                styleCell(c0, rowBg, borderColor);
                table.addCell(c0);

                // Col 1: Company Name & Status
                Paragraph pCompany = new Paragraph();
                pCompany.add(new Chunk(c.getCompanyName() != null ? c.getCompanyName() : "N/A", boldFont));
                pCompany.add(new Chunk("\n"));
                
                String status = c.getStatus() != null ? c.getStatus().toUpperCase() : "ACTIVE";
                Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.0f,
                    status.equals("ACTIVE") ? new Color(6, 95, 70) :
                    status.equals("PENDING") ? new Color(180, 83, 9) : new Color(185, 28, 28)
                );
                pCompany.add(new Chunk("Status: " + status, statusFont));
                PdfPCell c1 = new PdfPCell(pCompany);
                styleCell(c1, rowBg, borderColor);
                table.addCell(c1);

                // Col 2: Key Contact Person
                Paragraph pContact = new Paragraph();
                if (c.getContactName() != null && !c.getContactName().trim().isEmpty()) {
                    pContact.add(new Chunk(c.getContactName(), boldFont));
                    if (c.getContactDesignation() != null && !c.getContactDesignation().trim().isEmpty()) {
                        pContact.add(new Chunk("\n" + c.getContactDesignation(), mutedFont));
                    }
                    if (c.getContactMobileNumber() != null && !c.getContactMobileNumber().trim().isEmpty()) {
                        pContact.add(new Chunk("\nMob: " + c.getContactMobileNumber(), textFont));
                    }
                } else {
                    pContact.add(new Chunk("No Contact Listed", mutedFont));
                }
                PdfPCell c2 = new PdfPCell(pContact);
                styleCell(c2, rowBg, borderColor);
                table.addCell(c2);

                // Col 3: Corporate Channels
                Paragraph pChannels = new Paragraph();
                if (c.getEmail() != null && !c.getEmail().trim().isEmpty()) {
                    pChannels.add(new Chunk("Email: " + c.getEmail(), textFont));
                }
                if (c.getLandline() != null && !c.getLandline().trim().isEmpty()) {
                    pChannels.add(new Chunk("\nTel: " + c.getLandline(), textFont));
                }
                if (c.getWebsite() != null && !c.getWebsite().trim().isEmpty()) {
                    pChannels.add(new Chunk("\nWeb: " + c.getWebsite(), mutedFont));
                }
                if (pChannels.isEmpty()) {
                    pChannels.add(new Chunk("N/A", mutedFont));
                }
                PdfPCell c3 = new PdfPCell(pChannels);
                styleCell(c3, rowBg, borderColor);
                table.addCell(c3);

                // Col 4: Location
                Paragraph pLoc = new Paragraph();
                String city = c.getCity() != null ? c.getCity().trim() : "";
                String country = c.getCountry() != null ? c.getCountry().trim() : "";
                if (!city.isEmpty() || !country.isEmpty()) {
                    pLoc.add(new Chunk(city + (!city.isEmpty() && !country.isEmpty() ? ", " : "") + country, textFont));
                } else {
                    pLoc.add(new Chunk("Global", mutedFont));
                }
                PdfPCell c4 = new PdfPCell(pLoc);
                styleCell(c4, rowBg, borderColor);
                table.addCell(c4);

                // Col 5: Brands Dealt With
                Paragraph pBrands = new Paragraph();
                if (c.getBrands() != null && !c.getBrands().isEmpty()) {
                    String bNames = c.getBrands().stream()
                        .map(Brand::getBrandName)
                        .collect(Collectors.joining(", "));
                    pBrands.add(new Chunk(bNames, textFont));
                } else {
                    pBrands.add(new Chunk("None", mutedFont));
                }
                PdfPCell c5 = new PdfPCell(pBrands);
                styleCell(c5, rowBg, borderColor);
                table.addCell(c5);

                // Col 6: Products Portfolio
                Paragraph pProducts = new Paragraph();
                if (c.getProducts() != null && !c.getProducts().isEmpty()) {
                    String prodNames = c.getProducts().stream()
                        .map(Product::getName)
                        .collect(Collectors.joining(", "));
                    pProducts.add(new Chunk(prodNames, textFont));
                } else {
                    pProducts.add(new Chunk("None", mutedFont));
                }
                PdfPCell c6 = new PdfPCell(pProducts);
                styleCell(c6, rowBg, borderColor);
                table.addCell(c6);

                rowIndex++;
            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void styleCell(PdfPCell cell, Color bg, Color border) {
        cell.setBackgroundColor(bg);
        cell.setBorderColor(border);
        cell.setBorderWidth(0.6f);
        cell.setPaddingTop(5);
        cell.setPaddingBottom(5);
        cell.setPaddingLeft(6);
        cell.setPaddingRight(6);
    }
}
