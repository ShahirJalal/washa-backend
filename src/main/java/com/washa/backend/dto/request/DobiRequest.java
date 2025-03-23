package com.washa.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DobiRequest {
    @NotBlank
    private String name;

    private String description;

    private AddressRequest address;
}