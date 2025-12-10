package com.edubridge.edubridge.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;
    private String nickname;

    // 학년 정보
    private String gradeLevel;   // elementary, middle, high
    private Integer gradeNumber; // 1, 2, 3
    private String track;        // 문과/이과
    private String subjectPrimary;
    private String subjectDetail;

    // ⭐️ [수정됨] 사용자 학습 수준 (1 ~ 10 단계)
    // 1~3: 기초, 4~7: 보통, 8~10: 심화
    private Integer level = 1;

    // ⭐️ [수정됨] AI 분석 리포트 (초기값은 공백)
    private String aiAnalysis = "";

    private String role = "student";
    private String characterName = "코니";
    private int progressPoints = 0;
    private LocalDateTime createdAt = LocalDateTime.now();
}