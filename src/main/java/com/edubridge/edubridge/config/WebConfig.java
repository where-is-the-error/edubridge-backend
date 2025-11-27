// src/main/java/com/edubridge.edubridge.config/WebConfig.java

package com.edubridge.edubridge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

// src/main/java/com/edubridge/edubridge/config/WebConfig.java

@Configuration
public class WebConfig {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                // 1. 로그인/회원가입 경로 제외
                .requestMatchers("/api/auth/**")

                // 2. ⭐️ 크롤링 관련 경로 추가 (이 줄을 추가하세요!) ⭐️
                .requestMatchers("/api/crawl/**");
    }
}