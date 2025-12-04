package com.edubridge.edubridge.controller;

import com.edubridge.edubridge.dto.AiRequestDto;
import com.edubridge.edubridge.service.GeminiService; // 👈 변경됨
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AiController {

    private final GeminiService geminiService; // 👈 AiService 대신 사용

    @PostMapping("/generate")
    public ResponseEntity<String> generateProblem(@RequestBody AiRequestDto requestDto,
                                                  Authentication authentication) {
        String userId = authentication.getName();

        // GeminiService 호출
        String response = geminiService.generateProblem(userId, requestDto.getUserPrompt());

        return ResponseEntity.ok(response);
    }
}