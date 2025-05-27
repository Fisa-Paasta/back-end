package com.paasta.backend.service;

import com.paasta.backend.entity.User;
import com.paasta.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * 사용자 생성 (간단한 버전)
     */
    public User createUser(String employeeId, String password, String department, String userName) {
        if (userRepository.existsByEmployeeId(employeeId)) {
            throw new RuntimeException("이미 존재하는 사번입니다: " + employeeId);
        }

        User user = User.builder()
                .employeeId(employeeId)
                .password(password) // 실제로는 암호화 필요
                .department(department)
                .userName(userName)
                .role(User.Role.USER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("사용자 생성 완료: 사번={}, 이름={}", savedUser.getEmployeeId(), savedUser.getUserName());
        
        return savedUser;
    }

    /**
     * 관리자 계정 생성
     */
    public User createAdmin(String employeeId, String password, String department, String userName) {
        if (userRepository.existsByEmployeeId(employeeId)) {
            throw new RuntimeException("이미 존재하는 사번입니다: " + employeeId);
        }

        User admin = User.builder()
                .employeeId(employeeId)
                .password(password)
                .department(department)
                .userName(userName)
                .role(User.Role.ADMIN)
                .build();

        User savedAdmin = userRepository.save(admin);
        log.info("관리자 생성 완료: 사번={}, 이름={}", savedAdmin.getEmployeeId(), savedAdmin.getUserName());
        
        return savedAdmin;
    }

    /**
     * 사번으로 사용자 조회
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmployeeId(String employeeId) {
        return userRepository.findByEmployeeId(employeeId);
    }

    /**
     * 모든 사용자 조회
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 사용자 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean existsByEmployeeId(String employeeId) {
        return userRepository.existsByEmployeeId(employeeId);
    }

    /**
     * 간단한 로그인 검증 (임시)
     */
    @Transactional(readOnly = true)
    public Optional<User> validateLogin(String employeeId, String password) {
        Optional<User> userOpt = userRepository.findByEmployeeId(employeeId);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 실제로는 암호화된 비밀번호 비교 필요
            if (user.getPassword().equals(password)) {
                return userOpt;
            }
        }
        
        return Optional.empty();
    }
}