package com.mhsa.backend.auth.service;

import java.io.InputStream;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class S3StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client s3Client;

    @Value("${s3.bucket}")
    private String bucket;

    @Value("${s3.region}")
    private String region;

    public String uploadAvatar(String profileId, InputStream fileContent, String filename, String contentType) {
        String objectKey = String.format("avatars/%s/%s.%s",
                profileId,
                UUID.randomUUID(),
                getFileExtension(filename));

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putRequest, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(fileContent, getContentLength(fileContent)));
            log.info("Uploaded avatar: s3://{}/{}", bucket, objectKey);
            return objectKey;
        } catch (Exception e) {
            log.error("Failed to upload avatar to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload avatar", e);
        }
    }

    public String generatePresignedUrl(String objectKey, long durationSeconds) {
        try (S3Presigner presigner = S3Presigner.builder()
                .region(software.amazon.awssdk.regions.Region.of(region))
                .credentialsProvider(s3Client.serviceClientConfiguration().credentialsProvider())
                .build()) {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(java.time.Duration.ofSeconds(durationSeconds))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for {}: {}", objectKey, e.getMessage(), e);
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    public void deleteObject(String objectKey) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Deleted object: s3://{}/{}", bucket, objectKey);
        } catch (Exception e) {
            log.error("Failed to delete object {}: {}", objectKey, e.getMessage(), e);
            throw new RuntimeException("Failed to delete object", e);
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "bin";
    }

    private long getContentLength(InputStream fileContent) {
        try {
            return fileContent.available();
        } catch (Exception e) {
            return -1;
        }
    }
}
