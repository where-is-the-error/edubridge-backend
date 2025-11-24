// src/main/java/com/edubridge/edubridge/filter/JwtAuthenticationFilter.java

package com.edubridge.edubridge.filter;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value; // 💡 @Value import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// 💡 AuthService 대신 필요한 값들을 직접 주입받도록 수정
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret; // 💡 AuthService 대신 Secret Key 직접 주입

    // 💡 생성자 주입을 위한 RequiredArgsConstructor 제거

    // 💡 생성자 추가 (Spring이 @Value를 먼저 처리할 수 있도록)
    // Spring이 @Value를 필드에 주입한 후 객체를 생성합니다.

    // 이 필터는 다른 Service를 주입받지 않으므로 생성자 코드를 제거합니다.
    // 만약 UserDetailsService가 필요하다면 여기에 추가해야 합니다.

    // 토큰에서 사용자 ID 추출 로직 (AuthService에서 가져옴)
    private String extractUserId(String token) {
        try {
            byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretBytes))
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            System.err.println("JWT 토큰 유효성 검사 실패 (추출 오류): " + e.getMessage());
            return null;
        }
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String jwt = null;
        String userId = null;

        // 1. JWT 토큰 추출
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            userId = extractUserId(jwt); // 💡 수정된 추출 메서드 사용
        }

        // 2. JWT가 유효하고, SecurityContext에 인증 정보가 없을 경우
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 3. 인증 객체 생성
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userId, null, null
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 4. Security Context에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}