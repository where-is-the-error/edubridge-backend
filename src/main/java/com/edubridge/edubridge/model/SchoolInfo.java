package com.edubridge.edubridge.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "school_infos") // 'school_infos'라는 별도 컬렉션에 저장
public class SchoolInfo {

    @Id
    private String id;

    private String userId;      // 어떤 유저의 정보인지 구분을 위한 ID
    private String officeCode;  // 시도교육청 코드 (예: B10)
    private String officeName;  // 시도교육청 이름 (예: 서울특별시교육청)
    private String schoolCode;  // 학교 코드
    private String schoolName;  // 학교 이름 (예: OO고등학교)
    private String grade;       // 학년
    private String classNm;     // 반
}