package com.washa.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletDepositRequest {
    @NotNull
    @Positive
    private BigDecimal amount;
}