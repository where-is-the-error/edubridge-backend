package com.edubridge.edubridge.controller;

import com.edubridge.edubridge.model.CrawledData;
import com.edubridge.edubridge.repository.CrawledDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/crawled-data")
@RequiredArgsConstructor
public class CrawledDataController {

    private final CrawledDataRepository crawledDataRepository;

    // 저장된 모든 크롤링 데이터 조회
    @GetMapping
    public ResponseEntity<List<CrawledData>> getAllCrawledData() {
        // MongoDB에서 모든 데이터를 가져옵니다.
        List<CrawledData> dataList = crawledDataRepository.findAll();
        return ResponseEntity.ok(dataList);
    }
}