package com.edubridge.edubridge.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "crawled_items") // MongoDB 컬렉션 이름 지정
public class CrawledData {
    @Id
    private String id;
    private String title;      // 크롤링할 제목
    private String detailUrl;  // 크롤링할 상세 URL
    private String category;   // 크롤링할 카테고리 등
}