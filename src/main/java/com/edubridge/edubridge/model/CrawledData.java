package com.edubridge.edubridge.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
// ⭐️ 이 설정이 있으면 'edubridge' DB 안의 'crawled_data' 컬렉션에 저장됩니다.
@Document(collection = "crawled_data")
public class CrawledData {

    @Id
    private String id;

    private String title;           // 영상 제목
    private String detailUrl;       // 영상 링크
    private String imageUrl;        // 썸네일 이미지

    private String description;     // 영상 설명
    private List<String> comments;  // 수집된 댓글 리스트

    private Double aiRating;        // AI 별점
    private String aiComment;       // AI 한줄평
}