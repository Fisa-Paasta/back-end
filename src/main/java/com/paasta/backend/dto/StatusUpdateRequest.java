package com.paasta.backend.dto;

import lombok.Data;

@Data
public class StatusUpdateRequest {
    private String status;
    private String comments;
    private String approverEmployeeId; // 승인자 사번 (임시)
}