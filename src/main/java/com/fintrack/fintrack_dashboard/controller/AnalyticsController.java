package com.fintrack.fintrack_dashboard.controller;

import com.fintrack.fintrack_dashboard.dto.dashboard.*;
import com.fintrack.fintrack_dashboard.dto.transaction.TransactionResponse;
import com.fintrack.fintrack_dashboard.service.AnalyticsService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // ============================
    // SUMMARY
    // ============================
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        return ResponseEntity.ok(
                analyticsService.getSummary(startDate, endDate)
        );
    }

    // ============================
    // CATEGORY
    // ============================
    @GetMapping("/category")
    public ResponseEntity<?> getCategorySummary(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        return ResponseEntity.ok(
                analyticsService.getCategorySummary(startDate, endDate)
        );
    }

    // ============================
    // TREND
    // ============================
    @GetMapping("/trend")
    public ResponseEntity<?> getTrends(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        return ResponseEntity.ok(
                analyticsService.getMonthlyTrends(startDate, endDate)
        );
    }

    // ============================
    // RECENT (PAGINATION)
    // ============================
    @GetMapping("/recent")
    public ResponseEntity<Page<TransactionResponse>> getRecent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(
                analyticsService.getRecentTransactions(page, size)
        );
    }
}