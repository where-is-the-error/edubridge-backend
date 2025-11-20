// src/main/java/com/edubridge/edubridge/service/AuthService.java (Security 참조 없는 버전)

package com.edubridge.edubridge.service;

import com.edubridge.edubridge.model.User;
import com.edubridge.edubridge.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
// import org.springframework.security.crypto.password.PasswordEncoder; <- 이 줄은 반드시 삭제

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    // private final PasswordEncoder passwordEncoder; <- 이 final 필드도 삭제/주석 처리

    @Value("${jwt.secret}")
    private String jwtSecret;

    // 1. 사용자 회원가입 (원본 비밀번호 저장 - 임시)
    public User registerUser(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 암호화 로직을 완전히 제거하여 원본 비밀번호 저장 (임시)
        // user.setPassword(user.getPassword()); // 사실상 변경 없음

        return userRepository.save(user);
    }

    // 2. 사용자 로그인 및 JWT 발급
    public String authenticate(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 비밀번호 비교 (임시: 원본 문자열 비교)
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 3. JWT 토큰 생성 (생략)
        return generateToken(user.getId(), user.getRole());
    }

    // JWT 토큰 생성 메소드 (변경 없음)
    private String generateToken(String userId, String role) {

        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("role", role);

        Date now = new Date();
        Date validity = new Date(now.getTime() + 3600000);

        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(Keys.hmacShaKeyFor(secretBytes), SignatureAlgorithm.HS256)
                .compact();
    }
}