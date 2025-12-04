package com.edubridge.edubridge.service;

import com.edubridge.edubridge.model.User;
import com.edubridge.edubridge.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.api-url}")
    private String apiUrl;

    public String generateProblem(String userId, String userInput) {
        // 1. 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 2. 프롬프트 구성
        String promptText = String.format(
                "너는 학생을 위한 친절한 AI 선생님이야. 다음 정보를 바탕으로 학습 문제를 1개 만들어줘.\n" +
                        "- 학년: %s %d학년\n" +
                        "- 과목: %s\n" +
                        "- 요청사항: %s\n\n" +
                        "문제와 정답, 그리고 간단한 해설을 포함해서 답변해줘.",
                user.getGradeLevel(), user.getGradeNumber(),
                user.getSubjectPrimary(), userInput
        );

        // 3. Gemini API 요청 본문 생성
        GeminiRequest request = new GeminiRequest(
                Collections.singletonList(new Content(
                        Collections.singletonList(new Part(promptText))
                ))
        );

        // 4. 헤더 설정 (API 키는 URL 파라미터로 전달됨)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

        // 5. API 호출
        try {
            String url = apiUrl + "?key=" + apiKey;
            ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(url, entity, GeminiResponse.class);

            if (response.getBody() != null && !response.getBody().getCandidates().isEmpty()) {
                return response.getBody().getCandidates().get(0).getContent().getParts().get(0).getText();
            } else {
                return "AI 응답을 받아오지 못했습니다.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "AI 서비스 연결 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    // --- DTO 클래스들 (API 요청/응답 구조 매핑) ---
    @Data
    @lombok.AllArgsConstructor
    static class GeminiRequest {
        private List<Content> contents;
    }

    @Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    static class Content {
        private List<Part> parts;
    }

    @Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    static class Part {
        private String text;
    }

    @Data
    static class GeminiResponse {
        private List<Candidate> candidates;
    }

    @Data
    static class Candidate {
        private Content content;
    }
}