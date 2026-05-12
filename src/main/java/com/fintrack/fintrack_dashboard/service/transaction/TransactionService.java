package com.fintrack.fintrack_dashboard.service.transaction;

import com.fintrack.fintrack_dashboard.constant.NotificationType;
import com.fintrack.fintrack_dashboard.constant.TransactionStatus;
import com.fintrack.fintrack_dashboard.dto.common.PaginatedResponse;
import com.fintrack.fintrack_dashboard.dto.transaction.CreateTransactionRequest;
import com.fintrack.fintrack_dashboard.dto.transaction.TransactionFilterRequest;
import com.fintrack.fintrack_dashboard.dto.transaction.TransactionResponse;
import com.fintrack.fintrack_dashboard.entity.Transaction;
import com.fintrack.fintrack_dashboard.entity.User;
import com.fintrack.fintrack_dashboard.exception.ResourceNotFoundException;
import com.fintrack.fintrack_dashboard.mapper.TransactionMapper;
import com.fintrack.fintrack_dashboard.respository.TransactionRepository;
import com.fintrack.fintrack_dashboard.service.notification.NotificationService;
import com.fintrack.fintrack_dashboard.utils.SecurityUtils;
import com.fintrack.fintrack_dashboard.utils.TransactionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private static final Logger log =
            LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final SecurityUtils securityUtils;
    private final TransactionValidator validator;
    private final NotificationService notificationService;

    public TransactionService(
            TransactionRepository transactionRepository,
            TransactionMapper transactionMapper,
            SecurityUtils securityUtils,
            TransactionValidator validator,
            NotificationService notificationService
    ) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.securityUtils = securityUtils;
        this.validator = validator;
        this.notificationService = notificationService;
    }

    public TransactionResponse createTransaction(CreateTransactionRequest request) {

        User user = securityUtils.getCurrentUser();

        log.info("Creating transaction | userId={}", user.getId());

        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setUser(user);
        transaction.setStatus(TransactionStatus.PENDING);

        Transaction saved = transactionRepository.save(transaction);

//        notificationService.notifyApprovers(saved);

        log.info(
                "Transaction created successfully | transactionId={}",
                saved.getId()
        );

        return transactionMapper.toResponse(saved);
    }

    public PaginatedResponse<TransactionResponse> getTransactions(
            TransactionFilterRequest filter,
            int page,
            int size
    ) {

        User currentUser = securityUtils.getCurrentUser();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "date")
        );

        var specification =
                TransactionSpecification.getTransactions(filter);

        if (securityUtils.isEmployee(currentUser)) {

            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("user").get("id"),
                                    currentUser.getId()
                            )
            );
        }

        Page<Transaction> transactions =
                transactionRepository.findAll(
                        specification,
                        pageable
                );

        Page<TransactionResponse> responsePage =
                transactions.map(transactionMapper::toResponse);

        return PaginatedResponse.<TransactionResponse>builder()
                .content(responsePage.getContent())
                .page(responsePage.getNumber())
                .size(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .first(responsePage.isFirst())
                .last(responsePage.isLast())
                .build();
    }

    public TransactionResponse getTransactionById(Long id) {

        User user = securityUtils.getCurrentUser();

        Transaction transaction = getTransactionOrThrow(id);

        validator.validateOwnership(
                transaction,
                user,
                securityUtils.isAdmin(user)
        );

        return transactionMapper.toResponse(transaction);
    }

    public TransactionResponse updateTransaction(
            Long id,
            CreateTransactionRequest request
    ) {

        User user = securityUtils.getCurrentUser();

        Transaction transaction = getTransactionOrThrow(id);

        validator.validateOwnership(
                transaction,
                user,
                securityUtils.isAdmin(user)
        );

        validator.validatePending(transaction);

        transactionMapper.updateEntity(transaction, request);

        Transaction updated =
                transactionRepository.save(transaction);

        return transactionMapper.toResponse(updated);
    }

    public void deleteTransaction(Long id) {

        User user = securityUtils.getCurrentUser();

        Transaction transaction = getTransactionOrThrow(id);

        validator.validateOwnership(
                transaction,
                user,
                securityUtils.isAdmin(user)
        );

        validator.validatePending(transaction);

        transactionRepository.delete(transaction);
    }

    public TransactionResponse approveTransaction(Long id) {

        User approver = securityUtils.getCurrentUser();

        Transaction transaction = getTransactionOrThrow(id);

        validator.validateManagerOrAdmin(
                securityUtils.isAdmin(approver),
                securityUtils.isManager(approver)
        );

        validator.validatePending(transaction);

        transaction.setStatus(TransactionStatus.APPROVED);

        Transaction approved =
                transactionRepository.save(transaction);

        notificationService.notifyUser(
                transaction.getUser(),
                "Transaction Approved",
                "Your transaction has been approved.",
                NotificationType.TRANSACTION_APPROVED,
                "/transactions/" + approved.getId()
        );

        return transactionMapper.toResponse(approved);
    }

    public TransactionResponse rejectTransaction(Long id) {

        User approver = securityUtils.getCurrentUser();

        Transaction transaction = getTransactionOrThrow(id);

        validator.validateManagerOrAdmin(
                securityUtils.isAdmin(approver),
                securityUtils.isManager(approver)
        );

        validator.validatePending(transaction);

        transaction.setStatus(TransactionStatus.REJECTED);

        Transaction rejected =
                transactionRepository.save(transaction);

        notificationService.notifyUser(
                transaction.getUser(),
                "Transaction Rejected",
                "Your transaction has been rejected.",
                NotificationType.TRANSACTION_REJECTED,
                "/transactions/" + rejected.getId()
        );

        return transactionMapper.toResponse(rejected);
    }

    private Transaction getTransactionOrThrow(Long id) {

        return transactionRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn(
                            "Transaction not found | id={}",
                            id
                    );

                    return new ResourceNotFoundException(
                            "Transaction not found"
                    );
                });
    }
}