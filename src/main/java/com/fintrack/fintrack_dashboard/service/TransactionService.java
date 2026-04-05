package com.fintrack.fintrack_dashboard.service;

import com.fintrack.fintrack_dashboard.constant.TransactionStatus;
import com.fintrack.fintrack_dashboard.dto.transaction.*;
import com.fintrack.fintrack_dashboard.entity.Transaction;
import com.fintrack.fintrack_dashboard.entity.User;
import com.fintrack.fintrack_dashboard.exception.ResourceNotFoundException;
import com.fintrack.fintrack_dashboard.mapper.TransactionMapper;
import com.fintrack.fintrack_dashboard.respository.TransactionRepository;
import com.fintrack.fintrack_dashboard.utils.SecurityUtils;
import com.fintrack.fintrack_dashboard.utils.TransactionValidator;
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

    // ---------------- CREATE ----------------
    public TransactionResponse createTransaction(CreateTransactionRequest request) {

        User user = securityUtils.getCurrentUser();

        log.info("Creating transaction | userId: {}", user.getId());

        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setUser(user);
        transaction.setStatus(TransactionStatus.PENDING);

        return transactionMapper.toResponse(
                transactionRepository.save(transaction)
        );
    }

    // ---------------- READ ALL (RBAC CORE LOGIC) ----------------
    public Page<TransactionResponse> getTransactions(TransactionFilterRequest filter,
                                                     int page,
                                                     int size) {

        User user = securityUtils.getCurrentUser();

        log.info("Fetching transactions | userId: {}, role-based filtering applied", user.getId());

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

        var baseSpec = TransactionSpecification.getTransactions(filter);

        // EMPLOYEE → restrict to own data
        if (securityUtils.isEmployee(user)) {
            baseSpec = baseSpec.and((root, query, cb) ->
                    cb.equal(root.get("user").get("id"), user.getId())
            );
        }

        Page<Transaction> transactions = transactionRepository.findAll(baseSpec, pageable);

        return transactions.map(transactionMapper::toResponse);
    }

    public TransactionResponse getTransactionById(Long id) {

        User user = securityUtils.getCurrentUser();

        Transaction transaction = getTransactionOrThrow(id);

        log.info("Fetching transaction id: {} by userId: {}", id, user.getId());

        validator.validateOwnership(transaction, user, securityUtils.isAdmin(user));

        return transactionMapper.toResponse(transaction);
    }

    public TransactionResponse updateTransaction(Long id,
                                                 CreateTransactionRequest request) {

        User user = securityUtils.getCurrentUser();

        Transaction transaction = getTransactionOrThrow(id);

        log.info("Updating transaction id: {} by userId: {}", id, user.getId());

        validator.validateOwnership(transaction, user, securityUtils.isAdmin(user));
        validator.validatePending(transaction);

        transactionMapper.updateEntity(transaction, request);

        return transactionMapper.toResponse(
                transactionRepository.save(transaction)
        );
    }

    public void deleteTransaction(Long id) {

        User user = securityUtils.getCurrentUser();

        Transaction transaction = getTransactionOrThrow(id);

        log.warn("Deleting transaction id: {} by userId: {}", id, user.getId());

        validator.validateOwnership(transaction, user, securityUtils.isAdmin(user));
        validator.validatePending(transaction);

        transactionRepository.delete(transaction);
    }

    public TransactionResponse approveTransaction(Long id) {

        User user = securityUtils.getCurrentUser();

        Transaction transaction = getTransactionOrThrow(id);

        log.info("Approving transaction id: {} by userId: {}", id, user.getId());

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

    public TransactionResponse rejectTransaction(Long id) {

        User user = securityUtils.getCurrentUser();

        Transaction transaction = getTransactionOrThrow(id);

        log.info("Rejecting transaction id: {} by userId: {}", id, user.getId());

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

    private Transaction getTransactionOrThrow(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Transaction not found: {}", id);
                    return new ResourceNotFoundException("Transaction not found");
                });
    }
}