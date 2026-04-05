package com.fintrack.fintrack_dashboard.dto.transaction;

import com.fintrack.fintrack_dashboard.constant.RecordType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateTransactionRequest {
    private Double amount;
    private RecordType type;
    private String category;
    private String description;
    private LocalDate date;
}
