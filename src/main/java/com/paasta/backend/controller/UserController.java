package com.paasta.backend.controller;

import com.paasta.backend.entity.User;
import com.paasta.backend.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        try {
            User user = userService.createUser(
                    request.getEmployeeId(),
                    request.getPassword(),
                    request.getDepartment(),
                    request.getUserName()
            );
            return ResponseEntity.ok(UserResponse.from(user));
        } catch (Exception e) {
            log.error("사용자 생성 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/admin")
    public ResponseEntity<UserResponse> createAdmin(@RequestBody CreateUserRequest request) {
        try {
            User admin = userService.createAdmin(
                    request.getEmployeeId(),
                    request.getPassword(),
                    request.getDepartment(),
                    request.getUserName()
            );
            return ResponseEntity.ok(UserResponse.from(admin));
        } catch (Exception e) {
            log.error("관리자 생성 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ @PathVariable 명시
    @GetMapping("/{employeeId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable("employeeId") String employeeId) {
        Optional<User> userOpt = userService.findByEmployeeId(employeeId);
        
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(UserResponse.from(userOpt.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponse> responses = users.stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userService.validateLogin(
                request.getEmployeeId(),
                request.getPassword()
        );
        
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(UserResponse.from(userOpt.get()));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Data
    public static class CreateUserRequest {
        private String employeeId;
        private String password;
        private String department;
        private String userName;
    }

    @Data
    public static class LoginRequest {
        private String employeeId;
        private String password;
    }

    @Data
    public static class UserResponse {
        private String employeeId;
        private String department;
        private String userName;
        private String role;

        public static UserResponse from(User user) {
            UserResponse response = new UserResponse();
            response.setEmployeeId(user.getEmployeeId());
            response.setDepartment(user.getDepartment());
            response.setUserName(user.getUserName());
            response.setRole(user.getRole().name());
            return response;
        }
    }
}