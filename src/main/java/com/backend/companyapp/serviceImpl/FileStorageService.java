package com.backend.companyapp.serviceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String baseUrl;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp", "gif", "pdf");
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/pjpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "application/pdf",
            "application/x-pdf"
    );

    public String storeFile(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type: " + contentType + ". Allowed types: JPEG, PNG, WEBP, GIF, PDF");
        }

        // Validate File Extension
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file extension. Allowed extensions: jpg, jpeg, png, webp, gif, pdf");
        }

        // Base Upload Path normalization & Traversal protection
        Path baseUploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        String cleanFolder = (folder == null) ? "" : folder.trim();
        Path targetDirPath = baseUploadPath.resolve(cleanFolder).normalize();

        if (!targetDirPath.startsWith(baseUploadPath)) {
            throw new IllegalArgumentException("Invalid directory path traversal attempt");
        }

        if (!Files.exists(targetDirPath)) {
            Files.createDirectories(targetDirPath);
        }

        // Generate safe unique filename using UUID and sanitized extension only (dropping user-supplied filename completely)
        String safeFileName = UUID.randomUUID().toString() + "." + extension.toLowerCase();
        Path targetFilePath = targetDirPath.resolve(safeFileName).normalize();

        if (!targetFilePath.startsWith(baseUploadPath)) {
            throw new IllegalArgumentException("Invalid file path traversal attempt");
        }

        Files.copy(file.getInputStream(), targetFilePath, StandardCopyOption.REPLACE_EXISTING);

        String normalizedUploadDir = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
        String folderPrefix = cleanFolder.isEmpty() ? "" : cleanFolder.replaceAll("^/+", "").replaceAll("/+$", "") + "/";
        
        return normalizedUploadDir + folderPrefix + safeFileName;
    }

    public void deleteFile(String logoUrl, String folder) {
        if (logoUrl == null || logoUrl.trim().isEmpty() || logoUrl.startsWith("http://") || logoUrl.startsWith("https://")) {
            return;
        }
        try {
            Path baseUploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path path;
            if (logoUrl.startsWith(uploadDir)) {
                path = baseUploadPath.resolve(logoUrl.substring(uploadDir.length())).normalize();
            } else {
                String safeFolder = (folder == null) ? "" : folder.trim();
                path = baseUploadPath.resolve(safeFolder).resolve(logoUrl).normalize();
            }

            if (path.startsWith(baseUploadPath)) {
                Files.deleteIfExists(path);
            }
        } catch (Exception e) {
            // Silently ignore if file doesn't exist
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return null;
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}

