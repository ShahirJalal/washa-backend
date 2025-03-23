package com.washa.backend.controller;

import com.washa.backend.dto.request.OrderRequest;
import com.washa.backend.dto.request.OrderStatusUpdateRequest;
import com.washa.backend.dto.response.ApiResponse;
import com.washa.backend.model.*;
import com.washa.backend.repository.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DobiRepository dobiRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private DetergentRepository detergentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private DobiServiceRepository dobiServiceRepository;

    @Autowired
    private WalletRepository walletRepository;

    // Create a new order
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Dobi dobi = dobiRepository.findById(orderRequest.getDobiId())
                .orElseThrow(() -> new RuntimeException("Dobi not found"));

        Address pickupAddress = addressRepository.findById(orderRequest.getPickupAddressId())
                .orElseThrow(() -> new RuntimeException("Pickup address not found"));

        Address deliveryAddress = addressRepository.findById(orderRequest.getDeliveryAddressId())
                .orElseThrow(() -> new RuntimeException("Delivery address not found"));

        Detergent detergent = detergentRepository.findById(orderRequest.getDetergentId())
                .orElseThrow(() -> new RuntimeException("Detergent not found"));

        // Verify addresses belong to user
        if (!pickupAddress.getUser().getId().equals(user.getId()) ||
                !deliveryAddress.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid addresses"));
        }

        // Find price based on weight and dobi service
        List<DobiService> services = dobiServiceRepository.findByDobi(dobi);
        DobiService selectedService = services.stream()
                .filter(service -> service.getWeightCapacity().equals(orderRequest.getWeightKg()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Selected weight not supported by this dobi"));

        BigDecimal price = selectedService.getPrice();

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setDobi(dobi);
        order.setWeightKg(orderRequest.getWeightKg());
        order.setDetergent(detergent);
        order.setPickupAddress(pickupAddress);
        order.setDeliveryAddress(deliveryAddress);
        order.setPickupTime(orderRequest.getPickupTime());
        order.setDeliveryTime(orderRequest.getDeliveryTime());
        order.setTotalPrice(price);
        order.setStatus(Order.OrderStatus.ORDER_PLACED);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setPaymentMethod(Order.PaymentMethod.valueOf(orderRequest.getPaymentMethod()));

        Order savedOrder = orderRepository.save(order);

        // Record status history
        OrderStatusHistory statusHistory = new OrderStatusHistory();
        statusHistory.setOrder(savedOrder);
        statusHistory.setStatus(Order.OrderStatus.ORDER_PLACED);
        statusHistory.setUpdatedBy(user);
        orderStatusHistoryRepository.save(statusHistory);

        return ResponseEntity.ok(savedOrder);
    }

    // Get all orders for the current user
    @GetMapping
    public ResponseEntity<?> getUserOrders() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> orders = orderRepository.findByUser(user);
        return ResponseEntity.ok(orders);
    }

    // Get a specific order by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Check if user is the owner, or admin, or the dobi owner, or the assigned rider
        boolean isAuthorized = order.getUser().getId().equals(user.getId()) ||
                user.getRole() == User.Role.ADMIN ||
                (order.getDobi() != null && order.getDobi().getOwner().getId().equals(user.getId())) ||
                (order.getRider() != null && order.getRider().getUser().getId().equals(user.getId()));

        if (!isAuthorized) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Not authorized to access this order"));
        }

        return ResponseEntity.ok(order);
    }

    // Update order status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest statusRequest) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Determine if user has permission to update this status
        boolean canUpdate = false;

        // Admin can update any status
        if (user.getRole() == User.Role.ADMIN) {
            canUpdate = true;
        }
        // Dobi owner can update specific statuses
        else if (user.getRole() == User.Role.DOBI_OWNER &&
                order.getDobi().getOwner().getId().equals(user.getId())) {
            canUpdate = statusRequest.getStatus() == Order.OrderStatus.LAUNDRY_AT_DOBI ||
                    statusRequest.getStatus() == Order.OrderStatus.WASHING_IN_PROGRESS ||
                    statusRequest.getStatus() == Order.OrderStatus.DRYING_IN_PROGRESS ||
                    statusRequest.getStatus() == Order.OrderStatus.LAUNDRY_COMPLETED;
        }
        // Rider can update specific statuses
        else if (user.getRole() == User.Role.RIDER &&
                order.getRider() != null &&
                order.getRider().getUser().getId().equals(user.getId())) {
            canUpdate = statusRequest.getStatus() == Order.OrderStatus.RIDER_PICKUP_TRAVEL ||
                    statusRequest.getStatus() == Order.OrderStatus.RIDER_PICKUP_ARRIVED ||
                    statusRequest.getStatus() == Order.OrderStatus.LAUNDRY_PICKED_UP ||
                    statusRequest.getStatus() == Order.OrderStatus.RIDER_DOBI_TRAVEL ||
                    statusRequest.getStatus() == Order.OrderStatus.RIDER_DOBI_PICKUP ||
                    statusRequest.getStatus() == Order.OrderStatus.RIDER_DELIVERY_TRAVEL ||
                    statusRequest.getStatus() == Order.OrderStatus.LAUNDRY_DELIVERED;
        }

        if (!canUpdate) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Not authorized to update this status"));
        }

        // Update the order status
        order.setStatus(statusRequest.getStatus());
        orderRepository.save(order);

        // Record status history
        OrderStatusHistory statusHistory = new OrderStatusHistory();
        statusHistory.setOrder(order);
        statusHistory.setStatus(statusRequest.getStatus());
        statusHistory.setUpdatedBy(user);
        orderStatusHistoryRepository.save(statusHistory);

        return ResponseEntity.ok(new ApiResponse(true, "Order status updated successfully"));
    }

    // Get order status history
    @GetMapping("/{id}/history")
    public ResponseEntity<?> getOrderStatusHistory(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Check if user is authorized to view this order's history
        boolean isAuthorized = order.getUser().getId().equals(user.getId()) ||
                user.getRole() == User.Role.ADMIN ||
                (order.getDobi() != null && order.getDobi().getOwner().getId().equals(user.getId())) ||
                (order.getRider() != null && order.getRider().getUser().getId().equals(user.getId()));

        if (!isAuthorized) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Not authorized to access this order history"));
        }

        List<OrderStatusHistory> history = orderStatusHistoryRepository.findByOrderOrderByCreatedAtDesc(order);
        return ResponseEntity.ok(history);
    }
}