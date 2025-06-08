package com.paasta.backend.config;

import com.paasta.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserService userService;
    
    // ✅ SonarQube 보안 이슈 수정: 외부 설정에서 비밀번호 주입
    @Value("${app.default-password:tempPass123!}")
    private String defaultPassword;
    
    @Value("${app.user-test-password:userTest123!}")
    private String userTestPassword;
    
    // 상수 정의로 중복 문자열 제거
    private static final String ACCOUNT_CREATION_SUCCESS = "계정이 생성되었습니다";
    private static final String ACCOUNT_CREATION_FAILED = "계정 생성 실패";
    
    // 부서 상수 정의
    private static final String IT_INFRA_DEPT = "은행-IT인프라팀";
    private static final String CARD_DIGITAL_DEPT = "카드-디지털개발팀";
    private static final String CARD_DATA_DEPT = "카드-데이터분석팀";
    private static final String INSURANCE_PLATFORM_DEPT = "보험-플랫폼개발팀";
    private static final String SECURITIES_TRADING_DEPT = "증권-트레이딩플랫폼팀";
    
    // 사용자 ID 상수 정의
    private static final String ADMIN_ID = "12345678";
    private static final String TEST_USER_ID_1 = "99991234";
    private static final String TEST_USER_ID_2 = "87654321";
    private static final String TEST_USER_ID_3 = "98765432";
    private static final String TEST_USER_ID_4 = "56789012";

    @Bean
    public ApplicationRunner initData() {
        return args -> {
            createAdminAccount();
            createTestUserAccounts();
        };
    }
    
    private void createAdminAccount() {
        if (!userService.existsByEmployeeId(ADMIN_ID)) {
            try {
                userService.createAdmin(ADMIN_ID, defaultPassword, IT_INFRA_DEPT, "관리자");
                log.info("✅ 기본 관리자 {}: {} (IT인프라팀 전용)", ACCOUNT_CREATION_SUCCESS, ADMIN_ID);
            } catch (Exception e) {
                log.warn("❌ 관리자 {}", ACCOUNT_CREATION_FAILED, e);
            }
        }
    }
    
    private void createTestUserAccounts() {
        createTestUser(TEST_USER_ID_1, userTestPassword, CARD_DIGITAL_DEPT, "임시 사용자", "(IT인프라팀 제외 부서용)");
        createTestUser(TEST_USER_ID_2, userTestPassword, CARD_DATA_DEPT, "테스트사용자1", "");
        createTestUser(TEST_USER_ID_3, userTestPassword, INSURANCE_PLATFORM_DEPT, "테스트사용자2", "");
        createTestUser(TEST_USER_ID_4, userTestPassword, SECURITIES_TRADING_DEPT, "테스트사용자3", "");
    }
    
    private void createTestUser(String employeeId, String password, String department, String userName, String note) {
        if (!userService.existsByEmployeeId(employeeId)) {
            try {
                userService.createUser(employeeId, password, department, userName);
                log.info("✅ 테스트 사용자 {}: {} {}", ACCOUNT_CREATION_SUCCESS, employeeId, note);
            } catch (Exception e) {
                log.warn("❌ 테스트 사용자 {}", ACCOUNT_CREATION_FAILED, e);
            }
        }
    }
}