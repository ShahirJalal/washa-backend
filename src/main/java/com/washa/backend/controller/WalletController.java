package com.washa.backend.controller;

import com.washa.backend.dto.request.WalletDepositRequest;
import com.washa.backend.dto.response.ApiResponse;
import com.washa.backend.model.User;
import com.washa.backend.model.Wallet;
import com.washa.backend.model.WalletTransaction;
import com.washa.backend.repository.UserRepository;
import com.washa.backend.repository.WalletRepository;
import com.washa.backend.repository.WalletTransactionRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    // Get wallet balance
    @GetMapping("/balance")
    public ResponseEntity<?> getWalletBalance() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setUser(user);
                    newWallet.setBalance(BigDecimal.ZERO);
                    return walletRepository.save(newWallet);
                });

        return ResponseEntity.ok(wallet);
    }

    // Deposit money into wallet (simulated)
    @PostMapping("/deposit")
    public ResponseEntity<?> depositToWallet(@Valid @RequestBody WalletDepositRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setUser(user);
                    newWallet.setBalance(BigDecimal.ZERO);
                    return walletRepository.save(newWallet);
                });

        // Add amount to balance
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        walletRepository.save(wallet);

        // Record transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(request.getAmount());
        transaction.setType(WalletTransaction.TransactionType.DEPOSIT);
        transaction.setStatus(WalletTransaction.TransactionStatus.COMPLETED);
        walletTransactionRepository.save(transaction);

        return ResponseEntity.ok(new ApiResponse(true, "Deposit successful"));
    }

    // Get transaction history
    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactionHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        List<WalletTransaction> transactions = walletTransactionRepository.findByWalletOrderByCreatedAtDesc(wallet);

        return ResponseEntity.ok(transactions);
    }
}