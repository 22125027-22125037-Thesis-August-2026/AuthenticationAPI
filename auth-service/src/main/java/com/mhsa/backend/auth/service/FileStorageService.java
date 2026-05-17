package com.mhsa.backend.auth.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png");
    private static final long MAX_SIZE = 5L * 1024 * 1024; // 5MB
    private static final long PRESIGNED_URL_VALIDITY_SECONDS = 24 * 3600; // 24 hours for avatars

    private final S3StorageService s3StorageService;

    @Value("${s3.bucket}")
    private String bucket;

    public String storeAvatar(MultipartFile file, UUID profileId) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPEG and PNG images are accepted");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File size must not exceed 5MB");
        }

        try {
            String objectKey = s3StorageService.uploadAvatar(
                    profileId.toString(),
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    contentType);

            return s3StorageService.generatePresignedUrl(objectKey, PRESIGNED_URL_VALIDITY_SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store avatar file", e);
        }
    }

    public String getPresignedAvatarUrl(String objectKey) {
        return s3StorageService.generatePresignedUrl(objectKey, PRESIGNED_URL_VALIDITY_SECONDS);
    }

    public void deleteAvatar(String objectKey) {
        s3StorageService.deleteObject(objectKey);
    }
}
