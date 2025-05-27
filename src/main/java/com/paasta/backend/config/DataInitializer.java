package com.paasta.backend.config;

import com.paasta.backend.entity.User;
import com.paasta.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserService userService;

    @Bean
    public ApplicationRunner initData() {
        return args -> {
            // 관리자 계정 생성 (존재하지 않는 경우에만)
            if (!userService.existsByEmployeeId("admin001")) {
                try {
                    userService.createAdmin("admin001", "admin123", "IT인프라팀", "관리자");
                    log.info("기본 관리자 계정이 생성되었습니다: admin001");
                } catch (Exception e) {
                    log.warn("관리자 계정 생성 실패", e);
                }
            }

            // 테스트 사용자 계정 생성
            if (!userService.existsByEmployeeId("user0001")) {
                try {
                    userService.createUser("user0001", "user123", "개발팀", "테스트사용자1");
                    log.info("테스트 사용자 계정이 생성되었습니다: user0001");
                } catch (Exception e) {
                    log.warn("테스트 사용자 계정 생성 실패", e);
                }
            }

            if (!userService.existsByEmployeeId("user0002")) {
                try {
                    userService.createUser("user0002", "user123", "기획팀", "테스트사용자2");
                    log.info("테스트 사용자 계정이 생성되었습니다: user0002");
                } catch (Exception e) {
                    log.warn("테스트 사용자 계정 생성 실패", e);
                }
            }
        };
    }
}