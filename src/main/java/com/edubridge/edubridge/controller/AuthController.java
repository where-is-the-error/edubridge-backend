// src/main/java/com/edubridge/edubridge/controller/AuthController.java

package com.edubridge.edubridge.controller;

import com.edubridge.edubridge.model.User;
import com.edubridge.edubridge.service.AuthService;
import com.edubridge.edubridge.repository.UserRepository; // User 조회용
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 로그인 요청 DTO
@Data
class AuthRequest {
    private String email;
    private String password;
}

// 🌟 로그인 응답 DTO (사용자 정보 필드 추가)
@Data
class AuthResponse {
    private String token;
    private String nickname;
    private String gradeLevel;      // 예: elementary
    private Integer gradeNumber;    // 예: 3
    private String subjectPrimary;  // 예: math
    private String subjectDetail;   // 세부 과목
    private String track;           // 문/이과
}

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // 프론트엔드 주소 허용
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository; // 👈 추가: 사용자 정보 조회를 위해 주입

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        try {
            User registeredUser = authService.registerUser(user);
            registeredUser.setPassword(null); // 보안상 비밀번호 제거 후 반환
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 🌟 로그인 (수정됨)
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        System.out.println("--- Login Attempt: " + request.getEmail() + " ---");
        try {
            // 1. 인증 및 토큰 생성 (AuthService 위임)
            String token = authService.authenticate(request.getEmail(), request.getPassword());

            // 2. 사용자 상세 정보 조회 (DB에서 가져오기)
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 3. 응답 객체 생성 및 데이터 세팅
            AuthResponse response = new AuthResponse();
            response.setToken(token);
            response.setNickname(user.getNickname());
            response.setGradeLevel(user.getGradeLevel());
            response.setGradeNumber(user.getGradeNumber());
            response.setSubjectPrimary(user.getSubjectPrimary());
            response.setSubjectDetail(user.getSubjectDetail());
            response.setTrack(user.getTrack());

            // 4. 응답 반환
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}