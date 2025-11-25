// src/main/java/com.edubridge.edubridge.model/User.java

package com.edubridge.edubridge.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data // Getter, Setter, toString 등을 자동 생성
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 포함하는 생성자 (테스트 및 DTO 변환 시 유용)
@Document(collection = "users")
public class User {

    @Id // MongoDB의 ObjectId에 매핑
    private String id;

    @Indexed(unique = true) // 이메일은 중복 불가능
    private String email;

    private String password;
    private String nickname;

    // ============== 사용자 선택 정보 필드 ==============

    // 연령 그룹 (예: "elementary", "middle", "high")
    private String gradeLevel;

    // 학년 번호 (예: 1, 2, 3) - Integer 타입이므로 null 저장 가능
    private Integer gradeNumber;

    // 계열 (예: "society", "science")
    private String track;

    // 주 선택 과목 (예: "math", "korea")
    private String subjectPrimary;

    // 세부 과목 (고1 과학 등 세분화된 선택 시 사용)
    private String subjectDetail;

    // ===============================================

    // ... (기존 시스템 필드 유지)
    private String role = "student";
    private String characterName = "코니";
    private int progressPoints = 0;
    private LocalDateTime createdAt = LocalDateTime.now();

    // 참고: @NoArgsConstructor와 @AllArgsConstructor를 추가하여 Lombok 기능을 확장했습니다.
}