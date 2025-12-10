package com.edubridge.edubridge.repository;

import com.edubridge.edubridge.model.CrawledData;
import org.springframework.data.mongodb.repository.MongoRepository;
// import org.springframework.data.jpa.repository.JpaRepository; // <-- JPA를 썼다면 삭제

// JpaRepository(또는 CrudRepository) 대신 MongoRepository를 상속합니다.
public interface CrawledDataRepository extends MongoRepository<CrawledData, String> {
    // ⭐️ [추가] URL 중복 검사를 위한 메소드
    boolean existsByDetailUrl(String detailUrl);
}