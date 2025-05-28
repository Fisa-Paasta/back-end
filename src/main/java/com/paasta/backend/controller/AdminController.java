package com.paasta.backend.controller;

import com.paasta.backend.dto.ApplicationResponse;
import com.paasta.backend.dto.StatusUpdateRequest;
import com.paasta.backend.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class AdminController {

    private final ApplicationService applicationService;

    // ✅ 관리자는 삭제된 것 포함 모든 신청서 조회
    @GetMapping("/applications")
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {
        try {
            List<ApplicationResponse> applications = applicationService.getAllApplicationsIncludingDeleted();
            log.info("✅ 관리자 전체 신청서 조회 (삭제 포함): 건수={}", applications.size());
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            log.error("❌ 관리자 전체 신청서 조회 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/applications/status/{status}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByStatus(
            @PathVariable("status") String status) {
        try {
            List<ApplicationResponse> applications = applicationService.getApplicationsByStatus(status);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            log.error("상태별 신청서 조회 실패: 상태={}", status, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/applications/{id}/status")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable("id") Long id,
            @RequestBody StatusUpdateRequest request) {
        try {
            log.info("신청서 상태 업데이트 요청: ID={}, 상태={}, 승인자={}", 
                    id, request.getStatus(), request.getApproverEmployeeId());
            
            ApplicationResponse response = applicationService.updateApplicationStatus(
                    id, 
                    request.getStatus(), 
                    request.getComments(),
                    request.getApproverEmployeeId()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("신청서 상태 업데이트 실패: ID={}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationForAdmin(
            @PathVariable("id") Long id) {
        try {
            ApplicationResponse application = applicationService.getApplicationById(id);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            log.error("관리자용 신청서 상세 조회 실패: ID={}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
}