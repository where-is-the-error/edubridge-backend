package com.edubridge.edubridge.dto;
import lombok.Data;

@Data
public class AiRequestDto {
    private String userPrompt; // 사용자가 입력한 요구사항 (예: "피타고라스 정리 문제 내줘")
}