// src/main/java/com/edubridge/edubridge/model/Content.java

package com.edubridge.edubridge.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "contents") // 'contents' 컬렉션과 매핑
public class Content {

    @Id
    private String id;

    private String title;
    private String type; // video, worksheet, book, problem 등
    private String subject; // 국어, 수학, 영어
    private List<String> gradeLevels; // 여러 학년 대상 가능 (List 사용)
    private String url;
    private String source;
    private String description;
    private List<String> tags; // 검색용 키워드

    private double averageRating = 0.0; // 평균 별점
    private int reviewCount = 0; // 리뷰 수

    private boolean isApproved = false;
    private boolean isPaid = false;

    // 문제 상세 정보 (problem 타입일 경우)
    private ProblemDetails problemDetails;

    private LocalDateTime createdAt = LocalDateTime.now();

    // 내부 클래스: 문제 상세 정보를 담는 Document
    @Data
    public static class ProblemDetails {
        private String question;
        private String answer;
    }
}