// src/main/java/com/edubridge/edubridge/repository/ReviewRepository.java

package com.edubridge.edubridge.repository;

import com.edubridge.edubridge.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {

    // 특정 사용자가 특정 콘텐츠에 이미 리뷰를 작성했는지 확인하는 메소드
    Optional<Review> findByContentIdAndUserId(String contentId, String userId);
}