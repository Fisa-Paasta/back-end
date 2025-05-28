package com.paasta.backend.dto;

import lombok.Data;

@Data
public class ApplicationUpdateRequest {
    private String title;
    private String description;
}