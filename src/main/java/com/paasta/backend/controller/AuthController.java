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

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            log.info("로그인 시도: 사번={}", request.getId());
            
            Optional<User> userOpt = userService.validateLogin(
                    request.getId(),
                    request.getPassword()
            );
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                Map<String, Object> response = new HashMap<>();
                response.put("token", "mock-jwt-token-" + user.getEmployeeId());
                response.put("name", user.getUserName());
                response.put("role", user.getRole().name().toLowerCase());
                response.put("employeeId", user.getEmployeeId());
                response.put("department", user.getDepartment());
                
                log.info("로그인 성공: 사번={}, 이름={}, 역할={}", 
                        user.getEmployeeId(), user.getUserName(), user.getRole());
                
                return ResponseEntity.ok(response);
            } else {
                log.warn("로그인 실패: 사번={}", request.getId());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid credentials");
                return ResponseEntity.badRequest().body(errorResponse);
            }
        } catch (Exception e) {
            log.error("로그인 처리 중 오류", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/verify-token")
    public ResponseEntity<Map<String, Object>> verifyToken(
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                if (token.startsWith("mock-jwt-token-") || 
                    token.equals("test-user-token") || 
                    token.equals("test-admin-token")) {
                    
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

    @Data
    public static class LoginRequest {
        private String id;
        private String password;
        private String department;
    }
}