// src/main/java/com/edubridge/edubridge/service/CrawlerService.java
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
        Document doc = Jsoup.connect(targetUrl).get();

        // CSS Selector를 사용하여 크롤링할 목록 요소들 선택
        Elements items = doc.select(itemSelector);

        for (Element item : items) {
            // TODO: 실제 웹사이트에 맞춰 Selector를 수정해야 합니다.
            String title = item.selectFirst(".item-title").text();
            String link = item.selectFirst("a").attr("abs:href");

            CrawledData data = new CrawledData();
            data.setTitle(title);
            data.setDetailUrl(link);

            crawledDataRepository.save(data);
            savedCount++;

            // 서버 부하 방지용 딜레이
            Thread.sleep(500);
        }
        return savedCount;
    }
}