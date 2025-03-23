package com.washa.backend.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderRequest {
    @NotNull
    private Long dobiId;

    @NotNull
    private Integer weightKg; // 10, 15, 20 kg

    @NotNull
    private Long detergentId;

    @NotNull
    private Long pickupAddressId;

    @NotNull
    private Long deliveryAddressId;

    @NotNull
    @Future
    private LocalDateTime pickupTime;

    @NotNull
    @Future
    private LocalDateTime deliveryTime;

    @NotNull
    private String paymentMethod; // "CASH" or "WALLET"
}