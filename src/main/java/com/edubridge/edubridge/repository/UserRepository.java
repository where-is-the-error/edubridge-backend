// src/main/java/com/edubridge/edubridge/repository/UserRepository.java

package com.edubridge.edubridge.repository;

import com.edubridge.edubridge.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

// MongoRepository를 상속받아 기본적인 CRUD 기능을 자동 제공받습니다.
// <User: 대상 Model 클래스, String: 해당 클래스의 @Id 타입>
public interface UserRepository extends MongoRepository<User, String> {

    // 이메일을 이용해 사용자를 찾는 사용자 정의 메소드 (로그인/중복 체크에 필요)
    Optional<User> findByEmail(String email);
}