package com.edubridge.edubridge.controller;

import com.edubridge.edubridge.model.SchoolInfo;
import com.edubridge.edubridge.service.SchoolInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // ⭐️ Import 변경
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/school-info")
@RequiredArgsConstructor
public class SchoolInfoController {

    private final SchoolInfoService schoolInfoService;

    @PostMapping
    public ResponseEntity<SchoolInfo> saveInfo(
            Authentication authentication, // ⭐️ UserDetails 대신 Authentication 사용
            @RequestBody SchoolInfo schoolInfo) {

        // 인증 정보가 없으면 401 리턴
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        String userId = authentication.getName(); // 토큰에서 userId 추출

        // 서비스 호출 (userId 전달)
        SchoolInfo saved = schoolInfoService.saveSchoolInfo(userId, schoolInfo);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<SchoolInfo> getInfo(Authentication authentication) { // ⭐️ Authentication 사용

        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        String userId = authentication.getName(); // 토큰에서 userId 추출

        // 서비스 호출 (userId 전달)
        SchoolInfo info = schoolInfoService.getSchoolInfoByUserId(userId);

        if (info == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(info);
    }
}