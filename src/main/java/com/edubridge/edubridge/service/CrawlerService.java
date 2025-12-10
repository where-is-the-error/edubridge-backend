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

@Service
@RequiredArgsConstructor
public class CrawlerService {

    private final CrawledDataRepository crawledDataRepository;
    private final GeminiService geminiService;

    public int crawlYoutube(String keyword) {
        // 1. 크롬 드라이버 자동 설정
        WebDriverManager.chromedriver().setup();

        // 2. 브라우저 옵션 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // 브라우저 창 숨기기 (디버깅 시 주석 처리)
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");
        // 유튜브는 봇 탐지를 피하기 위해 User-Agent 설정 필수
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);
        int savedCount = 0;

        try {
            // 3. 검색 결과 페이지 이동
            String searchUrl = "https://www.youtube.com/results?search_query=" + keyword;
            System.out.println("Crawling URL: " + searchUrl);
            driver.get(searchUrl);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            // 영상 리스트가 로딩될 때까지 대기
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("ytd-video-renderer")));

            // 4. 영상 링크 수집 (상위 7개)
            List<WebElement> videoElements = driver.findElements(By.cssSelector("ytd-video-renderer"));
            List<String> videoLinks = new ArrayList<>();

            for (int i = 0; i < Math.min(videoElements.size(), 7); i++) {
                try {
                    WebElement titleEl = videoElements.get(i).findElement(By.id("video-title"));
                    String link = titleEl.getAttribute("href");

                    // ⭐️ [수정됨] 링크가 존재하고, 'shorts'가 포함되지 않은 경우에만 추가
                    if (link != null && !link.contains("shorts")) {
                        videoLinks.add(link);
                    }

                } catch (Exception e) {
                    continue;
                }
            }

            // 5. 각 영상 상세 크롤링 & 분석
            for (String link : videoLinks) {
                try {
                    CrawledData data = processSingleVideo(driver, link);
                    if (data != null) {

                        // ⭐️ [수정] 임시 사용자 객체 생성 또는 DB 조회 (실제로는 로그인된 사용자 정보를 써야 함)
                        User dummyUser = new User();
                        dummyUser.setNickname("학생");
                        dummyUser.setGradeLevel("중학교");
                        dummyUser.setGradeNumber(2);
                        dummyUser.setSubjectPrimary("수학");

                        // AI 분석 수행 (user 객체 전달)
                        CrawledData analyzedData = geminiService.analyzeYoutubeVideo(data, dummyUser);

                        // DB 저장
                        crawledDataRepository.save(analyzedData);
                        savedCount++;
                        System.out.println("Saved & Analyzed: " + data.getTitle());
                    }
                } catch (Exception e) {
                    System.err.println("Failed to process video: " + link + " / Error: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit(); // 브라우저 종료
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