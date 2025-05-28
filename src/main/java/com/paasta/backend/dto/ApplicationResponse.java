package com.paasta.backend.dto;

import com.paasta.backend.entity.Application;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    // ===== 기본 정보 =====
    private Long id;
    private String employeeId;
    private String title;
    private String description;
    private String status;
    
    // ===== Step 1: 환경 선택 =====
    private String envType;
    
    // ===== Step 2: VM 정보 (IaaS용) =====
    private String vmHostname;
    private String vmUsername;
    private String vmEnvironment;
    
    // ===== Step 2: K8s 정보 (PaaS용) =====
    private String k8sType;
    private String k8sNamespace;
    private String k8sNodeCount;
    
    // ===== Step 3: 자원 정보 =====
    private String resourceCpu;
    private String resourceRam;
    private String resourceDisk;
    
    // ===== Step 3: AWS 관련 자원 (IaaS AWS, PaaS EKS용) =====
    private String vmEc2Type;
    private String vmEbsType;
    private String vmEbsSize;
    
    // ===== Step 4: OS 정보 =====
    private String osName;
    private String osVersion;
    
    // ===== Step 5: 프론트엔드 정보 =====
    private String frontendItems;
    private String frontendDomain;
    
    // ===== Step 6: 백엔드 정보 =====
    private String backendItems;
    private String apiDomain;
    private String apiPaths;
    
    // ===== Step 7: 웹서버 정보 =====
    private String webServerItems;
    
    // ===== Step 8: 데이터베이스 정보 =====
    private String dbItems;
    
    // ===== 시스템 관리 정보 =====
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String comments;
    
    public static ApplicationResponse from(Application application) {
        return ApplicationResponse.builder()
                // 기본 정보
                .id(application.getId())
                .employeeId(application.getEmployeeId())
                .title(application.getTitle())
                .description(application.getDescription())
                .status(application.getStatus().getKoreanName()) // ✅ 한국어 상태명 사용
                
                // Step 1: 환경
                .envType(application.getEnvType())
                
                // Step 2: VM/K8s
                .vmHostname(application.getVmHostname())
                .vmUsername(application.getVmUsername())
                .vmEnvironment(application.getVmEnvironment())
                .k8sType(application.getK8sType())
                .k8sNamespace(application.getK8sNamespace())
                .k8sNodeCount(application.getK8sNodeCount())
                
                // Step 3: 자원
                .resourceCpu(application.getResourceCpu())
                .resourceRam(application.getResourceRam())
                .resourceDisk(application.getResourceDisk())
                .vmEc2Type(application.getVmEc2Type())
                .vmEbsType(application.getVmEbsType())
                .vmEbsSize(application.getVmEbsSize())
                
                // Step 4: OS
                .osName(application.getOsName())
                .osVersion(application.getOsVersion())
                
                // Step 5: 프론트엔드
                .frontendItems(application.getFrontendItems())
                .frontendDomain(application.getFrontendDomain())
                
                // Step 6: 백엔드
                .backendItems(application.getBackendItems())
                .apiDomain(application.getApiDomain())
                .apiPaths(application.getApiPaths())
                
                // Step 7: 웹서버
                .webServerItems(application.getWebServerItems())
                
                // Step 8: 데이터베이스
                .dbItems(application.getDbItems())
                
                // 시스템 관리 정보
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .approvedBy(application.getApprovedBy())
                .approvedAt(application.getApprovedAt())
                .comments(application.getComments())
                .build();
    }
}