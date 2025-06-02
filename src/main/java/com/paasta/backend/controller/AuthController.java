package com.paasta.backend.controller;

import com.paasta.backend.entity.User;
import com.paasta.backend.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class AuthController {

    private final UserService userService;
    
    // 상수 정의로 중복 문자열 제거
    private static final String ERROR_KEY = "error";
    private static final String INVALID_CREDENTIALS = "잘못된 사번 또는 비밀번호입니다.";
    private static final String ACCESS_DENIED = "권한이 없습니다.";
    private static final String LOGIN_ERROR = "로그인 처리 중 오류가 발생했습니다.";

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            log.info("로그인 시도: 사번={}, 부서={}", request.getId(), request.getDepartment());
            
            // 1. 사용자 정보 조회 및 비밀번호 검증
            Optional<User> userOpt = userService.validateLogin(request.getId(), request.getPassword());
            
            if (userOpt.isEmpty()) {
                return handleLoginError(INVALID_CREDENTIALS, request.getId());
            }
            
            User user = userOpt.get();
            
            // 2. 부서별 권한 검증
            if (!validateDepartmentAccess(user, request.getDepartment())) {
                return handleAccessDenied(user, request.getDepartment());
            }
            
            // 3. 로그인 성공 응답
            return handleLoginSuccess(user, request.getDepartment());
            
        } catch (Exception e) {
            log.error("로그인 처리 중 오류", e);
            return ResponseEntity.internalServerError().body(createErrorResponse(LOGIN_ERROR));
        }
    }

    private ResponseEntity<Map<String, Object>> handleLoginError(String message, String employeeId) {
        log.warn("로그인 실패: {} - 사번={}", message, employeeId);
        return ResponseEntity.badRequest().body(createErrorResponse(message));
    }

    private ResponseEntity<Map<String, Object>> handleAccessDenied(User user, String requestedDepartment) {
        log.warn("부서 권한 없음: 사번={}, 역할={}, 요청부서={}", 
                user.getEmployeeId(), user.getRole(), requestedDepartment);
        return ResponseEntity.badRequest().body(createErrorResponse(ACCESS_DENIED));
    }

    private ResponseEntity<Map<String, Object>> handleLoginSuccess(User user, String department) {
        Map<String, Object> response = new HashMap<>();
        response.put("token", "mock-jwt-token-" + user.getEmployeeId());
        response.put("name", user.getUserName());
        response.put("role", user.getRole().name().toLowerCase());
        response.put("employeeId", user.getEmployeeId());
        response.put("department", department);
        
        log.info("로그인 성공: 사번={}, 이름={}, 역할={}, 부서={}", 
                user.getEmployeeId(), user.getUserName(), user.getRole(), department);
        
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(ERROR_KEY, message);
        return errorResponse;
    }

    /**
     * 부서별 접근 권한 검증
     * - 관리자(ADMIN): IT인프라팀만 접근 가능
     * - 일반사용자(USER): IT인프라팀 제외한 모든 부서 접근 가능
     */
    private boolean validateDepartmentAccess(User user, String requestedDepartment) {
        log.info("🔍 부서 접근 권한 검증: 사용자={}, DB부서='{}', 요청부서='{}'", 
                user.getEmployeeId(), user.getDepartment(), requestedDepartment);
        
        // DB의 사용자 부서와 요청된 부서가 일치하는지 먼저 확인
        if (!user.getDepartment().equals(requestedDepartment)) {
            log.warn("❌ 부서 불일치 - 접근 거부");
            return false;
        }
        
        // IT인프라팀 여부 확인
        boolean isITInfraDept = isITInfrastructureDepartment(user.getDepartment());
        
        boolean hasAccess = (user.getRole() == User.Role.ADMIN) ? isITInfraDept : !isITInfraDept;
        
        log.info("🏁 권한 검증 결과: {}", hasAccess ? "✅ 허용" : "❌ 거부");
        return hasAccess;
    }

    private boolean isITInfrastructureDepartment(String department) {
        return "은행-IT인프라팀".equals(department);
    }

    @PostMapping("/verify-token")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                if (isValidToken(token)) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("valid", true);
                    return ResponseEntity.ok(response);
                }
            }
            
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("토큰 검증 중 오류", e);
            return ResponseEntity.status(401).build();
        }
    }

    private boolean isValidToken(String token) {
        return token.startsWith("mock-jwt-token-") || 
               "test-user-token".equals(token) || 
               "test-admin-token".equals(token);
    }

    @Data
    public static class LoginRequest {
        private String id;
        private String password;
        private String department;
    }
}