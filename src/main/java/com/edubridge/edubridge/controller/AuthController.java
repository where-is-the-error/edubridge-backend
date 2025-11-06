// src/main/java/com/edubridge/edubridge/controller/AuthController.java

package com.edubridge.edubridge.controller;

import com.edubridge.edubridge.model.User;
import com.edubridge.edubridge.service.AuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 로그인 요청/응답 DTO (Controller 내부에 정의)
@Data
class AuthRequest {
    private String email;
    private String password;
}

@Data
class AuthResponse {
    private String token;
}

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register 요청 처리 (회원가입)
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        try {
            User registeredUser = authService.registerUser(user);
            registeredUser.setPassword(null); // 보안상 PW null 처리
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // POST /api/auth/signin 요청 처리 (로그인)
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            // Service 계층에서 인증 및 토큰 생성
            String token = authService.authenticate(request.getEmail(), request.getPassword());

            AuthResponse response = new AuthResponse();
            response.setToken(token);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // 인증 실패 시 401 Unauthorized 반환 (이메일 없음, 비밀번호 불일치 등)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}