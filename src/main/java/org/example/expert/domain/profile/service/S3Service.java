package org.example.expert.domain.profile.service;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;


@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public String getPresignedUrl(String fileName) {
        validateFileExtension(fileName);
        String uniqueFileName = createUniqueFileName(fileName);
        String objectKey = "profiles/" + uniqueFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return presignedRequest.url().toString();
    }

    public void deleteFile(String objectKey) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            // 로깅 후 무시
        }
    }

    public boolean fileExists(String objectKey) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            s3Client.headObject(headRequest);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public String extractObjectKeyFromUrl(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains("profiles/")) return null;

        int profileIndex = fileUrl.indexOf("profiles/");
        String objectKey = fileUrl.substring(profileIndex);

        int queryIndex = objectKey.indexOf("?");
        if (queryIndex != -1) {
            objectKey = objectKey.substring(0, queryIndex);
        }
        return objectKey;
    }

    private void validateFileExtension(String fileName) {
        String lowerCaseFileName = fileName.toLowerCase();
        if (!(lowerCaseFileName.endsWith(".jpg") ||
                lowerCaseFileName.endsWith(".png") ||
                lowerCaseFileName.endsWith(".jpeg"))) {
            throw new IllegalArgumentException("지원하지 않는 파일 확장자입니다.");
        }
    }

    private String createUniqueFileName(String fileName) {
        return UUID.randomUUID().toString() + "-" + fileName;
    }
}
