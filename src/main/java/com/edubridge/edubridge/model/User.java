package com.edubridge.edubridge.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;
    private String nickname;

    // 🌟 1. 기존 필드 유지: 연령 그룹 (elementary, middle, high)
    private String gradeLevel;

    // 🌟 2. 추가된 구조화된 필드
    private Integer gradeNumber;    // 학년 번호 (예: 1, 2, 3)
    private String track;           // 계열 (예: "society", "science")
    private String subjectPrimary;  // 주 선택 과목 (예: "math", "korea")
    private String subjectDetail;   // 세부 과목 (예: "integrated")

    // ... (기존 필드 유지)
    private String role = "student";
    private String characterName = "코니";
    private int progressPoints = 0;
    private LocalDateTime createdAt = LocalDateTime.now();
}