package com.washa.backend.controller;

import com.washa.backend.dto.request.DetergentRequest;
import com.washa.backend.dto.request.DobiRequest;
import com.washa.backend.dto.request.DobiServiceRequest;
import com.washa.backend.dto.response.ApiResponse;
import com.washa.backend.model.*;
import com.washa.backend.repository.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dobis")
public class DobiController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DobiRepository dobiRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private DobiServiceRepository dobiServiceRepository;

    @Autowired
    private DetergentRepository detergentRepository;

    @Autowired
    private OrderRepository orderRepository;

    // Get all active dobis
    @GetMapping
    public ResponseEntity<?> getAllDobis() {
        List<Dobi> dobis = dobiRepository.findByIsActiveTrue();
        return ResponseEntity.ok(dobis);
    }

    // Get a single dobi by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getDobiById(@PathVariable Long id) {
        Dobi dobi = dobiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dobi not found"));
        return ResponseEntity.ok(dobi);
    }

    // Create a new dobi (only DOBI_OWNER can do this)
    @PostMapping
    @PreAuthorize("hasRole('DOBI_OWNER')")
    public ResponseEntity<?> createDobi(@Valid @RequestBody DobiRequest dobiRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create address
        Address address = new Address();
        address.setUser(user);
        address.setAddressLine(dobiRequest.getAddress().getAddressLine());
        address.setCity(dobiRequest.getAddress().getCity());
        address.setState(dobiRequest.getAddress().getState());
        address.setPostalCode(dobiRequest.getAddress().getPostalCode());
        address = addressRepository.save(address);

        // Create dobi
        Dobi dobi = new Dobi();
        dobi.setOwner(user);
        dobi.setName(dobiRequest.getName());
        dobi.setDescription(dobiRequest.getDescription());
        dobi.setAddress(address);
        dobi.setActive(true);

        dobiRepository.save(dobi);

        return ResponseEntity.ok(new ApiResponse(true, "Dobi registered successfully"));
    }

    // Add a service to a dobi
    @PostMapping("/{dobiId}/services")
    @PreAuthorize("hasRole('DOBI_OWNER')")
    public ResponseEntity<?> addDobiService(
            @PathVariable Long dobiId,
            @Valid @RequestBody DobiServiceRequest serviceRequest) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Dobi dobi = dobiRepository.findById(dobiId)
                .orElseThrow(() -> new RuntimeException("Dobi not found"));

        // Check if this dobi belongs to the current user
        if (!dobi.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "You don't own this dobi"));
        }

        DobiService service = new DobiService();
        service.setDobi(dobi);
        service.setWeightCapacity(serviceRequest.getWeightCapacity());
        service.setPrice(serviceRequest.getPrice());

        dobiServiceRepository.save(service);

        return ResponseEntity.ok(new ApiResponse(true, "Service added successfully"));
    }

    // Add a detergent to a dobi
    @PostMapping("/{dobiId}/detergents")
    @PreAuthorize("hasRole('DOBI_OWNER')")
    public ResponseEntity<?> addDobiDetergent(
            @PathVariable Long dobiId,
            @Valid @RequestBody DetergentRequest detergentRequest) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Dobi dobi = dobiRepository.findById(dobiId)
                .orElseThrow(() -> new RuntimeException("Dobi not found"));

        // Check if this dobi belongs to the current user
        if (!dobi.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "You don't own this dobi"));
        }

        Detergent detergent = new Detergent();
        detergent.setDobi(dobi);
        detergent.setName(detergentRequest.getName());
        detergent.setDescription(detergentRequest.getDescription());

        detergentRepository.save(detergent);

        return ResponseEntity.ok(new ApiResponse(true, "Detergent added successfully"));
    }

    // Get all services for a dobi
    @GetMapping("/{dobiId}/services")
    public ResponseEntity<?> getDobiServices(@PathVariable Long dobiId) {
        Dobi dobi = dobiRepository.findById(dobiId)
                .orElseThrow(() -> new RuntimeException("Dobi not found"));

        List<DobiService> services = dobiServiceRepository.findByDobi(dobi);

        return ResponseEntity.ok(services);
    }

    // Get all detergents for a dobi
    @GetMapping("/{dobiId}/detergents")
    public ResponseEntity<?> getDobiDetergents(@PathVariable Long dobiId) {
        Dobi dobi = dobiRepository.findById(dobiId)
                .orElseThrow(() -> new RuntimeException("Dobi not found"));

        List<Detergent> detergents = detergentRepository.findByDobi(dobi);

        return ResponseEntity.ok(detergents);
    }

    // Get orders for a dobi (only the dobi owner can see their orders)
    @GetMapping("/{dobiId}/orders")
    @PreAuthorize("hasRole('DOBI_OWNER')")
    public ResponseEntity<?> getDobiOrders(@PathVariable Long dobiId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Dobi dobi = dobiRepository.findById(dobiId)
                .orElseThrow(() -> new RuntimeException("Dobi not found"));

        // Check if this dobi belongs to the current user
        if (!dobi.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "You don't own this dobi"));
        }

        List<Order> orders = orderRepository.findByDobi(dobi);

        return ResponseEntity.ok(orders);
    }
}