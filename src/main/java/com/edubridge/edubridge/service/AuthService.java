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
// 💡 Spring Security의 PasswordEncoder를 import 합니다.
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    // 💡 PasswordEncoder final 필드를 다시 추가하고 주입받습니다.
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * 1. 사용자 회원가입 (비밀번호 해싱 처리)
     */
    public User registerUser(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        // 🌟 보안 수정: 비밀번호를 저장하기 전에 반드시 BCrypt로 해싱합니다.
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        return userRepository.save(user);
    }

    /**
     * 2. 사용자 로그인 및 JWT 발급
     */
    public String authenticate(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다1."));

        // 🌟 보안 수정: 사용자가 입력한 평문 비밀번호와 DB의 해시된 비밀번호를 비교합니다.
        // passwordEncoder.matches(평문, 해시)를 사용해야 합니다.
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다2.");
        }

        // 3. JWT 토큰 생성
        return generateToken(user.getId(), user.getRole());
    }

    /**
     * JWT 토큰 생성 메소드 (변경 없음)
     */
    private String generateToken(String userId, String role) {

        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("role", role);

        Date now = new Date();
        Date validity = new Date(now.getTime() + 3600000);

        // 🌟 수정 필요: Base64로 인코딩된 문자열을 사용하여 안전하게 256비트를 맞춥니다.
        // 하지만 현재는 StandardCharsets.UTF_8을 사용하므로,
        // 키가 충분히 길다면 Base64 변환 없이도 작동해야 합니다.

        // 🚨 키가 짧을 때 오류를 내지 않도록 Keys.hmacShaKeyFor를 사용하고 있으므로,
        // application.properties의 키 길이를 32자 이상으로 늘리는 것만으로 해결됩니다.

        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(Keys.hmacShaKeyFor(secretBytes), SignatureAlgorithm.HS256)
                .compact();
    }
    public String extractUserId(String token) {
        try {
            byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretBytes))
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject(); // Subject(사용자 ID) 추출
        } catch (Exception e) {
            // 토큰 파싱 또는 유효성 검사 실패 시
            return null;
        }
    }
}