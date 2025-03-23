package com.washa.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DetergentRequest {
    @NotBlank
    private String name;

    private String description;
}