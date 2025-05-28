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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ObjectMapper objectMapper;

    /**
     * 신청서 생성 (중복 확인 추가)
     */
    public ApplicationResponse createApplication(ApplicationRequest request) {
        try {
            // ✅ 중복 확인 - 같은 사용자가 같은 제목으로 최근에 신청했는지 확인
            List<Application> recentApplications = applicationRepository
                .findByEmployeeIdAndTitleAndCreatedAtAfter(
                    request.getEmployeeId(), 
                    request.getTitle(),
                    LocalDateTime.now().minusMinutes(5) // 5분 이내 중복 체크
                );
            
            if (!recentApplications.isEmpty()) {
                log.warn("중복 신청 감지: 사번={}, 제목={}", request.getEmployeeId(), request.getTitle());
                throw new RuntimeException("동일한 신청서가 이미 제출되었습니다.");
            }

            Application application = Application.builder()
                    .employeeId(request.getEmployeeId())
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .envType(request.getEnvType())
                    .status(Application.Status.RECEIVED)
                    .build();

            // VM 정보 설정 (IaaS용)
            if (request.getVm() != null) {
                application.setVmHostname(request.getVm().getHostname());
                application.setVmUsername(request.getVm().getUsername());
                application.setVmEnvironment(request.getVm().getEnvironment());
                application.setVmEc2Type(request.getVm().getEc2Type());
                application.setVmEbsType(request.getVm().getEbsType());
                application.setVmEbsSize(request.getVm().getEbsSize());
            }

            // K8s 정보 설정 (PaaS용)
            if (request.getK8s() != null) {
                application.setK8sType(request.getK8s().getType());
                application.setK8sNamespace(request.getK8s().getNamespace());
                application.setK8sNodeCount(request.getK8s().getNode());
            }

            // 자원 정보 설정
            if (request.getResources() != null) {
                application.setResourceCpu(request.getResources().getCpu());
                application.setResourceRam(request.getResources().getRam());
                application.setResourceDisk(request.getResources().getDisk());
            }

            // OS 정보 설정
            if (request.getOs() != null) {
                application.setOsName(request.getOs().getName());
                application.setOsVersion(request.getOs().getVersion());
            }

            // JSON 데이터 저장
            if (request.getFrontendItems() != null) {
                application.setFrontendItems(objectMapper.writeValueAsString(request.getFrontendItems()));
                application.setFrontendDomain(request.getFrontendDomain());
            }

            if (request.getBackendItems() != null) {
                application.setBackendItems(objectMapper.writeValueAsString(request.getBackendItems()));
                application.setApiDomain(request.getApiDomain());
                if (request.getApiPaths() != null) {
                    application.setApiPaths(objectMapper.writeValueAsString(request.getApiPaths()));
                }
            }

            if (request.getWebServerItems() != null) {
                application.setWebServerItems(objectMapper.writeValueAsString(request.getWebServerItems()));
            }

            if (request.getDbItems() != null) {
                application.setDbItems(objectMapper.writeValueAsString(request.getDbItems()));
            }

            Application savedApplication = applicationRepository.save(application);
            log.info("✅ 신청서 생성 완료: ID={}, 사번={}, 제목={}", 
                    savedApplication.getId(), savedApplication.getEmployeeId(), savedApplication.getTitle());
            
            return ApplicationResponse.from(savedApplication);

        } catch (JsonProcessingException e) {
            log.error("JSON 변환 중 오류 발생", e);
            throw new RuntimeException("JSON 변환 중 오류가 발생했습니다.", e);
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
                .collect(Collectors.toList());
    }

    /**
     * 모든 신청서 목록 조회 (관리자용)
     */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplications() {
        List<Application> applications = applicationRepository.findAllNotDeleted();
        log.info("✅ 전체 신청서 조회: 건수={}", applications.size());
        return applications.stream()
                .map(ApplicationResponse::from)
                .collect(Collectors.toList());
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
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("잘못된 상태값입니다: " + status);
        }
    }

    /**
     * 신청서 상태 업데이트 (관리자용)
     */
    public ApplicationResponse updateApplicationStatus(Long applicationId, String status, String comments, String approverEmployeeId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("신청서를 찾을 수 없습니다: ID=" + applicationId));

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
            throw new RuntimeException("잘못된 상태값입니다: " + status);
        }
    }

    /**
     * 신청서 상세 조회
     */
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("신청서를 찾을 수 없습니다: ID=" + applicationId));
        return ApplicationResponse.from(application);
    }

    /**
     * 신청서 삭제 (소프트 삭제)
     */
    public void deleteApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("신청서를 찾을 수 없습니다: ID=" + applicationId));

        application.setStatus(Application.Status.DELETED);
        applicationRepository.save(application);
        
        log.info("✅ 신청서 삭제: ID={}, 사번={}", applicationId, application.getEmployeeId());
    }
}