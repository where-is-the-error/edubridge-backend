// edubridge-backend/src/main/java/com/edubridge/edubridge/model/CrawledData.java

package com.edubridge.edubridge.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "crawled_data")
public class CrawledData {

    @Id
    private String id;

    private String title;           // 영상 제목
    private String detailUrl;       // 영상 링크
    private String imageUrl;        // 썸네일 이미지

    // --- 추가된 필드 ---
    private String description;     // 영상 설명
    private List<String> comments;  // 수집된 댓글 리스트 (여론 분석용)

    private Double aiRating;        // AI가 매긴 점수 (1.0 ~ 5.0)
    private String aiComment;       // AI 한줄평
}