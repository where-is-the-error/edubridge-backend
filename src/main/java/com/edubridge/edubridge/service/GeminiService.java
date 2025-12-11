package com.edubridge.edubridge.service;

import com.edubridge.edubridge.model.ChatLog;
import com.edubridge.edubridge.model.CrawledData;
import com.edubridge.edubridge.model.User;
import com.edubridge.edubridge.repository.ChatLogRepository;
import com.edubridge.edubridge.repository.MemoRepository;
import com.edubridge.edubridge.repository.UserRepository;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.edubridge.edubridge.model.Memo;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final UserRepository userRepository;
    private final ChatLogRepository chatLogRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final MemoRepository memoRepository;
    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.api-url}")
    private String apiUrl;
    public String summarizeMemosForTimetable(String userId) {
        // 1. 유저의 모든 메모 가져오기
        List<Memo> memos = memoRepository.findByUserId(userId);

        if (memos.isEmpty()) {
            return "아직 작성된 메모가 없어요! 메인 화면에서 메모를 붙여보세요. 📝";
        }

        // 2. 메모 내용 합치기
        String allMemos = memos.stream()
                .map(Memo::getContent)
                .collect(Collectors.joining("\n- "));

        // 3. AI 프롬프트 생성
        String prompt = String.format("""
            당신은 학생의 학습 비서 '코니'입니다.
            학생이 책상에 붙여둔 메모들을 확인하고, 시간표 옆에 붙여둘 '오늘의 요약 노트'를 만들어주세요.
            
            [학생의 메모 내용]
            - %s
            
            [작성 가이드]
            1. 중복되거나 비슷한 내용은 하나로 합치세요.
            2. '할 일(To-Do)', '기억할 것', '기타' 등으로 카테고리를 나눠 깔끔하게 정리하세요.
            3. 이모티콘을 사용하여 보기 좋게 꾸며주세요.
            4. 문체는 친절하게 존댓말을 사용하세요.
            5. 전체 길이는 너무 길지 않게 요약해주세요.
            """, allMemos);

        // 4. AI 호출 및 반환
        return callGeminiApi(prompt);
    }
    // =====================================================================
    // 1. 대화형 AI 채팅 (호랑이 선생님 코니) - [Chat 기능]
    // =====================================================================
    public String chatWithAi(String userId, String userMessage) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. 사용자 메시지 DB 저장
        saveChatLog(userId, "user", userMessage);

        // 3. 이전 대화 기록 전체 가져오기
        List<ChatLog> history = chatLogRepository.findByUserIdOrderByTimestampAsc(userId);

        // 4. 프롬프트 구성 (페르소나 + 대화 내역 + 현재 질문)
        String prompt = buildChatPrompt(user, history, userMessage);

        // 5. API 호출
        String aiResponse = callGeminiApi(prompt);

        // 6. AI 응답 DB 저장
        saveChatLog(userId, "model", aiResponse);

        return aiResponse;
    }

    // 채팅 로그 저장 헬퍼
    private void saveChatLog(String userId, String role, String msg) {
        ChatLog log = new ChatLog();
        log.setUserId(userId);
        log.setRole(role);
        log.setMessage(msg);
        chatLogRepository.save(log);
    }

    // 채팅용 프롬프트 생성 (호랑이 페르소나)
    private String buildChatPrompt(User user, List<ChatLog> history, String currentInput) {
        String historyText = history.stream()
                .map(log -> (log.getRole().equals("user") ? "학생: " : "선생님(코니): ") + log.getMessage())
                .collect(Collectors.joining("\n"));

        String studentName = user.getNickname();
        String grade = (user.getGradeLevel() != null ? user.getGradeLevel() : "") + " " + user.getGradeNumber() + "학년";
        String subject = user.getSubjectPrimary();

        return String.format("""
            당신은 '에듀브릿지'의 친절하고 열정적인 AI 선생님 '코니(호랑이)'입니다.
            학생의 이름은 '%s'이고, %s이며, 주력 과목은 '%s'입니다.
            현재 레벨은 %d/100 입니다.
            
            [말투 가이드]
            - "안녕! 코니야~", "참 잘했어!", "~했니?"처럼 친근하고 부드러운 반말(해요체 혼용 가능)을 쓰세요.
            - 문장 끝에 🐯, 📚, ✨, 💪 같은 이모티콘을 적절히 붙여주세요.
            - 설명은 아주 쉽고 친절하게 풀어주세요. 딱딱한 AI 느낌을 내지 마세요.
            
            [대화 기록]
            %s
            
            [학생의 질문]
            %s
            
            위 맥락을 다 기억해서 자연스럽게 답변해주세요.
            """,
                studentName, grade, subject, user.getLevel(),
                historyText, currentInput
        );
    }

    // =====================================================================
    // 2. 유튜브 영상 분석 (크롤링 데이터용) - [Crawler 기능]
    // =====================================================================
    public CrawledData analyzeYoutubeVideo(CrawledData data, User user) {
        String commentsText = (data.getComments() != null && !data.getComments().isEmpty())
                ? String.join(" | ", data.getComments())
                : "댓글 없음";

        String nickname = (user != null && user.getNickname() != null) ? user.getNickname() : "학생";
        String userGrade = (user != null && user.getGradeLevel() != null)
                ? user.getGradeLevel() + (user.getGradeNumber() != null ? user.getGradeNumber() : "")
                : "학생";
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
                        "예시: 시각 자료가 훌륭함 | 설명이 조금 빠름 | 4.5 | %s님에게 개념 정리용으로 딱임",
                data.getTitle(),
                // 설명이 너무 길면 자름
                (data.getDescription() != null && data.getDescription().length() > 300)
                        ? data.getDescription().substring(0, 300)
                        : data.getDescription(),
                commentsText,
                nickname, userGrade, userSubject,
                nickname, nickname
        );

        String result = callGeminiApi(prompt);

        try {
            if (result != null && result.contains("|")) {
                String[] parts = result.split("\\|", 4);
                if (parts.length >= 4) {
                    String pros = parts[0].trim();
                    String cons = parts[1].trim();
                    double rating = Double.parseDouble(parts[2].trim());
                    String comment = parts[3].trim();

                    data.setAiRating(rating);
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
            data.setAiRating(0.0);
            data.setAiComment("분석 실패: " + result);
        }

        return data;
    }

    // =====================================================================
    // 3. 공통 API 호출 로직
    // =====================================================================
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
            System.err.println("Gemini API 호출 오류: " + e.getMessage());
        }
        // 오류 발생 시 기본 메시지
        return "어흥? 잠시 연결이 원활하지 않아. 다시 말해줄래? 🐯";
    }

    // DTO Classes
    @Data @AllArgsConstructor @NoArgsConstructor static class GeminiRequest { private List<Content> contents; }
    @Data @AllArgsConstructor @NoArgsConstructor static class Content { private List<Part> parts; }
    @Data @AllArgsConstructor @NoArgsConstructor static class Part { private String text; }
    @Data static class GeminiResponse { private List<Candidate> candidates; }
    @Data static class Candidate { private Content content; }
}