// src/main/java/com/edubridge.edubridge.dto/UserUpdateDto.java (새 파일)

package com.edubridge.edubridge.dto;

import lombok.Data;

@Data
public class UserUpdateDto {
    // DB의 필드명에 맞춥니다. (User.java 모델 참조)
    private String gradeLevel;
    private Integer gradeNumber;
    private String track;
    private String subjectPrimary;
    private String subjectDetail;
}