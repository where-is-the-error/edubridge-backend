package com.edubridge.edubridge.controller;

import com.edubridge.edubridge.dto.UserUpdateDto;
import com.edubridge.edubridge.model.User;
import com.edubridge.edubridge.service.UserService;
import com.edubridge.edubridge.repository.UserRepository; // Repository 추가
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository; // 조회용

    /**
     * 내 정보 조회 API (GET /api/user/info)
     * - 로컬 스토리지 대신 DB에서 최신 정보를 가져올 때 사용
     */
    @GetMapping("/info")
    public ResponseEntity<User> getUserInfo(Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : null;

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userRepository.findById(userId)
                .map(user -> {
                    user.setPassword(null); // 보안상 비밀번호 제거
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 사용자 정보 업데이트 API (PUT /api/user/info)
     */
    @PutMapping("/info")
    public ResponseEntity<Void> updateUserInfo(@RequestBody UserUpdateDto updateDto,
                                               Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : null;
        System.out.println("--- Update Attempt User ID: " + (userId != null ? userId : "Anonymous/NULL") + " ---");

        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            userService.updateUserInfo(userId, updateDto);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            System.err.println("DB 업데이트 실패: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}