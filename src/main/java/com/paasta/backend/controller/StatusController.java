package com.paasta.backend.controller;

import com.paasta.backend.entity.Application;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/status")
@CrossOrigin(origins = {
    "https://paasta.store", 
    "https://www.paasta.store", 
    "https://api.paasta.store"
})
public class StatusController {

    /**
     * 사용 가능한 상태 목록 조회
     */
    @GetMapping("/list")
    public ResponseEntity<List<StatusInfo>> getStatusList() {
        List<StatusInfo> statusList = Arrays.stream(Application.Status.values())
                .filter(status -> status != Application.Status.DELETED) // 삭제됨 상태는 제외
                .map(status -> new StatusInfo(status.name(), status.getKoreanName()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(statusList);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusInfo {
        private String code;
        private String name;
    }
}