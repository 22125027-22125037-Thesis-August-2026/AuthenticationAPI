package com.mhsa.backend.tracking.service;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import lombok.RequiredArgsConstructor;

/**
 * Stores treasure media (image/audio/video) in object storage (MinIO in dev, S3 in prod),
 * mirroring auth-service's S3StorageService. Persists only the object key; callers generate
 * a short-lived presigned GET URL for responses.
 *
 * <p>Unlike auth-service, the presigner here is configured with the same endpoint/path-style
 * as the S3 client, so presigned URLs point at MinIO in dev instead of the public AWS host.
 */
@Service
@RequiredArgsConstructor
public class TreasureStorageService {

    private static final Logger log = LoggerFactory.getLogger(TreasureStorageService.class);

    private final S3Client s3Client;

    @Value("${s3.bucket}")
    private String bucket;

    @Value("${s3.region}")
    private String region;

    @Value("${s3.endpoint}")
    private String endpoint;

    @Value("${s3.path-style-access:true}")
    private boolean pathStyleAccess;

    /**
     * Uploads a single treasure media item and returns its object key.
     */
    public String uploadTreasureMedia(String profileId, InputStream fileContent, String filename, String contentType) {
        String objectKey = String.format("treasures/%s/%s.%s",
                profileId,
                UUID.randomUUID(),
                getFileExtension(filename));

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(fileContent, getContentLength(fileContent)));
            log.info("Uploaded treasure media: s3://{}/{}", bucket, objectKey);
            return objectKey;
        } catch (Exception e) {
            log.error("Failed to upload treasure media to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload treasure media", e);
        }
    }

    public String generatePresignedUrl(String objectKey, long durationSeconds) {
        S3Presigner.Builder presignerBuilder = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(s3Client.serviceClientConfiguration().credentialsProvider());

        // Match the S3 client's addressing so dev URLs resolve to MinIO, not the public AWS host.
        if (pathStyleAccess) {
            presignerBuilder.serviceConfiguration(S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build());
        }
        if (!endpoint.contains("amazonaws.com")) {
            presignerBuilder.endpointOverride(URI.create(endpoint));
        }

        try (S3Presigner presigner = presignerBuilder.build()) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(durationSeconds))
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
            log.info("Deleted treasure media: s3://{}/{}", bucket, objectKey);
        } catch (Exception e) {
            log.error("Failed to delete object {}: {}", objectKey, e.getMessage(), e);
            throw new RuntimeException("Failed to delete object", e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "bin";
        }
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
