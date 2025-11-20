// src/main/java/com/edubridge/edubridge/service/ContentService.java

package com.edubridge.edubridge.service;

import com.edubridge.edubridge.model.Content;
import com.edubridge.edubridge.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

// src/main/java/com/edubridge/edubridge/service/ContentService.java (추가)

// ... (기존 import 생략)
// ...

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;

    // 1. 모든 콘텐츠를 조회하는 로직 (기존 코드)
    public List<Content> getAllContents() {
        return contentRepository.findAll();
    }

    // ⭐️ 2. 새로운 콘텐츠를 등록하는 로직 ⭐️
    public Content createContent(Content content) {
        // [향후: 중복 체크나 유효성 검사 로직이 여기에 추가됩니다.]

        // MongoDB에 저장하고 저장된 객체를 반환합니다.
        return contentRepository.save(content);
    }
}