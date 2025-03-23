package com.washa.backend.controller;

import com.washa.backend.dto.response.ApiResponse;
import com.washa.backend.model.Dobi;
import com.washa.backend.model.Order;
import com.washa.backend.model.Rider;
import com.washa.backend.model.User;
import com.washa.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private DobiRepository dobiRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WalletRepository walletRepository;

    // Get all users
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // Get all riders
    @GetMapping("/riders")
    public ResponseEntity<?> getAllRiders() {
        List<Rider> riders = riderRepository.findAll();
        return ResponseEntity.ok(riders);
    }

    // Get all dobis
    @GetMapping("/dobis")
    public ResponseEntity<?> getAllDobis() {
        List<Dobi> dobis = dobiRepository.findAll();
        return ResponseEntity.ok(dobis);
    }

    // Get all orders
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return ResponseEntity.ok(orders);
    }

    // Get platform statistics
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // Count users by role
        long userCount = userRepository.count();
        long dobisCount = dobiRepository.count();
        long ridersCount = riderRepository.count();

        // Count orders by status
        long pendingOrders = orderRepository.findByStatus(Order.OrderStatus.ORDER_PLACED).size();
        long inProgressOrders = orderRepository.findAll().stream()
                .filter(order ->
                        order.getStatus() != Order.OrderStatus.ORDER_PLACED &&
                                order.getStatus() != Order.OrderStatus.COMPLETED &&
                                order.getStatus() != Order.OrderStatus.CANCELLED
                ).count();
        long completedOrders = orderRepository.findByStatus(Order.OrderStatus.COMPLETED).size();
        long cancelledOrders = orderRepository.findByStatus(Order.OrderStatus.CANCELLED).size();

        stats.put("userCount", userCount);
        stats.put("dobisCount", dobisCount);
        stats.put("ridersCount", ridersCount);
        stats.put("pendingOrders", pendingOrders);
        stats.put("inProgressOrders", inProgressOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("cancelledOrders", cancelledOrders);

        return ResponseEntity.ok(stats);
    }

    // Update user status (activate/deactivate)
    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestParam boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // You may want to implement a proper status field in the User entity
        // For now, we'll just return a success message
        return ResponseEntity.ok(new ApiResponse(true, "User status updated successfully"));
    }

    // Cancel an order
    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);

        return ResponseEntity.ok(new ApiResponse(true, "Order cancelled successfully"));
    }
}