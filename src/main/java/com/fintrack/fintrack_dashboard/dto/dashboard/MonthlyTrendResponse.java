package com.fintrack.fintrack_dashboard.dto.dashboard;

import lombok.Data;

@Data
public class MonthlyTrendResponse {
    private String month;
    private Double totalIncome;
    private Double totalExpense;
}