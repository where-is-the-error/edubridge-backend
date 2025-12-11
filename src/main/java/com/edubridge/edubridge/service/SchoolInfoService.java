package com.edubridge.edubridge.service;

import com.edubridge.edubridge.model.SchoolInfo;
import com.edubridge.edubridge.model.User;
import com.edubridge.edubridge.repository.SchoolInfoRepository;
import com.edubridge.edubridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchoolInfoService {

    private final SchoolInfoRepository schoolInfoRepository;
    private final UserRepository userRepository;

    // ⭐️ [수정] userId를 직접 받아서 처리하도록 변경
    public SchoolInfo saveSchoolInfo(String userId, SchoolInfo info) {

        // 1. 유저 존재 확인 (선택 사항이지만 안전을 위해 권장)
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        // 2. 이미 저장된 정보가 있는지 확인
        SchoolInfo existingInfo = schoolInfoRepository.findByUserId(userId)
                .orElse(new SchoolInfo());

        // 3. 정보 업데이트
        existingInfo.setUserId(userId);
        existingInfo.setOfficeCode(info.getOfficeCode());
        existingInfo.setOfficeName(info.getOfficeName());
        existingInfo.setSchoolCode(info.getSchoolCode());
        existingInfo.setSchoolName(info.getSchoolName());
        existingInfo.setGrade(info.getGrade());
        existingInfo.setClassNm(info.getClassNm());

        // 4. 저장
        return schoolInfoRepository.save(existingInfo);
    }

    // ⭐️ [수정] userId로 바로 조회 (이메일 불필요)
    public SchoolInfo getSchoolInfoByUserId(String userId) {
        return schoolInfoRepository.findByUserId(userId)
                .orElse(null);
    }
}