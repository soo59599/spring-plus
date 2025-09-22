package org.example.expert.domain.user.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

    // 허용할 이미지 파일의 Content Type 목록
    private static final List<String> ALLOWED_IMAGE_CONTENT_TYPES =
            List.of("image/jpeg", "image/png");

    // 허용할 이미지 파일의 크기
    private static final long maxSize = 5 * 1024 * 1024;

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null; // 파일이 비어있으면 null 반환
        }

        validateFile(file);

        String uniqueFileName = createUniqueFileName(file.getOriginalFilename());
        String objectKey = "profiles/" + uniqueFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // S3 버킷의 퍼블릭 URL 형식에 맞게 직접 조합
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, "ap-northeast-2", objectKey);
    }

    private void validateFile(MultipartFile file) {
        // 파일의 Content Type이 허용된 목록에 있는지 확인
        if (!ALLOWED_IMAGE_CONTENT_TYPES.contains(file.getContentType())) {
            // 허용되지 않은 파일 형식일 경우 예외 발생
            throw new IllegalArgumentException("지원하지 않는 이미지 파일 형식입니다: " + file.getContentType());
        }

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다.");
        }
    }

    private String createUniqueFileName(String fileName) {
        return UUID.randomUUID().toString() + "-" + fileName;
    }
}