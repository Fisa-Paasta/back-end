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
@CrossOrigin(origins = {
    "https://paasta.store", 
    "https://www.paasta.store", 
    "https://api.paasta.store"
})
@Slf4j
public class SubmissionController {

    private final ApplicationService applicationService;
    
    // 상수 정의로 중복 문자열 제거
    private static final String VERSION_LITERAL = "version";

    @PostMapping("/submit-card")
    public ResponseEntity<String> submitCard(@RequestBody Map<String, Object> request) {
        try {
            log.info("🔍 신청서 제출 요청 받음");
            log.info("📋 Request 전체 내용: {}", request);
            
            // Map에서 데이터 추출
            String userId = (String) request.get("userId");
            String title = (String) request.get("title");
            String desc = (String) request.get("desc");
            @SuppressWarnings("unchecked")
            Map<String, Object> formDataSnapshot = (Map<String, Object>) request.get("formDataSnapshot");
            
            log.info("🔍 추출된 기본 데이터:");
            log.info("  - userId: {}", userId);
            log.info("  - title: {}", title);
            log.info("  - desc: {}", desc);
            log.info("  - formDataSnapshot: {}", formDataSnapshot);
            
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
        
        log.info("🔄 데이터 변환 시작...");
        
        setBasicInfo(appRequest, userId, title, desc, formData);
        setVmInfo(appRequest, formData);
        setK8sInfo(appRequest, formData);
        setResourcesInfo(appRequest, formData);
        setOsInfo(appRequest, formData);
        setFrontendInfo(appRequest, formData);
        setBackendInfo(appRequest, formData);
        setWebServerInfo(appRequest, formData);
        setDbInfo(appRequest, formData);
        
        logFinalApplicationRequest(appRequest);
        
        return appRequest;
    }
    
    private void setBasicInfo(ApplicationRequest appRequest, String userId, String title, String desc, Map<String, Object> formData) {
        appRequest.setEmployeeId(userId);
        appRequest.setTitle(title);
        appRequest.setDescription(desc);
        appRequest.setEnvType((String) formData.get("env"));
        log.info("  - 기본 정보 설정 완료: env={}", formData.get("env"));
    }
    
    private void setVmInfo(ApplicationRequest appRequest, Map<String, Object> formData) {
        @SuppressWarnings("unchecked")
        Map<String, Object> vmData = (Map<String, Object>) formData.get("vm");
        if (vmData != null) {
            log.info("  - VM 데이터 처리 중: {}", vmData);
            ApplicationRequest.VmConfig vm = new ApplicationRequest.VmConfig();
            
            if ("iaas".equals(formData.get("env"))) {
                vm.setHostname((String) vmData.get("hostname"));
                vm.setUsername((String) vmData.get("username"));
                vm.setEnvironment((String) vmData.get("environment"));
            }
            
            vm.setEc2Type((String) vmData.get("ec2Type"));
            vm.setEbsType((String) vmData.get("ebsType"));
            vm.setEbsSize((String) vmData.get("ebsSize"));
            
            appRequest.setVm(vm);
            log.info("    ✅ VM 정보 설정 완료 (env: {}, ec2Type: {}, ebsType: {}, ebsSize: {})", 
                    formData.get("env"), vm.getEc2Type(), vm.getEbsType(), vm.getEbsSize());
        }
    }
    
    private void setK8sInfo(ApplicationRequest appRequest, Map<String, Object> formData) {
        @SuppressWarnings("unchecked")
        Map<String, Object> k8sData = (Map<String, Object>) formData.get("k8s");
        if (k8sData != null && "paas".equals(formData.get("env"))) {
            log.info("  - K8s 데이터 처리 중: {}", k8sData);
            ApplicationRequest.K8sConfig k8s = new ApplicationRequest.K8sConfig();
            k8s.setType((String) k8sData.get("type"));
            k8s.setNamespace((String) k8sData.get("namespace"));
            k8s.setNode((String) k8sData.get("node"));
            appRequest.setK8s(k8s);
            log.info("    ✅ K8s 정보 설정 완료");
        }
    }
    
    private void setResourcesInfo(ApplicationRequest appRequest, Map<String, Object> formData) {
        @SuppressWarnings("unchecked")
        Map<String, Object> resourcesData = (Map<String, Object>) formData.get("resources");
        if (resourcesData != null) {
            log.info("  - 자원 데이터 처리 중: {}", resourcesData);
            ApplicationRequest.ResourcesConfig resources = new ApplicationRequest.ResourcesConfig();
            resources.setCpu((String) resourcesData.get("cpu"));
            resources.setRam((String) resourcesData.get("ram"));
            resources.setDisk((String) resourcesData.get("disk"));
            appRequest.setResources(resources);
            log.info("    ✅ 자원 정보 설정 완료");
        }
    }
    
    private void setOsInfo(ApplicationRequest appRequest, Map<String, Object> formData) {
        @SuppressWarnings("unchecked")
        Map<String, Object> osData = (Map<String, Object>) formData.get("os");
        if (osData != null) {
            log.info("  - OS 데이터 처리 중: {}", osData);
            ApplicationRequest.OsConfig os = new ApplicationRequest.OsConfig();
            os.setName((String) osData.get("name"));
            os.setVersion((String) osData.get(VERSION_LITERAL));
            appRequest.setOs(os);
            log.info("    ✅ OS 정보 설정 완료");
        }
    }
    
    private void setFrontendInfo(ApplicationRequest appRequest, Map<String, Object> formData) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> frontendItemsData = (List<Map<String, Object>>) formData.get("frontendItems");
        if (frontendItemsData != null && !frontendItemsData.isEmpty()) {
            log.info("  - 프론트엔드 데이터 처리 중: {}", frontendItemsData);
            List<ApplicationRequest.FrontendItem> frontendItems = frontendItemsData.stream()
                .map(this::createFrontendItem)
                .toList();
            appRequest.setFrontendItems(frontendItems);
            appRequest.setFrontendDomain((String) formData.get("frontendDomain"));
            log.info("    ✅ 프론트엔드 정보 설정 완료: {} 개 항목", frontendItems.size());
        }
    }
    
