// src/main/java/com/edubridge/edubridge/repository/ContentRepository.java

package com.edubridge.edubridge.repository;

import com.edubridge.edubridge.model.Content;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContentRepository extends MongoRepository<Content, String> {
    // 모든 콘텐츠 조회 (GET /api/contents)는 MongoRepository의 findAll()로 충분합니다.

    // 향후 필터링/검색 기능 추가 시 여기에 메소드를 정의합니다.
}