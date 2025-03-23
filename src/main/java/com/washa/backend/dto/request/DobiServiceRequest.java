package com.washa.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DobiServiceRequest {
    @NotNull
    private Integer weightCapacity; // 10, 15, 20 kg

    @NotNull
    @Positive
    private BigDecimal price;
}