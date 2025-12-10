package com.edubridge.edubridge.service;

import com.edubridge.edubridge.model.CrawledData;
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

    // 🌟 1. 맞춤형 문제 생성 (수치화된 레벨 반영)
    public String generateProblem(String userId, String userInput) {

        // 실제 로그인된 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String nickname = user.getNickname();
        String gradeInfo = (user.getGradeLevel() != null ? user.getGradeLevel() : "") + " "
                + (user.getGradeNumber() != null ? user.getGradeNumber() + "학년" : "");
        String subject = user.getSubjectPrimary() != null ? user.getSubjectPrimary() : "일반";

        // ⭐️ 수치화된 정보 가져오기
        Integer userLevel = user.getLevel(); // 1 ~ 10
        String userAnalysis = (user.getAiAnalysis() != null && !user.getAiAnalysis().isEmpty())
                ? user.getAiAnalysis()
                : "아직 분석 데이터가 없습니다. 학생의 반응을 보고 수준을 파악해주세요.";

        // 프롬프트 엔지니어링: 수치 레벨에 따른 가이드라인 제시
        String promptText = String.format(
                "당신은 학생의 수준을 10단계로 정밀하게 관리하는 AI 튜터입니다.\n" +
                        "다음 학생 정보를 바탕으로 맞춤형 문제를 1개 출제해주세요.\n\n" +
                        "[학생 프로필]\n" +
                        "- 이름: %s\n" +
                        "- 학년/과목: %s / %s\n" +
                        "- **현재 레벨: %d (1~10단계)**\n" + // 👈 수치 레벨 전달
                        "- **AI 분석 노트: %s**\n\n" +
                        "[요청 사항]\n" +
                        "%s\n\n" +
                        "[출제 가이드라인]\n" +
                        "1. 학생의 레벨(%d)에 맞춰 난이도를 조절하세요.\n" +
                        "   - Lv 1~3: 기초 개념 확인, 힌트 제공, 아주 친절한 설명\n" +
                        "   - Lv 4~7: 대표 유형 문제, 함정 피하기, 명확한 해설\n" +
                        "   - Lv 8~10: 복합 개념 응용, 고난도 추론, 논리적 사고 요구\n" +
                        "2. 이전 분석 노트(%s)를 참고하여 학생의 약점을 보완하세요.\n" +
                        "3. 문제, 정답, 해설을 명확히 구분해서 답변해주세요.",
                nickname, gradeInfo, subject, userLevel, userAnalysis,
                userInput, userLevel, userAnalysis
        );

        return callGeminiApi(promptText);
    }

    // 🌟 2. 학습 평가 및 레벨 조정 (수치 기반)
    public void updateUserLevelAnalysis(String userId, String studyLog) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // AI에게 레벨 조정 판단 요청
        String analysisPrompt = String.format(
                "학생의 최근 문제 풀이 기록을 보고, 다음 두 가지를 수행해줘.\n" +
                        "1. 학생의 특성과 약점을 한 문단으로 요약 (DB 저장용)\n" +
                        "2. 현재 레벨(%d)을 유지할지, 올릴지(+1), 내릴지(-1) 판단\n\n" +
                        "기존 분석: %s\n" +
                        "최근 풀이 기록: %s\n\n" +
                        "답변 형식: [레벨변동] | [분석요약]\n" +
                        "(예시: +1 | 정답률이 높고 풀이 속도가 빠릅니다. 응용 단계로 넘어가도 좋습니다.)\n" +
                        "(예시: 0 | 아직 개념 이해가 부족해 보입니다. 현행 유지하여 기초를 다져야 합니다.)",
                user.getLevel(), user.getAiAnalysis(), studyLog
        );

        String response = callGeminiApi(analysisPrompt);

        // 응답 파싱 및 DB 업데이트
        try {
            if (response.contains("|")) {
                String[] parts = response.split("\\|", 2);
                String levelChangeStr = parts[0].trim(); // "+1", "0", "-1"
                String newAnalysis = parts[1].trim();

                // 1. 분석 내용 업데이트
                user.setAiAnalysis(newAnalysis);

                // 2. 레벨 수치 업데이트 (최소 1, 최대 10 제한)
                int currentLevel = user.getLevel();
                int change = Integer.parseInt(levelChangeStr.replace("+", "")); // "+1" -> 1
                int nextLevel = Math.max(1, Math.min(10, currentLevel + change));

                user.setLevel(nextLevel);

                userRepository.save(user);
            }
        } catch (Exception e) {
            System.err.println("레벨 업데이트 실패: " + e.getMessage());
            // 파싱 실패 시 분석 내용만이라도 저장 시도 가능
        }
    }

    // [기존 코드 유지] 유튜브 분석 (User 객체 받는 버전)
    public CrawledData analyzeYoutubeVideo(CrawledData data, User user) {
        // ... (이전 코드와 동일, user 정보를 활용) ...
        return data;
    }

    // [기존 코드 유지] API 호출 로직
    private String callGeminiApi(String promptText) {
        try {
            GeminiRequest request = new GeminiRequest(
                    Collections.singletonList(new Content(
                            Collections.singletonList(new Part(promptText))
                    ))
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

            String url = apiUrl + "?key=" + apiKey;
            ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(url, entity, GeminiResponse.class);

            if (response.getBody() != null && !response.getBody().getCandidates().isEmpty()) {
                return response.getBody().getCandidates().get(0).getContent().getParts().get(0).getText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "AI 응답 오류";
    }

    // DTO Classes
    @Data @lombok.AllArgsConstructor static class GeminiRequest { private List<Content> contents; }
    @Data @lombok.AllArgsConstructor @lombok.NoArgsConstructor static class Content { private List<Part> parts; }
    @Data @lombok.AllArgsConstructor @lombok.NoArgsConstructor static class Part { private String text; }
    @Data static class GeminiResponse { private List<Candidate> candidates; }
    @Data static class Candidate { private Content content; }
}