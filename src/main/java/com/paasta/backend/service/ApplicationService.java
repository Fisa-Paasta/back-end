package com.paasta.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paasta.backend.dto.ApplicationRequest;
import com.paasta.backend.dto.ApplicationResponse;
import com.paasta.backend.entity.Application;
import com.paasta.backend.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.paasta.backend.dto.ApplicationUpdateRequest;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ObjectMapper objectMapper;
    
    // 상수 정의로 중복 문자열 제거
    private static final String APPLICATION_NOT_FOUND_MSG = "신청서를 찾을 수 없습니다: ID=";

    /**
     * 신청서 생성 (중복 확인 및 상세 로깅 추가)
     */
    public ApplicationResponse createApplication(ApplicationRequest request) {
        try {
            logApplicationRequest(request);
            checkDuplicateApplication(request);
            
            Application application = buildApplicationFromRequest(request);
            setVmInformation(application, request);
            setK8sInformation(application, request);
            setResourceInformation(application, request);
            setOsInformation(application, request);
            setJsonData(application, request);

            Application savedApplication = applicationRepository.save(application);
            log.info("✅ 신청서 DB 저장 성공: ID={}, 사번={}, 제목={}", 
                    savedApplication.getId(), savedApplication.getEmployeeId(), savedApplication.getTitle());
            
            return ApplicationResponse.from(savedApplication);

        } catch (ApplicationServiceException e) {
            log.error("❌ 신청서 생성 중 알려진 오류 발생", e);
            throw e;
        } catch (Exception e) {
            log.error("❌ 신청서 생성 중 예상치 못한 오류 발생", e);
            throw new ApplicationServiceException("신청서 생성 중 오류가 발생했습니다.", e);
        }
    }

    private void logApplicationRequest(ApplicationRequest request) {
        log.info("🔍 신청서 생성 요청 받음:");
        log.info("  - 사번: {}", request.getEmployeeId());
        log.info("  - 제목: {}", request.getTitle());
        log.info("  - 환경타입: {}", request.getEnvType());
        log.info("  - 프론트엔드 항목 수: {}", request.getFrontendItems() != null ? request.getFrontendItems().size() : 0);
        log.info("  - 백엔드 항목 수: {}", request.getBackendItems() != null ? request.getBackendItems().size() : 0);
        log.info("  - 웹서버 항목 수: {}", request.getWebServerItems() != null ? request.getWebServerItems().size() : 0);
        log.info("  - DB 항목 수: {}", request.getDbItems() != null ? request.getDbItems().size() : 0);
    }

    private void checkDuplicateApplication(ApplicationRequest request) {
        List<Application> recentApplications = applicationRepository
                .findByEmployeeIdAndTitleAndCreatedAtAfter(
                    request.getEmployeeId(), 
                    request.getTitle(),
                    LocalDateTime.now().minusMinutes(5)
                );
        
        if (!recentApplications.isEmpty()) {
            log.warn("중복 신청 감지: 사번={}, 제목={}", request.getEmployeeId(), request.getTitle());
            throw new ApplicationServiceException("동일한 신청서가 이미 제출되었습니다.");
        }
    }

    private Application buildApplicationFromRequest(ApplicationRequest request) {
        return Application.builder()
                .employeeId(request.getEmployeeId())
                .title(request.getTitle())
                .description(request.getDescription())
                .envType(request.getEnvType())
                .status(Application.Status.RECEIVED)
                .build();
    }

    private void setVmInformation(Application application, ApplicationRequest request) {
        if (request.getVm() != null) {
            log.info("  - VM 정보 설정 중...");
            
            // IaaS 전용 필드
            if ("iaas".equals(request.getEnvType())) {
                application.setVmHostname(request.getVm().getHostname());
                application.setVmUsername(request.getVm().getUsername());
                application.setVmEnvironment(request.getVm().getEnvironment());
            }
            
            // IaaS + PaaS EKS 공통 필드 (EC2/EBS 정보)
            application.setVmEc2Type(request.getVm().getEc2Type());
            application.setVmEbsType(request.getVm().getEbsType());
            application.setVmEbsSize(request.getVm().getEbsSize());
            
            log.info("    ✅ VM 정보 설정 완료 (ec2Type: {}, ebsType: {}, ebsSize: {})", 
                    request.getVm().getEc2Type(), request.getVm().getEbsType(), request.getVm().getEbsSize());
        }
    }

    private void setK8sInformation(Application application, ApplicationRequest request) {
        if (request.getK8s() != null) {
            log.info("  - K8s 정보 설정 중...");
            application.setK8sType(request.getK8s().getType());
            application.setK8sNamespace(request.getK8s().getNamespace());
            application.setK8sNodeCount(request.getK8s().getNode());
            log.info("    ✅ K8s 정보 설정 완료");
        }
    }

    private void setResourceInformation(Application application, ApplicationRequest request) {
        if (request.getResources() != null) {
            log.info("  - 자원 정보 설정 중...");
            application.setResourceCpu(request.getResources().getCpu());
            application.setResourceRam(request.getResources().getRam());
            application.setResourceDisk(request.getResources().getDisk());
            log.info("    ✅ 자원 정보 설정 완료");
        }
    }

    private void setOsInformation(Application application, ApplicationRequest request) {
        if (request.getOs() != null) {
            log.info("  - OS 정보 설정 중...");
            application.setOsName(request.getOs().getName());
            application.setOsVersion(request.getOs().getVersion());
            log.info("    ✅ OS 정보 설정 완료");
        }
    }

    private void setJsonData(Application application, ApplicationRequest request) {
        setFrontendJsonData(application, request);
        setBackendJsonData(application, request);
        setWebServerJsonData(application, request);
        setDatabaseJsonData(application, request);
    }

    private void setFrontendJsonData(Application application, ApplicationRequest request) {
        if (request.getFrontendItems() != null && !request.getFrontendItems().isEmpty()) {
            try {
                log.info("  - 프론트엔드 항목 JSON 변환 중: {}", request.getFrontendItems());
                String frontendJson = objectMapper.writeValueAsString(request.getFrontendItems());
                application.setFrontendItems(frontendJson);
                application.setFrontendDomain(request.getFrontendDomain());
                log.info("    ✅ 프론트엔드 JSON 변환 성공: {}", frontendJson);
            } catch (JsonProcessingException e) {
                log.error("❌ 프론트엔드 JSON 변환 실패", e);
                throw new ApplicationServiceException("프론트엔드 데이터 변환 중 오류가 발생했습니다.", e);
            }
        } else {
            log.info("  - 프론트엔드 항목 없음 또는 비어있음");
        }
    }

    private void setBackendJsonData(Application application, ApplicationRequest request) {
        if (request.getBackendItems() != null && !request.getBackendItems().isEmpty()) {
            try {
                log.info("  - 백엔드 항목 JSON 변환 중: {}", request.getBackendItems());
                String backendJson = objectMapper.writeValueAsString(request.getBackendItems());
                application.setBackendItems(backendJson);
                application.setApiDomain(request.getApiDomain());
                
                if (request.getApiPaths() != null && !request.getApiPaths().isEmpty()) {
                    String apiPathsJson = objectMapper.writeValueAsString(request.getApiPaths());
                    application.setApiPaths(apiPathsJson);
                    log.info("    ✅ API 경로 JSON 변환 성공: {}", apiPathsJson);
                }
                log.info("    ✅ 백엔드 JSON 변환 성공: {}", backendJson);
            } catch (JsonProcessingException e) {
                log.error("❌ 백엔드 JSON 변환 실패", e);
                throw new ApplicationServiceException("백엔드 데이터 변환 중 오류가 발생했습니다.", e);
            }
        } else {
            log.info("  - 백엔드 항목 없음 또는 비어있음");
        }
    }

    private void setWebServerJsonData(Application application, ApplicationRequest request) {
        if (request.getWebServerItems() != null && !request.getWebServerItems().isEmpty()) {
            try {
                log.info("  - 웹서버 항목 JSON 변환 중: {}", request.getWebServerItems());
                String webServerJson = objectMapper.writeValueAsString(request.getWebServerItems());
                application.setWebServerItems(webServerJson);
                log.info("    ✅ 웹서버 JSON 변환 성공: {}", webServerJson);
            } catch (JsonProcessingException e) {
                log.error("❌ 웹서버 JSON 변환 실패", e);
                throw new ApplicationServiceException("웹서버 데이터 변환 중 오류가 발생했습니다.", e);
            }
        } else {
            log.info("  - 웹서버 항목 없음 또는 비어있음");
        }
    }

    private void setDatabaseJsonData(Application application, ApplicationRequest request) {
        if (request.getDbItems() != null && !request.getDbItems().isEmpty()) {
            try {
                log.info("  - DB 항목 JSON 변환 중: {}", request.getDbItems());
                String dbJson = objectMapper.writeValueAsString(request.getDbItems());
                application.setDbItems(dbJson);
                log.info("    ✅ DB JSON 변환 성공: {}", dbJson);
            } catch (JsonProcessingException e) {
                log.error("❌ DB JSON 변환 실패", e);
                throw new ApplicationServiceException("DB 데이터 변환 중 오류가 발생했습니다.", e);
            }
        } else {
            log.info("  - DB 항목 없음 또는 비어있음");
        }
    }

    /**
     * 특정 사용자의 신청서 목록 조회 (삭제된 것 포함)
     */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByEmployeeId(String employeeId) {
        List<Application> applications = applicationRepository.findByEmployeeIdIncludingDeleted(employeeId);
        log.info("✅ 사용자 신청서 조회 (삭제 포함): 사번={}, 건수={}", employeeId, applications.size());
        return applications.stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    /**
     * 모든 신청서 목록 조회 (관리자용 - 삭제된 것 포함)
     */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplicationsIncludingDeleted() {
        List<Application> applications = applicationRepository.findAllByOrderByCreatedAtDesc();
        log.info("✅ 전체 신청서 조회 (삭제 포함): 건수={}", applications.size());
        return applications.stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    /**
     * 모든 신청서 목록 조회 (관리자용 - 삭제된 것 제외)
     */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplications() {
        List<Application> applications = applicationRepository.findAllNotDeleted();
        log.info("✅ 전체 신청서 조회: 건수={}", applications.size());
        return applications.stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    /**
     * 상태별 신청서 조회
     */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByStatus(String status) {
        try {
            Application.Status statusEnum = Application.Status.valueOf(status.toUpperCase());
            List<Application> applications = applicationRepository.findByStatusOrderByCreatedAtDesc(statusEnum);
            log.info("✅ 상태별 신청서 조회: 상태={}, 건수={}", status, applications.size());
            return applications.stream()
                    .map(ApplicationResponse::from)
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new ApplicationServiceException("잘못된 상태값입니다: " + status);
        }
    }

    /**
     * 신청서 상태 업데이트 (관리자용)
     */
    public ApplicationResponse updateApplicationStatus(Long applicationId, String status, String comments, String approverEmployeeId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationServiceException(APPLICATION_NOT_FOUND_MSG + applicationId));

        try {
            Application.Status newStatus = Application.Status.valueOf(status.toUpperCase());
            
            application.setStatus(newStatus);
            application.setComments(comments);
            application.setApprovedBy(approverEmployeeId);
            
            if (newStatus == Application.Status.APPROVED) {
                application.setApprovedAt(LocalDateTime.now());
            }

            Application savedApplication = applicationRepository.save(application);
            log.info("✅ 신청서 상태 업데이트: ID={}, 상태={}, 승인자={}", 
                    savedApplication.getId(), newStatus, approverEmployeeId);
            
            return ApplicationResponse.from(savedApplication);

        } catch (IllegalArgumentException e) {
            throw new ApplicationServiceException("잘못된 상태값입니다: " + status);
        }
    }

    /**
     * 신청서 상세 조회
     */
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationServiceException(APPLICATION_NOT_FOUND_MSG + applicationId));
        return ApplicationResponse.from(application);
    }

    /**
     * 신청서 삭제 (소프트 삭제)
     */
    public void deleteApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationServiceException(APPLICATION_NOT_FOUND_MSG + applicationId));

        application.setStatus(Application.Status.DELETED);
        applicationRepository.save(application);
        
        log.info("✅ 신청서 삭제: ID={}, 사번={}", applicationId, application.getEmployeeId());
    }
    
    /**
     * 신청서의 제목과 요청사항(설명)을 수정합니다.
     */
    public void updateApplicationContent(Long id, ApplicationUpdateRequest request) {
        Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new ApplicationServiceException("신청서를 찾을 수 없습니다."));

        if (request.getTitle() != null) {
            application.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            application.setDescription(request.getDescription());
        }

        applicationRepository.save(application);
    }

    // 커스텀 예외 클래스
    public static class ApplicationServiceException extends RuntimeException {
        public ApplicationServiceException(String message) {
            super(message);
        }
        
        public ApplicationServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}