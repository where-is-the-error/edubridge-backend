package com.edubridge.edubridge.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "memos") // MongoDB의 'memos' 컬렉션에 저장됨
public class Memo {

    @Id
    private String id;

    private String userId;       // 작성자 ID (누가 썼는지)
    private Long frontendMemoId; // 프론트엔드에서 생성한 ID (매핑용)
    private String content;      // 메모 내용

    private LocalDateTime updatedAt = LocalDateTime.now(); // 수정 시간
}