// src/main/java/com.edubridge.edubridge.config/SecurityConfig.java

package com.edubridge.edubridge.config;

import com.edubridge.edubridge.filter.JwtAuthenticationFilter; // 💡 구현할 JWT 필터 import
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // 💡 필터 위치 지정용 import
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // JwtAuthenticationFilter 주입을 위해 사용
public class SecurityConfig {

    // 💡 JWT 필터를 주입받습니다.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 1. PasswordEncoder Bean 등록 (BCrypt 사용)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 2. SecurityFilterChain 설정 (인증 및 접근 제어)
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 🚨 CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 🚨 CSRF 보호 비활성화 (JWT를 사용하므로)
                .csrf(AbstractHttpConfigurer::disable)

                // 🚨 폼 로그인, HTTP Basic 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 💡 세션 사용 안함 설정 (JWT는 Stateless)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 💡 요청별 접근 권한 설정
                .authorizeHttpRequests(authz -> authz
                        // 회원가입 및 로그인 경로는 인증 없이 접근 허용
                        .requestMatchers("/api/auth/register", "/api/auth/signin").permitAll()
                        // 나머지 모든 요청은 인증되어야 접근 가능
                        .anyRequest().authenticated()
                )

                // 🌟🌟🌟 3. 커스텀 JWT 필터 등록 🌟🌟🌟
                // UsernamePasswordAuthenticationFilter 이전에 JWT 필터를 실행하도록 합니다.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 4. CORS Configuration Source Bean 정의
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 프론트엔드 출처(Origin) 명시적으로 허용
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));

        // 허용할 HTTP 메서드 (PUT/PATCH 포함)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 모든 헤더 허용 (Authorization 헤더 포함)
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 쿠키와 인증 정보 전송 허용
        configuration.setAllowCredentials(true);

        // 모든 경로에 CORS 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}