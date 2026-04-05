package com.fintrack.fintrack_dashboard.mapper;

import com.fintrack.fintrack_dashboard.entity.Transaction;
import com.fintrack.fintrack_dashboard.dto.transaction.CreateTransactionRequest;
import com.fintrack.fintrack_dashboard.dto.transaction.TransactionResponse;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(CreateTransactionRequest request) {

        Transaction transaction = new Transaction();

        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setDescription(request.getDescription());
        transaction.setDate(request.getTransactionDate());

        return transaction;
    }

    public TransactionResponse toResponse(Transaction transaction) {

        TransactionResponse response = new TransactionResponse();

        response.setId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType());
        response.setCategory(transaction.getCategory());
        response.setDescription(transaction.getDescription());
        response.setTransactionDate(transaction.getDate());
        response.setStatus(transaction.getStatus());

        return response;
    }

    public void updateEntity(Transaction transaction,
                             CreateTransactionRequest request) {

        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setDescription(request.getDescription());
        transaction.setDate(request.getTransactionDate());
    }
}