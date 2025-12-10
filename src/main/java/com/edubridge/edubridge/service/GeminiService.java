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

    // 1. 맞춤형 문제 생성 (1~100 레벨 반영)
    public String generateProblem(String userId, String userInput) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String nickname = user.getNickname();
        String gradeInfo = (user.getGradeLevel() != null ? user.getGradeLevel() : "") + " "
                + (user.getGradeNumber() != null ? user.getGradeNumber() + "학년" : "");
        String subject = user.getSubjectPrimary() != null ? user.getSubjectPrimary() : "일반";

        Integer userLevel = user.getLevel();
        String userAnalysis = (user.getAiAnalysis() != null && !user.getAiAnalysis().isEmpty())
                ? user.getAiAnalysis()
                : "신규 학습자입니다. 기초적인 수준부터 탐색해주세요.";

        String promptText = String.format(
                "당신은 학생의 실력을 1부터 100까지 수치화하여 관리하는 AI 튜터입니다.\n" +
                        "다음 학생 정보를 바탕으로 맞춤형 문제를 1개 출제해주세요.\n\n" +
                        "[학생 프로필]\n" +
                        "- 이름: %s\n" +
                        "- 학년/과목: %s / %s\n" +
                        "- **현재 레벨: %d (1~100)**\n" +
                        "- **AI 분석 기록: %s**\n\n" +
                        "[요청 사항]\n" +
                        "%s\n\n" +
                        "[출제 가이드라인]\n" +
                        "1. 학생의 레벨(%d/100)에 맞춰 난이도를 정밀하게 조절하세요.\n" +
                        "   - 1~20: 아주 쉬운 기초 개념 (자세한 힌트 포함)\n" +
                        "   - 21~50: 교과서 기본 예제 수준\n" +
                        "   - 51~80: 응용 문제 및 심화 유형\n" +
                        "   - 81~100: 최상위권 킬러 문항, 창의적 사고 요구\n" +
                        "2. 이전 분석 기록을 참고하여 학생의 약점은 보완하고 강점은 강화하세요.\n" +
                        "3. 문제, 정답, 해설을 명확히 구분해서 답변해주세요.",
                nickname, gradeInfo, subject, userLevel, userAnalysis,
                userInput, userLevel
        );

        return callGeminiApi(promptText);
    }

    // 2. 학습 평가 및 레벨 미세 조정 (점수제)
    public void updateUserLevelAnalysis(String userId, String studyLog) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String analysisPrompt = String.format(
                "학생의 최근 문제 풀이 기록을 분석하여 다음 두 가지를 수행해줘.\n" +
                        "1. 학생의 특성, 장단점을 한 문단으로 요약 (DB 저장용)\n" +
                        "2. 현재 레벨(%d/100)에서 몇 점을 올리거나 내릴지 정수로 판단 (예: +5, -2, 0)\n\n" +
                        "기존 분석: %s\n" +
                        "최근 풀이: %s\n\n" +
                        "답변 형식: [점수변동] | [분석요약]\n" +
                        "(예시: +3 | 계산 속도가 빨라졌습니다. 다만 응용력은 아직 부족해 보입니다.)",
                user.getLevel(), user.getAiAnalysis(), studyLog
        );

        String response = callGeminiApi(analysisPrompt);

        try {
            if (response.contains("|")) {
                String[] parts = response.split("\\|", 2);
                String scoreChangeStr = parts[0].trim().replace("+", "");
                String newAnalysis = parts[1].trim();

                user.setAiAnalysis(newAnalysis);

                int currentLevel = user.getLevel();
                int change = Integer.parseInt(scoreChangeStr);
                int nextLevel = Math.max(1, Math.min(100, currentLevel + change));

                user.setLevel(nextLevel);

                userRepository.save(user);
            }
        } catch (Exception e) {
            System.err.println("레벨 업데이트 실패: " + e.getMessage());
        }
    }

    // ⭐️ 3. 유튜브 영상 분석 (비어있던 부분 채워넣음!)
    public CrawledData analyzeYoutubeVideo(CrawledData data, User user) {
        String commentsText = (data.getComments() != null && !data.getComments().isEmpty())
                ? String.join(" | ", data.getComments())
                : "댓글 없음";

        // 사용자 정보 처리 (Null 방지)
        String nickname = (user != null && user.getNickname() != null) ? user.getNickname() : "학생";
        String userGrade = (user != null && user.getGradeLevel() != null)
                ? user.getGradeLevel() + (user.getGradeNumber() != null ? user.getGradeNumber() : "")
                : "중학생";
        String userSubject = (user != null && user.getSubjectPrimary() != null) ? user.getSubjectPrimary() : "일반";

        String prompt = String.format(
                "다음 유튜브 학습 영상 정보를 분석해서 학습자에게 맞춤 평가를 해주세요.\n\n" +
                        "[영상 정보]\n" +
                        "제목: %s\n" +
                        "설명: %s\n" +
                        "댓글 반응: %s\n\n" +
                        "[학습자 정보]\n" +
                        "- 이름: %s\n" +
                        "- 학년: %s\n" +
                        "- 관심 과목: %s\n\n" +
                        "위 정보를 바탕으로 다음 4가지를 '|'로 구분하여 답변해주세요.\n" +
                        "1. 장점 1가지\n" +
                        "2. 단점 1가지\n" +
                        "3. 추천 별점 (1.0~5.0)\n" +
                        "4. %s님을 위한 한 줄 추천평 (친근하게)\n\n" +
                        "형식: 장점 | 단점 | 별점 | 한줄평\n" +
                        "예시: 시각 자료가 훌륭함 | 설명이 조금 빠름 | 4.5 | %s님에게 개념 정리용으로 딱이에요!",
                data.getTitle(),
                data.getDescription().substring(0, Math.min(data.getDescription().length(), 300)),
                commentsText,
                nickname, userGrade, userSubject,
                nickname, nickname
        );

        String result = callGeminiApi(prompt);
        System.out.println(">>> Gemini 분석 결과: " + result); // 디버깅용 로그

        // 결과 파싱
        try {
            if (result.contains("|")) {
                String[] parts = result.split("\\|", 4);

                if (parts.length >= 4) {
                    String pros = parts[0].trim();
                    String cons = parts[1].trim();
                    double rating = Double.parseDouble(parts[2].trim());
                    String comment = parts[3].trim();

                    data.setAiRating(rating);
                    // DB에 저장할 최종 코멘트 (줄바꿈 포함)
                    String combinedComment = String.format("👍 장점: %s\n👎 단점: %s\n💬 %s", pros, cons, comment);
                    data.setAiComment(combinedComment);
                } else {
                    data.setAiRating(0.0);
                    data.setAiComment(result);
                }
            } else {
                data.setAiRating(0.0);
                data.setAiComment(result);
            }
        } catch (Exception e) {
            System.err.println("AI 분석 파싱 실패: " + e.getMessage());
            data.setAiRating(0.0);
            data.setAiComment("분석 실패");
        }

        return data;
    }

    // 공통 API 호출 메서드
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
            System.err.println("Gemini API 호출 중 오류 발생: " + e.getMessage());
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