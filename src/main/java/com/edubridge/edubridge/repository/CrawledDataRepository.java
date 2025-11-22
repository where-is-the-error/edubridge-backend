// src/main/java/com/edubridge/edubridge/repository/CrawledDataRepository.java
package com.edubridge.edubridge.repository;

import com.edubridge.edubridge.model.CrawledData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawledDataRepository extends MongoRepository<CrawledData, String> {
}