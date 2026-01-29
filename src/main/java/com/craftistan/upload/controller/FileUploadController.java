package com.craftistan.upload.controller;

import com.craftistan.common.dto.ApiResponse;
import com.craftistan.upload.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "File Upload", description = "File upload endpoints")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/image")
    @Operation(summary = "Upload single image")
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @RequestParam("file") MultipartFile file) throws IOException {
        String url = fileUploadService.uploadFile(file);
        return ResponseEntity.ok(ApiResponse.success(url, "Image uploaded successfully"));
    }

    @PostMapping("/images")
    @Operation(summary = "Upload multiple images (max 5)")
    public ResponseEntity<ApiResponse<List<String>>> uploadImages(
            @RequestParam("files") List<MultipartFile> files) throws IOException {
        List<String> urls = fileUploadService.uploadFiles(files);
        return ResponseEntity.ok(ApiResponse.success(urls, "Images uploaded successfully"));
    }
}
