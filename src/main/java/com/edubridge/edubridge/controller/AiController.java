package com.edubridge.edubridge.controller;

import com.edubridge.edubridge.dto.AiRequestDto;
import com.edubridge.edubridge.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final GeminiService geminiService;
    @GetMapping("/memo-summary")
    public ResponseEntity<String> getMemoSummary(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        String userId = authentication.getName();
        String summary = geminiService.summarizeMemosForTimetable(userId);
        return ResponseEntity.ok(summary);
    }
    @PostMapping("/chat") // 엔드포인트 이름 변경 권장
    public ResponseEntity<String> chat(@RequestBody AiRequestDto requestDto,
                                       Authentication authentication) {
        String userId = authentication.getName();

        // chatWithAi 메서드 호출 (DB 저장 및 히스토리 반영됨)
        String response = geminiService.chatWithAi(userId, requestDto.getUserPrompt());

        return ResponseEntity.ok(response);
    }
}