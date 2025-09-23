package org.example.expert.domain.profile.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.profile.service.ProfileService;
import org.example.expert.domain.profile.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class S3ProfileController {

    private final S3Service s3Service;
    private final ProfileService profileService;

    @GetMapping("/presigned-url")
    public ResponseEntity<String> getPresignedUrl(@RequestParam("filename") String filename) {
        String presignedUrl = s3Service.getPresignedUrl(filename);
        return ResponseEntity.ok(presignedUrl);
    }

    @PostMapping("/upload-complete")
    public ResponseEntity<Void> uploadComplete(
            @RequestParam("objectKey") String objectKey,
            @AuthenticationPrincipal AuthUser authUser) {

        profileService.updateProfileImage(authUser.getId(), objectKey);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/image")
    public ResponseEntity<Void> deleteProfileImage(@AuthenticationPrincipal AuthUser authUser) {
        profileService.deleteProfileImage(authUser.getId());
        return ResponseEntity.ok().build();
    }
}