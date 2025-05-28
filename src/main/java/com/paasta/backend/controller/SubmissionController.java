package com.paasta.backend.controller;

import com.paasta.backend.dto.ApplicationRequest;
import com.paasta.backend.dto.ApplicationResponse;
import com.paasta.backend.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class SubmissionController {

    private final ApplicationService applicationService;

    @PostMapping("/submit-card")
    public ResponseEntity<String> submitCard(@RequestBody Map<String, Object> request) {
        try {
            log.info("신청서 제출 요청 받음");
            log.info("Request 내용: {}", request);
            
            // Map에서 데이터 추출
            String userId = (String) request.get("userId");
            String title = (String) request.get("title");
            String desc = (String) request.get("desc");
            Map<String, Object> formDataSnapshot = (Map<String, Object>) request.get("formDataSnapshot");
            
            // ApplicationRequest 생성
            ApplicationRequest appRequest = convertMapToApplicationRequest(userId, title, desc, formDataSnapshot);
            
            // DB에 저장
            ApplicationResponse response = applicationService.createApplication(appRequest);
            
            log.info("✅ 신청서 DB 저장 성공: ID={}, 사번={}", response.getId(), response.getEmployeeId());
            
            return ResponseEntity.ok("신청서 접수 완료 - DB 저장됨 (ID: " + response.getId() + ")");
        } catch (Exception e) {
            log.error("❌ 신청서 제출 실패", e);
            return ResponseEntity.badRequest().body("신청서 제출 실패: " + e.getMessage());
        }
    }
    
    private ApplicationRequest convertMapToApplicationRequest(String userId, String title, String desc, Map<String, Object> formData) {
        ApplicationRequest appRequest = new ApplicationRequest();
        
        appRequest.setEmployeeId(userId);
        appRequest.setTitle(title);
        appRequest.setDescription(desc);
        appRequest.setEnvType((String) formData.get("env"));
        
        // VM 정보 (IaaS용)
        Map<String, Object> vmData = (Map<String, Object>) formData.get("vm");
        if (vmData != null && "iaas".equals(formData.get("env"))) {
            ApplicationRequest.VmConfig vm = new ApplicationRequest.VmConfig();
            vm.setHostname((String) vmData.get("hostname"));
            vm.setUsername((String) vmData.get("username"));
            vm.setEnvironment((String) vmData.get("environment"));
            vm.setEc2Type((String) vmData.get("ec2Type"));
            vm.setEbsType((String) vmData.get("ebsType"));
            vm.setEbsSize((String) vmData.get("ebsSize"));
            appRequest.setVm(vm);
        }
        
        // K8s 정보 (PaaS용)
        Map<String, Object> k8sData = (Map<String, Object>) formData.get("k8s");
        if (k8sData != null && "paas".equals(formData.get("env"))) {
            ApplicationRequest.K8sConfig k8s = new ApplicationRequest.K8sConfig();
            k8s.setType((String) k8sData.get("type"));
            k8s.setNamespace((String) k8sData.get("namespace"));
            k8s.setNode((String) k8sData.get("node"));
            appRequest.setK8s(k8s);
        }
        
        // 자원 정보
        Map<String, Object> resourcesData = (Map<String, Object>) formData.get("resources");
        if (resourcesData != null) {
            ApplicationRequest.ResourcesConfig resources = new ApplicationRequest.ResourcesConfig();
            resources.setCpu((String) resourcesData.get("cpu"));
            resources.setRam((String) resourcesData.get("ram"));
            resources.setDisk((String) resourcesData.get("disk"));
            appRequest.setResources(resources);
        }
        
        // OS 정보
        Map<String, Object> osData = (Map<String, Object>) formData.get("os");
        if (osData != null) {
            ApplicationRequest.OsConfig os = new ApplicationRequest.OsConfig();
            os.setName((String) osData.get("name"));
            os.setVersion((String) osData.get("version"));
            appRequest.setOs(os);
        }
        
        log.info("🔄 변환된 ApplicationRequest: env={}, title={}", appRequest.getEnvType(), appRequest.getTitle());
        
        return appRequest;
    }

    @GetMapping("/submitted-cards")
    public ResponseEntity<List<Object>> getSubmittedCards() {
        try {
            // 빈 리스트 반환 (기존 로컬 상태 사용)
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            log.error("제출된 신청서 목록 조회 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }
}