package com.paasta.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class ApplicationRequest {
    private String title;
    private String description;
    private String envType; // iaas, paas
    private String employeeId; // 임시로 받기 (나중에 인증으로 대체)
    
    // VM 정보
    private VmConfig vm;
    
    // K8s 정보
    private K8sConfig k8s;
    
    // 자원 정보
    private ResourcesConfig resources;
    
    // OS 정보
    private OsConfig os;
    
    // 프론트엔드 정보
    private List<FrontendItem> frontendItems;
    private String frontendDomain;
    
    // 백엔드 정보
    private List<BackendItem> backendItems;
    private String apiDomain;
    private List<String> apiPaths;
    
    // 웹서버 정보
    private List<WebServerItem> webServerItems;
    
    // 데이터베이스 정보
    private List<DbItem> dbItems;
    
    @Data
    public static class VmConfig {
        private String hostname;
        private String username;
        private String environment;
        private String ec2Type;
        private String ebsType;
        private String ebsSize;
    }
    
    @Data
    public static class K8sConfig {
        private String type;
        private String namespace;
        private String node;
    }
    
    @Data
    public static class ResourcesConfig {
        private String cpu;
        private String ram;
        private String disk;
    }
    
    @Data
    public static class OsConfig {
        private String name;
        private String version;
    }
    
    @Data
    public static class FrontendItem {
        private Long id;
        private String framework;
        private String version;
    }
    
    @Data
    public static class BackendItem {
        private Long id;
        private String language;
        private String languageVersion;
        private String framework;
        private String frameworkVersion;
    }
    
    @Data
    public static class WebServerItem {
        private Long id;
        private String server;
        private String version;
    }
    
    @Data
    public static class DbItem {
        private Long id;
        private String type;
        private String name;
        private String version;
        private String size;
    }
}