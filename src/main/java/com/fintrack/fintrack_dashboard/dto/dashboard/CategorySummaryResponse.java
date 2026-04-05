package com.fintrack.fintrack_dashboard.dto.dashboard;

import lombok.Data;

@Data
public class CategorySummaryResponse {
    private String category;
    private Double total;
}