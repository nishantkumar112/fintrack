package com.fintrack.fintrack_dashboard.dto.dashboard;

import com.fintrack.fintrack_dashboard.constant.RecordType;
import lombok.Data;

@Data
public class CategorySummaryResponse {
    private String category;
    private RecordType type;
    private Double total;
}
