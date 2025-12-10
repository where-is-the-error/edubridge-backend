package com.edubridge.edubridge.controller;

import com.edubridge.edubridge.model.CrawledData;
import com.edubridge.edubridge.repository.CrawledDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final CrawledDataRepository repository;

    // 접속 주소: http://localhost:3000/test/db
    @GetMapping("/test/db")
    public String testDbInsert() {
        try {
            CrawledData data = new CrawledData();
            data.setTitle("테스트 데이터입니다");
            data.setDescription("DB 연결 확인용");
            data.setDetailUrl("http://test.com");

            repository.save(data); // 저장 시도

            return "저장 성공! DataGrip에서 확인해보세요. (ID: " + data.getId() + ")";
        } catch (Exception e) {
            e.printStackTrace();
            return "저장 실패: " + e.getMessage();
        }
    }
}