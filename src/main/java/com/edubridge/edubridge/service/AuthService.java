// src/main/java/com/edubridge/edubridge/service/AuthService.java

package com.edubridge.edubridge.service;

import com.edubridge.edubridge.model.User;
import com.edubridge.edubridge.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // application.properties에서 JWT Secret Key를 주입받음
    @Value("${jwt.secret}")
    private String jwtSecret;

    // 1. 사용자 회원가입
    public User registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        return userRepository.save(user);
    }

    // 2. 사용자 로그인 및 JWT 발급
    public String authenticate(String email, String password) {
        // 1. 이메일로 사용자 찾기 (없으면 예외 발생 -> 401)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 2. 비밀번호 일치 확인 (BCrypt 비교)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 3. JWT 토큰 생성
        return generateToken(user.getId(), user.getRole());
    }

    // JWT 토큰 생성 메소드
    private String generateToken(String userId, String role) {

        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("role", role);

        Date now = new Date();
        Date validity = new Date(now.getTime() + 3600000); // 1시간 유효기간

        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(Keys.hmacShaKeyFor(secretBytes), SignatureAlgorithm.HS256)
                .compact();
    }
}