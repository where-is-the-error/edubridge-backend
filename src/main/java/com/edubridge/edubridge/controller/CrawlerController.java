package com.edubridge.edubridge.controller;

import com.edubridge.edubridge.model.User;
import com.edubridge.edubridge.repository.UserRepository;
import com.edubridge.edubridge.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CrawlerController {

    private final CrawlerService crawlerService;
    private final UserRepository userRepository; // 유저 조회를 위해 추가

    // 사용법: http://localhost:3000/api/crawl/youtube?keyword=중학교+2학년+수학
    // ⭐️ 로그인 상태에서(헤더에 토큰 포함) 요청해야 개인화 분석이 됩니다.
    @GetMapping("/api/crawl/youtube")
    public String startYoutubeCrawl(@RequestParam(defaultValue = "중학교 2학년 수학") String keyword,
                                    Authentication authentication) {
        try {
            // 1. 로그인된 사용자 정보 가져오기
            User user = null;
            if (authentication != null) {
                String userId = authentication.getName();
                user = userRepository.findById(userId).orElse(null);
            }

            // 2. 유저 정보가 없으면(비로그인) 기본 더미 유저 생성 (오류 방지)
            if (user == null) {
                user = new User();
                user.setNickname("학생");
                user.setGradeLevel("중학교");
                user.setGradeNumber(2);
                user.setSubjectPrimary("수학");
                user.setLevel(50); // 기본 레벨
            }

            // 3. 서비스에 유저 객체 전달
            int count = crawlerService.crawlYoutube(keyword, user);

            return String.format("크롤링 완료! '%s' 관련 영상 %d개를 분석하여 저장했습니다. (사용자: %s)",
                    keyword, count, user.getNickname());
        } catch (Exception e) {
            e.printStackTrace();
            return "크롤링 실패: " + e.getMessage();
        }
    }
}