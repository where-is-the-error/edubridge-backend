package com.edubridge.edubridge.dto;

import lombok.Data;

@Data
public class UserUpdateDto {
    // ⭐️ [추가] 닉네임 필드 추가
    private String nickname;

    private String gradeLevel;
    private Integer gradeNumber;
    private String track;
    private String subjectPrimary;
    private String subjectDetail;
}