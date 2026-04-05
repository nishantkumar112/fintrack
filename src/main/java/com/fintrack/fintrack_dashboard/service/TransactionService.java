package com.fintrack.fintrack_dashboard.service;

import com.fintrack.fintrack_dashboard.constant.TransactionStatus;
import com.fintrack.fintrack_dashboard.dto.transaction.*;
import com.fintrack.fintrack_dashboard.entity.Transaction;
import com.fintrack.fintrack_dashboard.entity.User;
import com.fintrack.fintrack_dashboard.mapper.TransactionMapper;
import com.fintrack.fintrack_dashboard.respository.TransactionRepository;
import com.fintrack.fintrack_dashboard.utils.SecurityUtils;
import com.fintrack.fintrack_dashboard.utils.TransactionValidator;
import com.fintrack.fintrack_dashboard.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final SecurityUtils securityUtils;
    private final TransactionValidator validator;

    public TransactionService(TransactionRepository transactionRepository,
                              TransactionMapper transactionMapper,
                              SecurityUtils securityUtils,
                              TransactionValidator validator) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.securityUtils = securityUtils;
        this.validator = validator;
    }

    // ============================
    // CREATE
    // ============================
    public TransactionResponse createTransaction(CreateTransactionRequest request) {

        User user = securityUtils.getCurrentUser();

        log.info("Creating transaction for userId: {}", user.getId());

        Transaction transaction = transactionMapper.toEntity(request);

        transaction.setUser(user);
        transaction.setStatus(TransactionStatus.PENDING);

        Transaction saved = transactionRepository.save(transaction);

        log.info("Transaction created with id: {}", saved.getId());

        return transactionMapper.toResponse(saved);
    }

    // ============================
    // GET (FILTER + PAGINATION)
    // ============================
    public Page<TransactionResponse> getTransactions(TransactionFilterRequest filter,
                                                     int page,
                                                     int size) {

        User user = securityUtils.getCurrentUser();

        log.info("Fetching transactions | userId: {}, filter: {}", user.getId(), filter);

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

        Page<Transaction> transactions;

        if (securityUtils.isAdmin(user)) {
            transactions = transactionRepository.findAll(
                    TransactionSpecification.getTransactions(filter),
                    pageable
            );
        } else {
            transactions = transactionRepository.findAll(
                    TransactionSpecification.getTransactions(filter)
                            .and((root, query, cb) ->
                                    cb.equal(root.get("createdBy").get("id"), user.getId())),
                    pageable
            );
        }

        return transactions.map(transactionMapper::toResponse);
    }

    // ============================
    // GET BY ID
    // ============================
    public TransactionResponse getTransactionById(Long id) {

        log.info("Fetching transaction id: {}", id);

        Transaction transaction = getTransactionOrThrow(id);
        User user = securityUtils.getCurrentUser();

        validator.validateOwnership(transaction, user, securityUtils.isAdmin(user));

        return transactionMapper.toResponse(transaction);
    }

    // ============================
    // UPDATE
    // ============================
    public TransactionResponse updateTransaction(Long id,
                                                 CreateTransactionRequest request) {

        log.info("Updating transaction id: {}", id);

        Transaction transaction = getTransactionOrThrow(id);
        User user = securityUtils.getCurrentUser();

        validator.validateOwnership(transaction, user, securityUtils.isAdmin(user));
        validator.validatePending(transaction);

        transactionMapper.updateEntity(transaction, request);

        return transactionMapper.toResponse(
                transactionRepository.save(transaction)
        );
    }

    // ============================
    // DELETE
    // ============================
    public void deleteTransaction(Long id) {

        log.warn("Deleting transaction id: {}", id);

        Transaction transaction = getTransactionOrThrow(id);
        User user = securityUtils.getCurrentUser();

        validator.validateOwnership(transaction, user, securityUtils.isAdmin(user));
        validator.validatePending(transaction);

        transactionRepository.delete(transaction);
    }

    // ============================
    // APPROVE
    // ============================
    public TransactionResponse approveTransaction(Long id) {

        log.info("Approving transaction id: {}", id);

        Transaction transaction = getTransactionOrThrow(id);
        User user = securityUtils.getCurrentUser();

        validator.validateManagerOrAdmin(
                securityUtils.isAdmin(user),
                securityUtils.isManager(user)
        );

        validator.validatePending(transaction);

        transaction.setStatus(TransactionStatus.APPROVED);

        return transactionMapper.toResponse(
                transactionRepository.save(transaction)
        );
    }

    // ============================
    // REJECT
    // ============================
    public TransactionResponse rejectTransaction(Long id) {

        log.info("Rejecting transaction id: {}", id);

        Transaction transaction = getTransactionOrThrow(id);
        User user = securityUtils.getCurrentUser();

        validator.validateManagerOrAdmin(
                securityUtils.isAdmin(user),
                securityUtils.isManager(user)
        );

        validator.validatePending(transaction);

        transaction.setStatus(TransactionStatus.REJECTED);

        return transactionMapper.toResponse(
                transactionRepository.save(transaction)
        );
    }

    // ============================
    // HELPER
    // ============================
    private Transaction getTransactionOrThrow(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Transaction not found: {}", id);
                    return new ResourceNotFoundException("Transaction not found");
                });
    }
}