package com.fintrack.fintrack_dashboard.controller;

import com.fintrack.fintrack_dashboard.dto.dashboard.CategorySummaryResponse;
import com.fintrack.fintrack_dashboard.dto.dashboard.DashboardSummaryResponse;
import com.fintrack.fintrack_dashboard.dto.dashboard.MonthlyTrendResponse;
import com.fintrack.fintrack_dashboard.dto.transaction.TransactionResponse;
import com.fintrack.fintrack_dashboard.service.AnalyticsService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getSummary(startDate, endDate));
    }

    @GetMapping("/category")
    public ResponseEntity<List<CategorySummaryResponse>> getCategorySummary(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getCategorySummary(startDate, endDate));
    }

    @GetMapping("/trend")
    public ResponseEntity<List<MonthlyTrendResponse>> getTrends(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getMonthlyTrends(startDate, endDate));
    }

    @GetMapping("/recent")
    public ResponseEntity<Page<TransactionResponse>> getRecent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(analyticsService.getRecentTransactions(page, size));
    }
}