    private ApplicationRequest.FrontendItem createFrontendItem(Map<String, Object> item) {
        ApplicationRequest.FrontendItem frontendItem = new ApplicationRequest.FrontendItem();
        frontendItem.setId(getLongValue(item.get("id")));
        frontendItem.setFramework((String) item.get("framework"));
        frontendItem.setVersion((String) item.get(VERSION_LITERAL));
        return frontendItem;
    }
    
    private void setBackendInfo(ApplicationRequest appRequest, Map<String, Object> formData) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> backendItemsData = (List<Map<String, Object>>) formData.get("backendItems");
        if (backendItemsData != null && !backendItemsData.isEmpty()) {
            log.info("  - 백엔드 데이터 처리 중: {}", backendItemsData);
            List<ApplicationRequest.BackendItem> backendItems = backendItemsData.stream()
                .map(this::createBackendItem)
                .toList();
            appRequest.setBackendItems(backendItems);
            appRequest.setApiDomain((String) formData.get("apiDomain"));
            
            @SuppressWarnings("unchecked")
            List<String> apiPaths = (List<String>) formData.get("apiPaths");
            if (apiPaths != null) {
                appRequest.setApiPaths(apiPaths);
            }
            log.info("    ✅ 백엔드 정보 설정 완료: {} 개 항목", backendItems.size());
        }
    }
    
    private ApplicationRequest.BackendItem createBackendItem(Map<String, Object> item) {
        ApplicationRequest.BackendItem backendItem = new ApplicationRequest.BackendItem();
        backendItem.setId(getLongValue(item.get("id")));
        backendItem.setLanguage((String) item.get("language"));
        backendItem.setLanguageVersion((String) item.get("languageVersion"));
        backendItem.setFramework((String) item.get("framework"));
        backendItem.setFrameworkVersion((String) item.get("frameworkVersion"));
        return backendItem;
    }
    
    private void setWebServerInfo(ApplicationRequest appRequest, Map<String, Object> formData) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> webServerItemsData = (List<Map<String, Object>>) formData.get("webServerItems");
        if (webServerItemsData != null && !webServerItemsData.isEmpty()) {
            log.info("  - 웹서버 데이터 처리 중: {}", webServerItemsData);
            List<ApplicationRequest.WebServerItem> webServerItems = webServerItemsData.stream()
                .map(this::createWebServerItem)
                .toList();
            appRequest.setWebServerItems(webServerItems);
            log.info("    ✅ 웹서버 정보 설정 완료: {} 개 항목", webServerItems.size());
        }
    }
    
    private ApplicationRequest.WebServerItem createWebServerItem(Map<String, Object> item) {
        ApplicationRequest.WebServerItem webServerItem = new ApplicationRequest.WebServerItem();
        webServerItem.setId(getLongValue(item.get("id")));
        webServerItem.setServer((String) item.get("server"));
        webServerItem.setVersion((String) item.get(VERSION_LITERAL));
        return webServerItem;
    }
    
    private void setDbInfo(ApplicationRequest appRequest, Map<String, Object> formData) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dbItemsData = (List<Map<String, Object>>) formData.get("dbItems");
        if (dbItemsData != null && !dbItemsData.isEmpty()) {
            log.info("  - DB 데이터 처리 중: {}", dbItemsData);
            List<ApplicationRequest.DbItem> dbItems = dbItemsData.stream()
                .map(this::createDbItem)
                .toList();
            appRequest.setDbItems(dbItems);
            log.info("    ✅ DB 정보 설정 완료: {} 개 항목", dbItems.size());
        }
    }
    
    private ApplicationRequest.DbItem createDbItem(Map<String, Object> item) {
        ApplicationRequest.DbItem dbItem = new ApplicationRequest.DbItem();
        dbItem.setId(getLongValue(item.get("id")));
        dbItem.setType((String) item.get("type"));
        dbItem.setName((String) item.get("name"));
        dbItem.setVersion((String) item.get(VERSION_LITERAL));
        dbItem.setSize((String) item.get("size"));
        return dbItem;
    }
    
    private void logFinalApplicationRequest(ApplicationRequest appRequest) {
        log.info("🔄 변환된 ApplicationRequest 최종 확인:");
        log.info("  - env: {}", appRequest.getEnvType());
        log.info("  - title: {}", appRequest.getTitle());
        log.info("  - 프론트엔드 항목: {}", appRequest.getFrontendItems() != null ? appRequest.getFrontendItems().size() : 0);
        log.info("  - 백엔드 항목: {}", appRequest.getBackendItems() != null ? appRequest.getBackendItems().size() : 0);
        log.info("  - 웹서버 항목: {}", appRequest.getWebServerItems() != null ? appRequest.getWebServerItems().size() : 0);
        log.info("  - DB 항목: {}", appRequest.getDbItems() != null ? appRequest.getDbItems().size() : 0);
    }
    
    // ✅ ID 값 안전하게 변환하는 헬퍼 메서드
    private Long getLongValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String stringValue) {
            try {
                return Long.parseLong(stringValue);
            } catch (NumberFormatException e) {
                log.warn("ID 값 변환 실패: {}", value);
                return null;
            }
        }
        return null;
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