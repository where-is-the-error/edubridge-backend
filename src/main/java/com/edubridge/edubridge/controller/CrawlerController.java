// edubridge-backend/src/main/java/com/edubridge/edubridge/controller/CrawlerController.java

package com.edubridge.edubridge.controller;

import com.edubridge.edubridge.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CrawlerController {

    private final CrawlerService crawlerService;

    // 사용법: http://localhost:3000/api/crawl/youtube?keyword=중학교+2학년+수학
    @GetMapping("/api/crawl/youtube")
    public String startYoutubeCrawl(@RequestParam(defaultValue = "중학교 2학년 수학") String keyword) {
        try {
            int count = crawlerService.crawlYoutube(keyword);
            return String.format("크롤링 완료! '%s' 관련 영상 %d개를 분석하여 저장했습니다.", keyword, count);
        } catch (Exception e) {
            return "크롤링 실패: " + e.getMessage();
        }
    }
}