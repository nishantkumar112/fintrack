package com.fintrack.fintrack_dashboard.dto.dashboard;

import lombok.Data;

@Data
public class DashboardSummaryResponse {
    private Double totalIncome;
    private Double totalExpense;
    private Double netBalance;
}