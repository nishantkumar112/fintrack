package com.fintrack.fintrack_dashboard.dto.transaction;

import com.fintrack.fintrack_dashboard.constant.RecordType;
import com.fintrack.fintrack_dashboard.constant.TransactionStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TransactionFilterRequest {

    private RecordType type;
    private String category;
    private TransactionStatus status;

    private LocalDate startDate;
    private LocalDate endDate;
}