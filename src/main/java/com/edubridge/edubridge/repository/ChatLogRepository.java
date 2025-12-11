package com.edubridge.edubridge.repository;

import com.edubridge.edubridge.model.ChatLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ChatLogRepository extends MongoRepository<ChatLog, String> {
    // 특정 유저의 채팅 기록을 시간순으로 가져오기 (최근 N개만 가져올 때 사용)
    List<ChatLog> findByUserIdOrderByTimestampAsc(String userId);
}