package com.washa.backend.controller;

import com.washa.backend.dto.request.RiderAvailabilityRequest;
import com.washa.backend.dto.response.ApiResponse;
import com.washa.backend.model.Order;
import com.washa.backend.model.Rider;
import com.washa.backend.model.User;
import com.washa.backend.repository.OrderRepository;
import com.washa.backend.repository.RiderRepository;
import com.washa.backend.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rider")
@PreAuthorize("hasRole('RIDER')")
public class RiderController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PutMapping("/availability")
    public ResponseEntity<?> updateAvailability(@Valid @RequestBody RiderAvailabilityRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Rider rider = riderRepository.findByUser(user)
                .orElseGet(() -> {
                    // Create rider profile if it doesn't exist
                    Rider newRider = new Rider();
                    newRider.setUser(user);
                    newRider.setAvailable(false);
                    return riderRepository.save(newRider);
                });

        rider.setAvailable(request.isAvailable());
        riderRepository.save(rider);

        return ResponseEntity.ok(new ApiResponse(true, "Availability updated successfully"));
    }

    // Get available orders that need a rider
    @GetMapping("/available-orders")
    public ResponseEntity<?> getAvailableOrders() {
        List<Order> availableOrders = orderRepository.findByStatus(Order.OrderStatus.ORDER_PLACED);
        return ResponseEntity.ok(availableOrders);
    }

    // Accept an order
    @PostMapping("/orders/{orderId}/accept")
    public ResponseEntity<?> acceptOrder(@PathVariable Long orderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Rider rider = riderRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Rider profile not found"));

        if (!rider.isAvailable()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "You must be available to accept orders"));
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != Order.OrderStatus.ORDER_PLACED) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Order is not available for acceptance"));
        }

        order.setRider(rider);
        order.setStatus(Order.OrderStatus.RIDER_ASSIGNED);
        orderRepository.save(order);

        return ResponseEntity.ok(new ApiResponse(true, "Order accepted successfully"));
    }

    // Get rider's current orders
    @GetMapping("/orders")
    public ResponseEntity<?> getRiderOrders() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Rider rider = riderRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Rider profile not found"));

        List<Order> orders = orderRepository.findByRider(rider);
        return ResponseEntity.ok(orders);
    }
}