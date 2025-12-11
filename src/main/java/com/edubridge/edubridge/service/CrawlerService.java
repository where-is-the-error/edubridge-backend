// edubridge-backend/src/main/java/com/edubridge/edubridge/service/CrawlerService.java

package com.edubridge.edubridge.service;

import com.edubridge.edubridge.model.CrawledData;
import com.edubridge.edubridge.model.User;
import com.edubridge.edubridge.repository.CrawledDataRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.edubridge.edubridge.model.User; // User import 확인

@Service
@RequiredArgsConstructor
public class CrawlerService {

    private final CrawledDataRepository crawledDataRepository;
    private final GeminiService geminiService;

    // ⭐️ 파라미터에 User 추가
    public int crawlYoutube(String keyword, User user) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);
        int savedCount = 0;

        try {
            String searchUrl = "https://www.youtube.com/results?search_query=" + keyword;
            System.out.println("Crawling URL: " + searchUrl);
            driver.get(searchUrl);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("ytd-video-renderer")));

            List<WebElement> videoElements = driver.findElements(By.cssSelector("ytd-video-renderer"));
            List<String> videoLinks = new ArrayList<>();

            for (int i = 0; i < Math.min(videoElements.size(), 30); i++) { // 테스트를 위해 5개로 줄임
                try {
                    WebElement titleEl = videoElements.get(i).findElement(By.id("video-title"));
                    String link = titleEl.getAttribute("href");
                    if (link != null && !link.contains("shorts")) {
                        videoLinks.add(link);
                    }
                } catch (Exception e) { continue; }
            }

            // 5. 각 영상 상세 크롤링 & 분석 (수정된 부분)
            for (String link : videoLinks) {
                try {
                    // ⭐️ [추가된 로직] DB에 이미 존재하는 영상인지 확인
                    if (crawledDataRepository.existsByDetailUrl(link)) {
                        System.out.println("이미 존재하는 영상입니다. 건너뜁니다: " + link);
                        continue; // 아래 로직(상세 크롤링, AI 분석, 저장) 모두 스킵하고 다음 영상으로 넘어감
                    }

                    // --- 여기서부터는 새로운 영상일 때만 실행됨 ---
                    CrawledData data = processSingleVideo(driver, link);

                    if (data != null) {
                        System.out.println("AI 분석 대기 중... (4초)");
                        Thread.sleep(4000);

                        // AI 분석
                        CrawledData analyzedData = geminiService.analyzeYoutubeVideo(data, user);

                        // AI 점수가 0이면 저장하지 않음 (선택사항: 필요하면 주석 해제)
                        /*
                        if (analyzedData.getAiRating() == 0) {
                            System.out.println("AI 분석 실패로 저장하지 않습니다.");
                            continue;
                        }
                        */

                        crawledDataRepository.save(analyzedData);
                        savedCount++;

                        System.out.println("--------------------------------------------------");
                        System.out.println("Saved New Video: " + analyzedData.getTitle());
                        System.out.println("--------------------------------------------------");
                    }
                } catch (Exception e) {
                    System.err.println("Failed to process video: " + link + " / Error: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return savedCount;
    }

    // 개별 영상 상세 정보 및 댓글 수집
    private CrawledData processSingleVideo(WebDriver driver, String url) throws InterruptedException {
        driver.get(url);
        Thread.sleep(2000); // 페이지 로딩 대기

        CrawledData data = new CrawledData();
        data.setDetailUrl(url);

        // 1. 제목
        String title = driver.findElement(By.cssSelector("#title > h1 > yt-formatted-string")).getText();
        data.setTitle(title);

        // 2. 썸네일 (URL에서 ID 추출하여 생성 - 가장 확실한 방법)
        // url 예시: https://www.youtube.com/watch?v=VIDEO_ID
        String videoId = url.split("v=")[1];
        if (videoId.contains("&")) videoId = videoId.split("&")[0];
        data.setImageUrl("https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg");

        // 3. 설명 (더보기 버튼 클릭 시도)
        try {
            // 설명창이 닫혀있다면 클릭 (선택적)
            // 유튜브 UI 구조상 복잡하므로, 메타데이터나 상단 설명만 가져오는 것이 안전
            WebElement descriptionEl = driver.findElement(By.cssSelector("#description-inline-expander"));
            data.setDescription(descriptionEl.getText());
        } catch (Exception e) {
            data.setDescription("설명 없음");
        }

        // 4. 댓글 수집 (스크롤 다운 필요)
        JavascriptExecutor js = (JavascriptExecutor) driver;
        // 댓글 섹션 로딩을 위해 스크롤 다운
        js.executeScript("window.scrollBy(0, 600)");
        Thread.sleep(1500);
        js.executeScript("window.scrollBy(0, 600)"); // 확실하게 로딩하기 위해 한번 더
        Thread.sleep(1500);

        List<WebElement> commentEls = driver.findElements(By.cssSelector("#content-text"));
        List<String> comments = new ArrayList<>();

        // 상위 5개 댓글만 수집
        for (int i = 0; i < Math.min(commentEls.size(), 5); i++) {
            comments.add(commentEls.get(i).getText());
        }
        data.setComments(comments);

        return data;
    }
}