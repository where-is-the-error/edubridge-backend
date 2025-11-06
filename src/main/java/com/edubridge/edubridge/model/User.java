// src/main/java/com/edubridge/edubridge/model/User.java

package com.edubridge.edubridge.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data // Lombok: Getter, Setter, toString 등을 자동 생성
@Document(collection = "users") // 이 클래스가 MongoDB의 'users' 컬렉션과 매핑됨을 의미
public class User {

    @Id // MongoDB의 ObjectId에 매핑
    private String id;

    @Indexed(unique = true) // 이메일은 중복 불가능
    private String email;

    private String password; // 암호화된 비밀번호
    private String nickname;
    private String gradeLevel; // grade_level (초1, 초2 등)
    private String role = "student"; // 기본값 설정
    private String characterName = "코니"; // 기본값 설정
    private int progressPoints = 0; // 기본값 설정
    private LocalDateTime createdAt = LocalDateTime.now(); // 생성일자

    // gradeLevel, role 등에 대한 ENUM 검증은 서비스 로직에서 처리합니다.
}