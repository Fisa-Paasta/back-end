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
    private Long id;
    private String employeeId;
    private String title;
    private String description;
    private String status;
    private String envType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String comments;
    
    // 상세 정보들
    private String vmHostname;
    private String vmUsername;
    private String vmEnvironment;
    private String vmEc2Type;
    private String vmEbsType;
    private String vmEbsSize;
    
    private String k8sType;
    private String k8sNamespace;
    private String k8sNodeCount;
    
    private String resourceCpu;
    private String resourceRam;
    private String resourceDisk;
    
    private String osName;
    private String osVersion;
    
    private String frontendItems;
    private String frontendDomain;
    private String backendItems;
    private String apiDomain;
    private String apiPaths;
    private String webServerItems;
    private String dbItems;
    
    public static ApplicationResponse from(Application application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .employeeId(application.getEmployeeId())
                .title(application.getTitle())
                .description(application.getDescription())
                .status(application.getStatus().getKoreanName())
                .envType(application.getEnvType())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .approvedBy(application.getApprovedBy())
                .approvedAt(application.getApprovedAt())
                .comments(application.getComments())
                .vmHostname(application.getVmHostname())
                .vmUsername(application.getVmUsername())
                .vmEnvironment(application.getVmEnvironment())
                .vmEc2Type(application.getVmEc2Type())
                .vmEbsType(application.getVmEbsType())
                .vmEbsSize(application.getVmEbsSize())
                .k8sType(application.getK8sType())
                .k8sNamespace(application.getK8sNamespace())
                .k8sNodeCount(application.getK8sNodeCount())
                .resourceCpu(application.getResourceCpu())
                .resourceRam(application.getResourceRam())
                .resourceDisk(application.getResourceDisk())
                .osName(application.getOsName())
                .osVersion(application.getOsVersion())
                .frontendItems(application.getFrontendItems())
                .frontendDomain(application.getFrontendDomain())
                .backendItems(application.getBackendItems())
                .apiDomain(application.getApiDomain())
                .apiPaths(application.getApiPaths())
                .webServerItems(application.getWebServerItems())
                .dbItems(application.getDbItems())
                .build();
    }
}
