// src/main/java/com.edubridge.edubridge.service/UserService.java

package com.edubridge.edubridge.service;

import com.edubridge.edubridge.dto.UserUpdateDto;
import com.edubridge.edubridge.model.User;
import com.edubridge.edubridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 사용자 정보를 업데이트합니다.
     * @param userId - JWT에서 추출된 현재 로그인 사용자의 ID
     * @param updateDto - 프론트에서 받은 업데이트 데이터
     */
    public void updateUserInfo(String userId, UserUpdateDto updateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 🌟 업데이트 로직: DTO에서 값이 있는 경우에만 User 모델 필드를 업데이트합니다.
        // ⭐️ [추가] 닉네임 업데이트 로직
        if (updateDto.getNickname() != null && !updateDto.getNickname().isEmpty()) {
            user.setNickname(updateDto.getNickname());
        }

        // 1. 학제/학년 정보 업데이트
        if (updateDto.getGradeLevel() != null) {
            user.setGradeLevel(updateDto.getGradeLevel());
        }
        if (updateDto.getGradeNumber() != null) {
            user.setGradeNumber(updateDto.getGradeNumber());
        }

        // 2. 계열 정보 업데이트 (중/고등학생 관련)
        if (updateDto.getTrack() != null) {
            user.setTrack(updateDto.getTrack());
        }

        // 3. 과목 정보 업데이트
        if (updateDto.getSubjectPrimary() != null) {
            user.setSubjectPrimary(updateDto.getSubjectPrimary());
        }
        if (updateDto.getSubjectDetail() != null) {
            user.setSubjectDetail(updateDto.getSubjectDetail());
        }

        // 데이터베이스에 변경 사항 저장
        userRepository.save(user);
    }
}