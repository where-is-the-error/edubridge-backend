// src/main/java/com/edubridge/edubridge/controller/ContentController.java

package com.edubridge.edubridge.controller;

import com.edubridge.edubridge.model.Content;
import com.edubridge.edubridge.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import java.util.List;

// src/main/java/com/edubridge/edubridge/controller/ContentController.java (추가)

// ... (기존 import 생략)
import org.springframework.web.bind.annotation.PostMapping; // POST 맵핑 import
import org.springframework.web.bind.annotation.RequestBody; // JSON Body 수신 import

// ...

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    // 1. 모든 콘텐츠 목록 조회 API: GET /api/contents (기존 코드)
    @GetMapping
    public ResponseEntity<List<Content>> getAllContents() {
        List<Content> contents = contentService.getAllContents();
        return ResponseEntity.ok(contents);
    }

    // ⭐️ 2. 새로운 콘텐츠 등록 API: POST /api/contents ⭐️
    @PostMapping
    public ResponseEntity<Content> createContent(@RequestBody Content content) {
        // Service를 통해 DB에 저장합니다.
        Content newContent = contentService.createContent(content);

        // 201 Created 상태 코드와 함께 새로 저장된 객체를 반환합니다.
        return ResponseEntity.status(HttpStatus.CREATED).body(newContent);
    }
}