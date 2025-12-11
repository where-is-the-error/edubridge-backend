package com.edubridge.edubridge.repository;

import com.edubridge.edubridge.model.Memo;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface MemoRepository extends MongoRepository<Memo, String> {

    // 특정 유저의 모든 메모 가져오기
    List<Memo> findByUserId(String userId);

    // 특정 유저의 특정 메모(프론트 ID 기준) 가져오기 -> 수정/삭제용
    Optional<Memo> findByUserIdAndFrontendMemoId(String userId, Long frontendMemoId);
}