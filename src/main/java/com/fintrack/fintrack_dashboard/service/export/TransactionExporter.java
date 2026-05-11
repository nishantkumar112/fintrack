package com.fintrack.fintrack_dashboard.service.export;

import com.fintrack.fintrack_dashboard.dto.transaction.TransactionResponse;

import java.util.List;

public interface TransactionExporter {
    byte[]  export(List<TransactionResponse> transactionResponses);
    String getContentType();
    String getFileExtension();
}
