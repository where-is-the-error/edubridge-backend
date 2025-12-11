package com.edubridge.edubridge.repository;

import com.edubridge.edubridge.model.SchoolInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface SchoolInfoRepository extends MongoRepository<SchoolInfo, String> {
    // 특정 유저의 학교 정보를 찾기 위한 메소드
    Optional<SchoolInfo> findByUserId(String userId);
}