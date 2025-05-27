package com.paasta.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employee_id", nullable = false)
    private String employeeId; // 신청자 사번
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.RECEIVED;
    
    @Column(name = "env_type")
    private String envType; // iaas, paas
    
    // VM 정보 (IaaS용)
    @Column(name = "vm_hostname")
    private String vmHostname;
    
    @Column(name = "vm_username")
    private String vmUsername;
    
    @Column(name = "vm_environment")
    private String vmEnvironment; // on-premise, aws
    
    @Column(name = "vm_ec2_type")
    private String vmEc2Type;
    
    @Column(name = "vm_ebs_type")
    private String vmEbsType;
    
    @Column(name = "vm_ebs_size")
    private String vmEbsSize;
    
    // K8s 정보 (PaaS용)
    @Column(name = "k8s_type")
    private String k8sType; // kubernetes, amazon_eks
    
    @Column(name = "k8s_namespace")
    private String k8sNamespace;
    
    @Column(name = "k8s_node_count")
    private String k8sNodeCount;
    
    // 자원 정보
    @Column(name = "resource_cpu")
    private String resourceCpu;
    
    @Column(name = "resource_ram")
    private String resourceRam;
    
    @Column(name = "resource_disk")
    private String resourceDisk;
    
    // OS 정보
    @Column(name = "os_name")
    private String osName;
    
    @Column(name = "os_version")
    private String osVersion;
    
    // 프론트엔드 정보
    @Column(name = "frontend_items", columnDefinition = "TEXT")
    private String frontendItems; // JSON 형태로 저장
    
    @Column(name = "frontend_domain")
    private String frontendDomain;
    
    // 백엔드 정보
    @Column(name = "backend_items", columnDefinition = "TEXT")
    private String backendItems; // JSON 형태로 저장
    
    @Column(name = "api_domain")
    private String apiDomain;
    
    @Column(name = "api_paths", columnDefinition = "TEXT")
    private String apiPaths; // JSON 형태로 저장
    
    // 웹서버 정보
    @Column(name = "webserver_items", columnDefinition = "TEXT")
    private String webServerItems; // JSON 형태로 저장
    
    // 데이터베이스 정보
    @Column(name = "db_items", columnDefinition = "TEXT")
    private String dbItems; // JSON 형태로 저장
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "approved_by")
    private String approvedBy; // 승인자 사번
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(columnDefinition = "TEXT")
    private String comments; // 관리자 코멘트
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum Status {
        RECEIVED("접수중"),
        RECEIVED_COMPLETE("접수완료"), 
        APPROVAL_PENDING("승인처리중"),
        APPROVED("승인완료"),
        BUILDING("구축중"),
        COMPLETED("구축완료"),
        DELETED("삭제됨");
        
        private final String koreanName;
        
        Status(String koreanName) {
            this.koreanName = koreanName;
        }
        
        public String getKoreanName() {
            return koreanName;
        }
    }
}