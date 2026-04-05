package com.fintrack.fintrack_dashboard.dto.transaction;

import com.fintrack.fintrack_dashboard.constant.RecordType;
import com.fintrack.fintrack_dashboard.constant.TransactionStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TransactionResponse {
    private Long id;
    private Double amount;
    private RecordType type;
    private String category;
    private String description;
    private TransactionStatus status;
    private LocalDate date;
}
