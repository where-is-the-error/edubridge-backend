package com.edubridge.edubridge.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "chat_logs")
public class ChatLog {
    @Id
    private String id;

    private String userId;      // 누구의 채팅인지
    private String role;        // "user" 또는 "model" (AI)
    private String message;     // 대화 내용
    private LocalDateTime timestamp = LocalDateTime.now();
}