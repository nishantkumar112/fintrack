package com.fintrack.fintrack_dashboard.controller;

import com.fintrack.fintrack_dashboard.constant.ExportFormat;
import com.fintrack.fintrack_dashboard.constant.RecordType;
import com.fintrack.fintrack_dashboard.constant.TransactionStatus;
import com.fintrack.fintrack_dashboard.dto.transaction.CreateTransactionRequest;
import com.fintrack.fintrack_dashboard.dto.transaction.TransactionFilterRequest;
import com.fintrack.fintrack_dashboard.dto.transaction.TransactionResponse;
import com.fintrack.fintrack_dashboard.service.transaction.TransactionService;
import com.fintrack.fintrack_dashboard.service.export.ExportService;
import com.fintrack.fintrack_dashboard.service.export.TransactionExporter;
import com.fintrack.fintrack_dashboard.dto.common.PaginatedResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;
    private final ExportService exportService;

    public TransactionController(TransactionService transactionService,ExportService exportService) {
        this.transactionService = transactionService;
        this.exportService = exportService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @RequestBody CreateTransactionRequest request) {

        return ResponseEntity.ok(
                transactionService.createTransaction(request)
        );
    }

    @GetMapping
    public PaginatedResponse<TransactionResponse> getTransactions(

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

        return transactionService.getTransactions(filter, page, size);
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

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportTransactions(

            @ModelAttribute
            TransactionFilterRequest filter,

            @RequestParam(defaultValue = "CSV")
            ExportFormat format
    ) {

        List<TransactionResponse> transactions =
                transactionService
                        .getTransactions(
                                filter,
                                0,
                                Integer.MAX_VALUE
                        )
                        .getContent();

        TransactionExporter exporter =
                exportService.getExporter(format);

        byte[] data =
                exporter.export(transactions);

        String filename =
                "transactions."
                        + exporter.getFileExtension();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename
                )
                .header(
                        HttpHeaders.CONTENT_TYPE,
                        exporter.getContentType()
                )
                .body(data);
    }
}