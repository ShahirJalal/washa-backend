package com.washa.backend.controller;

import com.washa.backend.dto.request.AddressRequest;
import com.washa.backend.dto.response.ApiResponse;
import com.washa.backend.dto.response.UserResponse;
import com.washa.backend.model.Address;
import com.washa.backend.model.User;
import com.washa.backend.repository.AddressRepository;
import com.washa.backend.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(new UserResponse(user));
    }

    @GetMapping("/addresses")
    public ResponseEntity<?> getUserAddresses() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Address> addresses = addressRepository.findByUser(user);

        return ResponseEntity.ok(addresses);
    }

    @PostMapping("/addresses")
    public ResponseEntity<?> addUserAddress(@Valid @RequestBody AddressRequest addressRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = new Address();
        address.setUser(user);
        address.setAddressLine(addressRequest.getAddressLine());
        address.setCity(addressRequest.getCity());
        address.setState(addressRequest.getState());
        address.setPostalCode(addressRequest.getPostalCode());

        // If this is set as default, reset any other default
        if (addressRequest.isDefault()) {
            addressRepository.findByUserAndIsDefaultTrue(user)
                    .ifPresent(defaultAddress -> {
                        defaultAddress.setDefault(false);
                        addressRepository.save(defaultAddress);
                    });
            address.setDefault(true);
        }

        addressRepository.save(address);

        return ResponseEntity.ok(new ApiResponse(true, "Address added successfully"));
    }
}