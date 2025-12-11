package com.edubridge.edubridge.service;

import com.edubridge.edubridge.model.User;
import com.edubridge.edubridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate; // 실제 API 호출 시 필요

@Service
@RequiredArgsConstructor
public class AiService {

    private final UserRepository userRepository;

    // 실제로는 OpenAI API Key 등을 사용하여 HTTP 요청을 보내야 합니다.
    // 여기서는 로직의 흐름을 보여주는 모의(Mock) 응답을 구현합니다.
    public String generateProblem(String userId, String userInput) {

        // 1. 사용자 정보 조회 (개인화를 위함)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 2. 프롬프트 엔지니어링 (사용자 정보를 문맥에 추가)
        String grade = user.getGradeLevel(); // 예: elementary, high
        Integer gradeNum = user.getGradeNumber(); // 예: 3
        String subject = user.getSubjectPrimary(); // 예: math

        // 문맥 생성
        StringBuilder systemPrompt = new StringBuilder();
        systemPrompt.append("당신은 친절한 과외 선생님입니다. ");
        systemPrompt.append("대상 학생은 " + grade + " " + gradeNum + "학년이며, ");
        systemPrompt.append("관심 과목은 " + subject + "입니다. ");
        systemPrompt.append("학생의 수준에 딱 맞춰서 답변해주세요.");

        String finalPrompt = String.format(
                "[%s] 설정에 맞춰서 다음 요청을 처리해줘: %s",
                systemPrompt.toString(),
                userInput
        );

        // 3. (가상) AI API 호출 부분
        // RestTemplate 등을 사용하여 OpenAI API에 'finalPrompt'를 전송합니다.
        System.out.println(">>> AI로 전송될 프롬프트: " + finalPrompt);

        // --- 여기서는 더미 응답을 반환합니다 ---
        return "AI가 생성한 맞춤 문제: [" + subject + "] " + userInput + "에 대한 "
                + grade + " " + gradeNum + "학년 수준의 문제입니다. (예시: 1/2 + 1/4은?)";
    }
}