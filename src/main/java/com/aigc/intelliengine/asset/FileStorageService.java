package com.aigc.intelliengine.asset;

import com.aigc.intelliengine.common.config.MinioConfig;
import com.aigc.intelliengine.common.exception.BusinessException;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    public String uploadFile(MultipartFile file, String assetCode) {
        String bucket = minioConfig.getBucket();
        ensureBucketExists(bucket);

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String objectName = assetCode + "/" + UUID.randomUUID() + extension;

        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(is, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            log.info("File uploaded to MinIO: bucket={}, object={}", bucket, objectName);
            return objectName;
        } catch (Exception e) {
            log.error("MinIO upload failed", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    public String getPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(3600)
                    .build());
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for {}", objectName, e);
            return null;
        }
    }

    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to delete MinIO object: {}", objectName, e);
        }
    }

    private void ensureBucketExists(String bucket) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("MinIO bucket created: {}", bucket);
            }
        } catch (Exception e) {
            log.error("MinIO bucket check failed", e);
            throw new BusinessException("存储服务异常");
        }
    }
}
