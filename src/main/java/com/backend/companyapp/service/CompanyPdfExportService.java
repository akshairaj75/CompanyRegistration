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

import java.io.File;
import org.springframework.beans.factory.annotation.Value;

@Service
public class CompanyPdfExportService {

    private final CompanyRepository companyRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    public CompanyPdfExportService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public ByteArrayInputStream exportSingleCompanyPdf(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company with id " + companyId + " not found"));

        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);

            // Add Header/Footer event
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    PdfContentByte cb = writer.getDirectContent();
                    Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(148, 163, 184));
                    
                    Phrase footerLeft = new Phrase("Official Enterprise Profile  •  " + company.getCompanyName(), footerFont);
                    Phrase footerRight = new Phrase("Page " + writer.getPageNumber(), footerFont);
                    
                    ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, footerLeft, document.left(), document.bottom() - 14, 0);
                    ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, footerRight, document.right(), document.bottom() - 14, 0);
                }
            });

            document.open();

            // 1. Top Accent Bar / Header Banner
            PdfPTable headerBanner = new PdfPTable(2);
            headerBanner.setWidthPercentage(100);
            headerBanner.setWidths(new float[]{75f, 25f});
            headerBanner.setSpacingAfter(14);

            PdfPCell titleCell = new PdfPCell();
            titleCell.setBorder(Rectangle.NO_BORDER);
            
            Font pretitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, new Color(5, 150, 105));
            Paragraph pretitle = new Paragraph("ENTERPRISE PROFILE & CONTACT SHOWCASE", pretitleFont);
            titleCell.addElement(pretitle);

            Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, new Color(15, 23, 42));
            Paragraph name = new Paragraph(company.getCompanyName(), nameFont);
            name.setSpacingAfter(4);
            titleCell.addElement(name);

            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 8f, new Color(100, 116, 139));
            String dateStr = company.getCreatedAt() != null ? company.getCreatedAt().format(DATE_FORMATTER) : LocalDateTime.now().format(DATE_FORMATTER);
            titleCell.addElement(new Paragraph("Registry ID: #" + company.getId() + "  •  Generated: " + dateStr, metaFont));
            headerBanner.addCell(titleCell);

            PdfPCell statusCell = new PdfPCell();
            statusCell.setBorder(Rectangle.NO_BORDER);
            statusCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            statusCell.setVerticalAlignment(Element.ALIGN_TOP);

            String status = company.getStatus() != null ? company.getStatus().toUpperCase() : "ACTIVE";
            Color statusBg = status.equals("ACTIVE") ? new Color(209, 250, 229) :
                             status.equals("PENDING") ? new Color(254, 243, 199) : new Color(254, 226, 226);
            Color statusColor = status.equals("ACTIVE") ? new Color(6, 95, 70) :
                                status.equals("PENDING") ? new Color(180, 83, 9) : new Color(185, 28, 28);
            
            PdfPTable statusTable = new PdfPTable(1);
            statusTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            statusTable.setWidthPercentage(95);
            PdfPCell badgeCell = new PdfPCell(new Phrase("STATUS: " + status, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, statusColor)));
            badgeCell.setBackgroundColor(statusBg);
            badgeCell.setBorderColor(statusColor);
            badgeCell.setBorderWidth(1f);
            badgeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            badgeCell.setPaddingTop(5);
            badgeCell.setPaddingBottom(5);
            statusTable.addCell(badgeCell);
            statusCell.addElement(statusTable);
            headerBanner.addCell(statusCell);

            document.add(headerBanner);

            // Divider Line
            PdfPTable divider = new PdfPTable(1);
            divider.setWidthPercentage(100);
            PdfPCell dCell = new PdfPCell();
            dCell.setFixedHeight(2.5f);
            dCell.setBackgroundColor(new Color(5, 150, 105));
            dCell.setBorder(Rectangle.NO_BORDER);
            divider.addCell(dCell);
            divider.setSpacingAfter(14);
            document.add(divider);

            // 2. Contact Details & Corporate Information (Two Column Box)
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.5f, new Color(15, 23, 42));
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f, new Color(71, 85, 105));
            Font valFont = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, new Color(15, 23, 42));
            Font linkFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, new Color(2, 132, 199));

            PdfPTable infoGrid = new PdfPTable(2);
            infoGrid.setWidthPercentage(100);
            infoGrid.setWidths(new float[]{50f, 50f});
            infoGrid.setSpacingAfter(14);

            // Left Box: Key Contact
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBackgroundColor(new Color(248, 250, 252));
            leftCell.setBorderColor(new Color(226, 232, 240));
            leftCell.setBorderWidth(1f);
            leftCell.setPadding(10);

            Paragraph pLeftTitle = new Paragraph("PRIMARY KEY CONTACT", sectionTitleFont);
            pLeftTitle.setSpacingAfter(8);
            leftCell.addElement(pLeftTitle);

            addInfoRow(leftCell, "Contact Person:", (company.getContactName() != null ? company.getContactName() : "N/A"), labelFont, valFont);
            addInfoRow(leftCell, "Designation:", (company.getContactDesignation() != null ? company.getContactDesignation() : "N/A"), labelFont, valFont);
            addInfoRow(leftCell, "Mobile Number:", (company.getContactMobileNumber() != null ? company.getContactMobileNumber() : "N/A"), labelFont, valFont);
            addInfoRow(leftCell, "Direct Email:", (company.getContactEmail() != null ? company.getContactEmail() : "N/A"), labelFont, valFont);
            infoGrid.addCell(leftCell);

            // Right Box: Corporate Channels & Address
            PdfPCell rightCell = new PdfPCell();
            rightCell.setBackgroundColor(new Color(248, 250, 252));
            rightCell.setBorderColor(new Color(226, 232, 240));
            rightCell.setBorderWidth(1f);
            rightCell.setPadding(10);

            Paragraph pRightTitle = new Paragraph("CORPORATE CHANNELS & HQ", sectionTitleFont);
            pRightTitle.setSpacingAfter(8);
            rightCell.addElement(pRightTitle);

            addInfoRow(rightCell, "Corporate Email:", (company.getEmail() != null ? company.getEmail() : "N/A"), labelFont, valFont);
            addInfoRow(rightCell, "Landline Phone:", (company.getLandline() != null ? company.getLandline() : "N/A"), labelFont, valFont);
            
            String loc = "";
            if (company.getAddress() != null && !company.getAddress().trim().isEmpty()) loc += company.getAddress() + ", ";
            if (company.getCity() != null && !company.getCity().trim().isEmpty()) loc += company.getCity() + ", ";
            loc += (company.getCountry() != null ? company.getCountry() : "Global");
            addInfoRow(rightCell, "Headquarters:", loc, labelFont, valFont);

            addInfoRow(rightCell, "Official Website:", (company.getWebsite() != null ? company.getWebsite() : "N/A"), labelFont, linkFont);
            infoGrid.addCell(rightCell);

            document.add(infoGrid);

            // 3. Brands & Products Portfolio
            PdfPTable portfolioTable = new PdfPTable(2);
            portfolioTable.setWidthPercentage(100);
            portfolioTable.setWidths(new float[]{50f, 50f});
            portfolioTable.setSpacingAfter(14);

            PdfPCell brandsCell = new PdfPCell();
            brandsCell.setBackgroundColor(Color.WHITE);
            brandsCell.setBorderColor(new Color(226, 232, 240));
            brandsCell.setBorderWidth(1f);
            brandsCell.setPadding(10);
            brandsCell.addElement(new Paragraph("BRANDS DEALT WITH", sectionTitleFont));
            
            if (company.getBrands() != null && !company.getBrands().isEmpty()) {
                String brandsStr = company.getBrands().stream().map(Brand::getBrandName).collect(Collectors.joining(", "));
                Paragraph pB = new Paragraph(brandsStr, valFont);
                pB.setSpacingBefore(4);
                brandsCell.addElement(pB);
            } else {
                Paragraph pB = new Paragraph("No partner brands linked.", labelFont);
                pB.setSpacingBefore(4);
                brandsCell.addElement(pB);
            }
            portfolioTable.addCell(brandsCell);

            PdfPCell prodsCell = new PdfPCell();
            prodsCell.setBackgroundColor(Color.WHITE);
            prodsCell.setBorderColor(new Color(226, 232, 240));
            prodsCell.setBorderWidth(1f);
            prodsCell.setPadding(10);
            prodsCell.addElement(new Paragraph("PRODUCTS CATALOG", sectionTitleFont));
            
            if (company.getProducts() != null && !company.getProducts().isEmpty()) {
                String prodsStr = company.getProducts().stream()
                    .map(Product::getName)
                    .limit(10)
                    .collect(Collectors.joining(", "));
                if (company.getProducts().size() > 10) {
                    prodsStr += " (+" + (company.getProducts().size() - 10) + " more)";
                }
                Paragraph pP = new Paragraph(prodsStr, valFont);
                pP.setSpacingBefore(4);
                prodsCell.addElement(pP);
            } else {
                Paragraph pP = new Paragraph("No specific products catalogued.", labelFont);
                pP.setSpacingBefore(4);
                prodsCell.addElement(pP);
            }
            portfolioTable.addCell(prodsCell);

            document.add(portfolioTable);

            // 4. Corporate Overview
            if (company.getDescription() != null && !company.getDescription().trim().isEmpty()) {
                PdfPTable descTable = new PdfPTable(1);
                descTable.setWidthPercentage(100);
                descTable.setSpacingAfter(14);
                PdfPCell dBox = new PdfPCell();
                dBox.setBackgroundColor(new Color(248, 250, 252));
                dBox.setBorderColor(new Color(226, 232, 240));
                dBox.setBorderWidth(1f);
                dBox.setPadding(10);
                dBox.addElement(new Paragraph("CORPORATE OVERVIEW & SCOPE", sectionTitleFont));
                Paragraph descP = new Paragraph(company.getDescription(), valFont);
                descP.setSpacingBefore(4);
                dBox.addElement(descP);
                descTable.addCell(dBox);
                document.add(descTable);
            }

            // 5. ATTACHED BUSINESS CARDS IMAGES SECTION
            List<String> cardPaths = new java.util.ArrayList<>();
            if (company.getBusinessCards() != null && !company.getBusinessCards().isEmpty()) {
                cardPaths.addAll(company.getBusinessCards());
            } else if (company.getBusinessCard() != null && !company.getBusinessCard().trim().isEmpty()) {
                cardPaths.add(company.getBusinessCard());
            }

            Paragraph cardsTitle = new Paragraph("ATTACHED VISITING CARDS & BRAND ASSETS (" + cardPaths.size() + ")", sectionTitleFont);
            cardsTitle.setSpacingBefore(6);
            cardsTitle.setSpacingAfter(8);
            document.add(cardsTitle);

            if (cardPaths.isEmpty()) {
                PdfPTable noCardTable = new PdfPTable(1);
                noCardTable.setWidthPercentage(100);
                PdfPCell nCell = new PdfPCell(new Phrase("No visiting card or brand asset files attached.", labelFont));
                nCell.setBackgroundColor(new Color(248, 250, 252));
                nCell.setBorderColor(new Color(226, 232, 240));
                nCell.setPadding(10);
                noCardTable.addCell(nCell);
                document.add(noCardTable);
            } else {
                int columns = cardPaths.size() > 1 ? 2 : 1;
                PdfPTable cardsGrid = new PdfPTable(columns);
                cardsGrid.setWidthPercentage(100);
                cardsGrid.setSpacingAfter(14);

                int cardIndex = 1;
                for (String rawPath : cardPaths) {
                    PdfPCell cardCell = new PdfPCell();
                    cardCell.setBackgroundColor(Color.WHITE);
                    cardCell.setBorderColor(new Color(203, 213, 225));
                    cardCell.setBorderWidth(1.2f);
                    cardCell.setPadding(8);
                    cardCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                    Paragraph cardHeader = new Paragraph("Visiting Card #" + cardIndex, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, new Color(15, 23, 42)));
                    cardHeader.setSpacingAfter(6);
                    cardCell.addElement(cardHeader);

                    if (rawPath.toLowerCase().endsWith(".pdf")) {
                        // PDF File attachment notice
                        Paragraph pdfNote = new Paragraph("📄 Attached PDF Document:\n" + new File(rawPath).getName(), labelFont);
                        pdfNote.setAlignment(Element.ALIGN_CENTER);
                        cardCell.addElement(pdfNote);
                    } else {
                        // Image file
                        File imgFile = resolveFile(rawPath);
                        boolean imageLoaded = false;
                        if (imgFile != null && imgFile.exists()) {
                            try {
                                Image cardImg = Image.getInstance(imgFile.getAbsolutePath());
                                cardImg.scaleToFit(230f, 140f);
                                cardImg.setAlignment(Element.ALIGN_CENTER);
                                cardCell.addElement(cardImg);
                                imageLoaded = true;
                            } catch (Exception ex) {
                                // Fallback if image format unsupported by lowagie
                            }
                        }

                        if (!imageLoaded) {
                            Paragraph placeholder = new Paragraph("Attached Image File:\n" + new File(rawPath).getName(), labelFont);
                            placeholder.setAlignment(Element.ALIGN_CENTER);
                            cardCell.addElement(placeholder);
                        }
                    }

                    cardsGrid.addCell(cardCell);
                    cardIndex++;
                }

                // If odd number of cells in 2-column table, add empty cell
                if (columns == 2 && cardPaths.size() % 2 != 0) {
                    PdfPCell emptyCell = new PdfPCell();
                    emptyCell.setBorder(Rectangle.NO_BORDER);
                    cardsGrid.addCell(emptyCell);
                }

                document.add(cardsGrid);
            }

            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addInfoRow(PdfPCell container, String label, String value, Font labelFont, Font valFont) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", labelFont));
        p.add(new Chunk(value != null && !value.isEmpty() ? value : "N/A", valFont));
        p.setSpacingAfter(3.5f);
        container.addElement(p);
    }

    private File resolveFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) return null;
        File direct = new File(filePath);
        if (direct.exists()) return direct;

        File inUpload = new File(uploadDir, filePath);
        if (inUpload.exists()) return inUpload;

        String stripped = filePath.replaceFirst("^/?uploads/", "");
        File inStripped = new File(uploadDir, stripped);
        if (inStripped.exists()) return inStripped;

        File rootStripped = new File(stripped);
        if (rootStripped.exists()) return rootStripped;

        return null;
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
