package com.paasta.backend.controller;

import com.paasta.backend.dto.ApplicationRequest;
import com.paasta.backend.dto.ApplicationResponse;
import com.paasta.backend.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.paasta.backend.dto.ApplicationUpdateRequest;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(@RequestBody ApplicationRequest request) {
        try {
            log.info("신청서 생성 요청: 사번={}, 제목={}", request.getEmployeeId(), request.getTitle());
            ApplicationResponse response = applicationService.createApplication(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("신청서 생성 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ @PathVariable 명시
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByEmployee(
            @PathVariable("employeeId") String employeeId) {
        try {
            List<ApplicationResponse> applications = applicationService.getApplicationsByEmployeeId(employeeId);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            log.error("신청서 목록 조회 실패: 사번={}", employeeId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ @PathVariable 명시
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplication(@PathVariable("id") Long id) {
        try {
            ApplicationResponse application = applicationService.getApplicationById(id);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            log.error("신청서 상세 조회 실패: ID={}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ @PathVariable 명시
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteApplication(@PathVariable("id") Long id) {
        try {
            applicationService.deleteApplication(id);
            return ResponseEntity.ok("신청서가 삭제되었습니다.");
        } catch (Exception e) {
            log.error("신청서 삭제 실패: ID={}", id, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateApplication(
        @PathVariable("id") Long id,
        @RequestBody ApplicationUpdateRequest request
    ) {
        try {
            applicationService.updateApplicationContent(id, request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("신청서 수정 실패: ID={}, 에러={}", id, e.getMessage());
            return ResponseEntity.badRequest().body("신청서 수정 실패: " + e.getMessage());
        }
    }
    
}