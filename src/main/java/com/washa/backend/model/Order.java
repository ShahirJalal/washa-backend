package com.washa.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "dobi_id", nullable = false)
    private Dobi dobi;

    @ManyToOne
    @JoinColumn(name = "rider_id")
    private Rider rider;

    @Column(nullable = false)
    private Integer weightKg;

    @ManyToOne
    @JoinColumn(name = "detergent_id")
    private Detergent detergent;

    @ManyToOne
    @JoinColumn(name = "pickup_address_id", nullable = false)
    private Address pickupAddress;

    @ManyToOne
    @JoinColumn(name = "delivery_address_id", nullable = false)
    private Address deliveryAddress;

    @Column(nullable = false)
    private LocalDateTime pickupTime;

    @Column(nullable = false)
    private LocalDateTime deliveryTime;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = OrderStatus.ORDER_PLACED;
        paymentStatus = PaymentStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum OrderStatus {
        ORDER_PLACED,
        RIDER_ASSIGNED,
        RIDER_PICKUP_TRAVEL,
        RIDER_PICKUP_ARRIVED,
        LAUNDRY_PICKED_UP,
        RIDER_DOBI_TRAVEL,
        LAUNDRY_AT_DOBI,
        WASHING_IN_PROGRESS,
        DRYING_IN_PROGRESS,
        LAUNDRY_COMPLETED,
        RIDER_DOBI_PICKUP,
        RIDER_DELIVERY_TRAVEL,
        LAUNDRY_DELIVERED,
        COMPLETED,
        CANCELLED
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED
    }

    public enum PaymentMethod {
        CASH, WALLET
    }
}