package org.example.expert.domain.profile.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Transactional
    public void updateProfileImage(Long userId, String objectKey) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("사용자를 찾을 수 없습니다."));

        // S3에 파일이 실제로 업로드되었는지 확인
        if (!s3Service.fileExists(objectKey)) {
            throw new InvalidRequestException("파일 업로드가 완료되지 않았습니다.");
        }

        // 기존 프로필 이미지 삭제
        deleteOldProfileImage(user.getProfileImageUrl());

        // 새로운 프로필 이미지 URL 설정
        String newImageUrl = generateImageUrl(objectKey);
        user.updateProfileImage(newImageUrl);
    }

    @Transactional
    public void deleteProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("사용자를 찾을 수 없습니다."));

        deleteOldProfileImage(user.getProfileImageUrl());
        user.updateProfileImage(null);
    }

    private void deleteOldProfileImage(String oldImageUrl) {
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            String oldObjectKey = s3Service.extractObjectKeyFromUrl(oldImageUrl);
            if (oldObjectKey != null) {
                s3Service.deleteFile(oldObjectKey);
            }
        }
    }

    private String generateImageUrl(String objectKey) {
        return String.format("https://%s.s3.amazonaws.com/%s",
                bucket, objectKey);
    }
}