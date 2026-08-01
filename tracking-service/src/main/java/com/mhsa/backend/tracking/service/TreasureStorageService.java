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
 * Stores tracking-service media (treasure, diary attachments, ...) in object storage (MinIO in
 * dev, S3 in prod), mirroring auth-service's S3StorageService. Persists only the object key;
 * callers generate a short-lived presigned GET URL for responses.
 *
 * <p>Despite the name (kept to avoid churn from its original treasure-only scope), this is the
 * shared upload/presign/delete service for every media kind in tracking-service — see
 * {@link #uploadObject} which {@link #uploadTreasureMedia} and the diary attachment flow both
 * delegate to.
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

    // Public-facing endpoint used ONLY for presigned GET URLs handed to clients (the mobile
    // app). Defaults to the internal s3.endpoint for local dev; in deployed envs it points at
    // the gateway (e.g. http://<host>:8080) which proxies /mhsa-media/ to MinIO, because the
    // internal "minio:9000" host is unreachable from devices. Uploads/deletes keep using the
    // S3Client's internal endpoint (configured in S3Config).
    @Value("${s3.public-endpoint:${s3.endpoint}}")
    private String publicEndpoint;

    @Value("${s3.path-style-access:true}")
    private boolean pathStyleAccess;

    /**
     * Uploads a single treasure media item and returns its object key.
     */
    public String uploadTreasureMedia(String profileId, InputStream fileContent, String filename, String contentType) {
        return uploadObject(String.format("treasures/%s", profileId), fileContent, filename, contentType);
    }

    /**
     * Uploads an object under the given key prefix (e.g. "diary/{profileId}") and returns the
     * generated object key. Shared by every media kind in tracking-service.
     */
    public String uploadObject(String keyPrefix, InputStream fileContent, String filename, String contentType) {
        String objectKey = String.format("%s/%s.%s",
                keyPrefix,
                UUID.randomUUID(),
                getFileExtension(filename));

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(fileContent, getContentLength(fileContent)));
            log.info("Uploaded object: s3://{}/{}", bucket, objectKey);
            return objectKey;
        } catch (Exception e) {
            log.error("Failed to upload object to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload object", e);
        }
    }

    public String generatePresignedUrl(String objectKey, long durationSeconds) {
        S3Presigner.Builder presignerBuilder = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(s3Client.serviceClientConfiguration().credentialsProvider());

        // Sign for the public-facing endpoint so the URL is reachable from devices, not the
        // internal "minio:9000". Path-style keeps the bucket in the path (/mhsa-media/...),
        // matching the nginx /mhsa-media/ proxy location.
        if (pathStyleAccess) {
            presignerBuilder.serviceConfiguration(S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build());
        }
        if (!publicEndpoint.contains("amazonaws.com")) {
            presignerBuilder.endpointOverride(URI.create(publicEndpoint));
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
