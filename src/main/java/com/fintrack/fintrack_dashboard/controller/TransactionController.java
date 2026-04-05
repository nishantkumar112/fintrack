package com.fintrack.fintrack_dashboard.controller;

import com.fintrack.fintrack_dashboard.constant.RecordType;
import com.fintrack.fintrack_dashboard.constant.TransactionStatus;
import com.fintrack.fintrack_dashboard.dto.transaction.CreateTransactionRequest;
import com.fintrack.fintrack_dashboard.dto.transaction.TransactionFilterRequest;
import com.fintrack.fintrack_dashboard.dto.transaction.TransactionResponse;
import com.fintrack.fintrack_dashboard.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @RequestBody CreateTransactionRequest request) {

        return ResponseEntity.ok(
                transactionService.createTransaction(request)
        );
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getTransactions(

            @RequestParam(required = false) RecordType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) TransactionStatus status,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        TransactionFilterRequest filter = new TransactionFilterRequest();
        filter.setType(type);
        filter.setCategory(category);
        filter.setStatus(status);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);

        return ResponseEntity.ok(
                transactionService.getTransactions(filter, page, size)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long id) {

        return ResponseEntity.ok(
                transactionService.getTransactionById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            @PathVariable Long id,
            @RequestBody CreateTransactionRequest request) {

        return ResponseEntity.ok(
                transactionService.updateTransaction(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        transactionService.deleteTransaction(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<TransactionResponse> approve(@PathVariable Long id) {

        return ResponseEntity.ok(
                transactionService.approveTransaction(id)
        );
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<TransactionResponse> reject(@PathVariable Long id) {

        return ResponseEntity.ok(
                transactionService.rejectTransaction(id)
        );
    }
}