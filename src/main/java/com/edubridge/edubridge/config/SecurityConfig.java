// src/main/java/com/edubridge/edubridge/config/SecurityConfig.java

package com.edubridge.edubridge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. 비밀번호 암호화 도구 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. API 접근 권한 설정 (최종 해결 로직)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF, HTTP Basic, FormLogin 비활성화 (401, 403 오류 유발 요소 제거)
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // 2. 세션 관리를 Stateless(상태 없음)로 설정 (JWT 필수)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3. 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 회원가입 및 로그인 경로는 인증 없이 접근 허용 (401 오류 해결)
                        .requestMatchers("/api/auth/**").permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}