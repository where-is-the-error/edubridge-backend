package com.edubridge.edubridge.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "crawled_data") // MongoDB 컬렉션 이름을 지정합니다.
public class CrawledData {

    @Id // 이 필드가 MongoDB의 고유 ID (_id)가 됩니다.
    private String id; // MongoDB의 ID는 String이나 ObjectId 타입이 일반적입니다.

    private String title;
    private String detailUrl;
    private String imageUrl;
    // Getter, Setter, Constructors (생략)
    // ...

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}