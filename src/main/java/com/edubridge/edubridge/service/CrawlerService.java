// src/main/java/com/edubridge.edubridge.service/CrawlerService.java

package com.edubridge.edubridge.service;

import com.edubridge.edubridge.model.CrawledData;
import com.edubridge.edubridge.repository.CrawledDataRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class CrawlerService {

    @Autowired
    private CrawledDataRepository crawledDataRepository;

    public int crawlAndSave(String targetUrl, String itemSelector) throws IOException, InterruptedException {
        int savedCount = 0;

        // Jsoup으로 URL에 접속하여 HTML 문서 가져오기
        Document doc = Jsoup.connect(targetUrl)
                .userAgent("Mozilla/5.0")
                .timeout(30000)
                .get();

        // 1. 목록 요소들 선택
        Elements items = doc.select(itemSelector);

        // ⭐️ 디버깅 팁: 목록 개수 확인
        System.out.println("DEBUG: Found " + items.size() + " potential items with selector: " + itemSelector);

        for (Element item : items) {

            // ⭐️ 10개만 저장하고 루프를 종료하는 조건 ⭐️
            if (savedCount >= 10) {
                System.out.println("DEBUG: Reached maximum count (10). Stopping crawl loop.");
                break;
            }

            // 2. 제목 추출: 실제 웹사이트에 맞춰 셀렉터를 수정해야 합니다. (예시: h3 태그 안의 a 태그)
            Element titleElement = item.selectFirst("span.recent-title");
            String title = (titleElement != null) ? titleElement.text() : "TITLE NOT FOUND";

            // 3. 링크 추출: 제목 요소의 부모 또는 주변에서 링크를 찾습니다.
            Element linkElement = item.selectFirst("a");
            String link = (linkElement != null) ? linkElement.attr("abs:href") : "LINK NOT FOUND";
            Element imgElement = item.selectFirst("img");
            String imgUrl = (imgElement != null) ? imgElement.attr("abs:src") : "https://example.com/default-image.png";
            // 4. 저장 조건: 제목과 링크가 실제로 발견되었을 때만 저장
            if (titleElement != null && linkElement != null) {
                CrawledData data = new CrawledData();
                data.setTitle(title);
                data.setDetailUrl(link);
                data.setImageUrl(imgUrl);
                crawledDataRepository.save(data);
                savedCount++;
            }

            // 서버 부하 방지용 딜레이
            Thread.sleep(500);
        }

        System.out.println("DEBUG: Total items successfully saved: " + savedCount);
        return savedCount;
    }
}