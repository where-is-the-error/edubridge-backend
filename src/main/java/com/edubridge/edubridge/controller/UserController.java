// src/main/java/com/edubridge/edubridge/controller/UserController.java

package com.edubridge.edubridge.controller;

import com.edubridge.edubridge.dto.UserUpdateDto;
import com.edubridge.edubridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
// 프론트엔드 출처 허용
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    /**
     * 사용자 정보 업데이트 API (PUT /api/user/info)
     * - Requires Authentication (인증 필요)
     */
    @PutMapping("/info")
    public ResponseEntity<Void> updateUserInfo(@RequestBody UserUpdateDto updateDto,
                                               Authentication authentication) {

        // 1. JWT 토큰에서 사용자 ID(Subject) 추출 (Spring Security가 자동으로 처리)
        // Spring Security는 인증된 사용자 정보를 Authentication 객체에 담고,
        // .getName()은 JWT의 Subject(사용자 ID)를 반환합니다.
        String userId = authentication != null ? authentication.getName() : null;

        // 🌟 디버깅 로그 추가: 사용자 ID 확인
        System.out.println("--- Update Attempt User ID: " + (userId != null ? userId : "Anonymous/NULL") + " ---");

        // 2. 인증되지 않은 사용자인 경우 (userId가 null인 경우)
        if (userId == null || userId.isEmpty()) {
            System.err.println("인증되지 않은 사용자 접근 (401 Unauthorized 반환)");
            // JWT 검증 필터가 없으면, 이곳에서 401을 명시적으로 반환합니다.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // 3. 서비스 로직 호출
            userService.updateUserInfo(userId, updateDto);

            // 200 OK 반환
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            // 사용자 DB 조회 실패 등 런타임 예외 처리
            System.err.println("DB 업데이트 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build(); // 400 Bad Request
        }
    }
}