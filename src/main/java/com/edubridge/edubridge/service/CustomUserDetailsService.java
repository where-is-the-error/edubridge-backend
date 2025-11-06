// src/main/java/com/edubridge/edubridge/service/CustomUserDetailsService.java

package com.edubridge.edubridge.service;

import com.edubridge.edubridge.model.User;
import com.edubridge.edubridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Spring Security가 로그인 시 사용자 정보를 찾을 때 호출하는 메소드
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // DB에서 이메일을 이용해 사용자 정보를 가져옵니다.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        // UserDetails 객체를 생성하여 Spring Security에 반환합니다.
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(), // 해시된 비밀번호
                Collections.emptyList() // 권한 정보 (일단 비워둡니다)
        );
    }
}