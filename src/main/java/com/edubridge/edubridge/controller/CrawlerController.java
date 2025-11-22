// src/main/java/com/edubridge/edubridge/controller/CrawlerController.java
package com.edubridge.edubridge.controller;

import com.edubridge.edubridge.service.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // 이 클래스가 RESTful API의 컨트롤러임을 명시
public class CrawlerController {

    // CrawlerService 의존성 주입 (Autowired)
    @Autowired
    private CrawlerService crawlerService;

    // HTTP GET 요청을 처리하는 엔드포인트 정의
    @GetMapping("/api/crawl/start")
    public String startCrawling() {
        try {
            // TODO: 실제 타겟 URL과 CSS Selector를 입력하세요.
            String url = "https://www.example.com";
            String selector = ".main-list .item";

            // Service 계층의 크롤링 로직 호출
            int count = crawlerService.crawlAndSave(url, selector);
            return "크롤링 완료! 총 " + count + "개의 데이터가 MongoDB에 저장되었습니다.";
        } catch (Exception e) {
            // 예외 발생 시 에러 메시지 반환
            return "크롤링 실패: " + e.getMessage();
        }
    }
}