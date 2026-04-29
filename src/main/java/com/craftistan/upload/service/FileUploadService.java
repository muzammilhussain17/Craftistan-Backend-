package com.craftistan.upload.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FileUploadService {

    private final Cloudinary cloudinary;
    private final String folder;

    @Value("${app.upload.max-size:5242880}")
    private long maxSize;

    @Value("${app.upload.allowed-types:image/jpeg,image/png,image/webp}")
    private String allowedTypes;

    public FileUploadService(
            @Value("${app.cloudinary.cloud-name}") String cloudName,
            @Value("${app.cloudinary.api-key}")    String apiKey,
            @Value("${app.cloudinary.api-secret}") String apiSecret,
            @Value("${app.cloudinary.folder:craftistan}") String folder) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key",    apiKey,
                "api_secret", apiSecret,
                "secure",     true
        ));
        this.folder = folder;
    }

    /**
     * Upload a single file to Cloudinary.
     *
     * @param file the multipart file to upload
     * @return the public HTTPS URL of the uploaded image
     */
    public String uploadFile(MultipartFile file) throws IOException {
        validateFile(file);

        @SuppressWarnings("rawtypes")
        Map result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder",        folder,
                        "resource_type", "image",
                        "overwrite",     false
                )
        );

        String secureUrl = (String) result.get("secure_url");
        log.info("File uploaded to Cloudinary: {}", secureUrl);
        return secureUrl;
    }

    /**
     * Upload multiple files to Cloudinary (max 5).
     *
     * @param files list of multipart files
     * @return list of public HTTPS URLs
     */
    public List<String> uploadFiles(List<MultipartFile> files) throws IOException {
        if (files.size() > 5) {
            throw new IllegalArgumentException("Maximum 5 files allowed per request");
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadFile(file));
        }
        return urls;
    }

    /**
     * Delete a file from Cloudinary using its public ID extracted from the secure URL.
     * Safe to call even if the URL is null or not a Cloudinary URL.
     *
     * @param fileUrl the full Cloudinary secure_url
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        try {
            String publicId = extractPublicId(fileUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("File deleted from Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.error("Error deleting file from Cloudinary: {}", fileUrl, e);
        }
    }

    // ─── Private Helpers ────────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(
                    "File size exceeds the maximum limit of 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new IllegalArgumentException(
                    "File type not allowed. Accepted types: JPEG, PNG, WebP");
        }
    }

    /**
     * Extract Cloudinary public_id from a secure_url.
     * Example URL:
     *   https://res.cloudinary.com/demo/image/upload/v1234/craftistan/abc123.jpg
     * → public_id: craftistan/abc123
     */
    private String extractPublicId(String secureUrl) {
        // Remove everything up to "/upload/"
        int uploadIndex = secureUrl.indexOf("/upload/");
        if (uploadIndex == -1) {
            return secureUrl; // not a Cloudinary URL — return as-is, destroy will fail silently
        }
        String afterUpload = secureUrl.substring(uploadIndex + 8); // skip "/upload/"

        // Strip optional version segment  v1234567890/
        if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
            int slashPos = afterUpload.indexOf('/');
            String versionToken = afterUpload.substring(1, slashPos);
            if (versionToken.chars().allMatch(Character::isDigit)) {
                afterUpload = afterUpload.substring(slashPos + 1);
            }
        }

        // Strip file extension
        int dotIndex = afterUpload.lastIndexOf('.');
        return dotIndex != -1 ? afterUpload.substring(0, dotIndex) : afterUpload;
    }
}
