package com.washa.backend.dto.request;

import com.washa.backend.model.Order;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    @NotNull
    private Order.OrderStatus status;
}