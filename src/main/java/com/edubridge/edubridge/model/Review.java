// src/main/java/com/edubridge/edubridge/model/Review.java

package com.edubridge.edubridge.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;

@Data
@Document(collection = "reviews") // 'reviews' 컬렉션과 매핑
public class Review {

    @Id
    private String id;

    // 콘텐츠 참조 (MongoDB의 ObjectId 참조)
    private String contentId;

    // 사용자 참조
    private String userId;

    private int rating; // 1~5점
    private String comment;
    private LocalDateTime createdAt = LocalDateTime.now();
}