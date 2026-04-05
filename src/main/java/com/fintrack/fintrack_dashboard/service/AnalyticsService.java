package com.fintrack.fintrack_dashboard.service;

import com.fintrack.fintrack_dashboard.dto.dashboard.*;
import com.fintrack.fintrack_dashboard.dto.transaction.TransactionResponse;
import com.fintrack.fintrack_dashboard.entity.Transaction;
import com.fintrack.fintrack_dashboard.entity.User;
import com.fintrack.fintrack_dashboard.mapper.TransactionMapper;
import com.fintrack.fintrack_dashboard.respository.TransactionRepository;
import com.fintrack.fintrack_dashboard.utils.SecurityUtils;
import com.fintrack.fintrack_dashboard.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final TransactionRepository transactionRepository;
    private final SecurityUtils securityUtils;
    private final TransactionMapper transactionMapper;

    public AnalyticsService(TransactionRepository transactionRepository,
                            SecurityUtils securityUtils,
                            TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.securityUtils = securityUtils;
        this.transactionMapper = transactionMapper;
    }

    // ============================
    // SUMMARY (FILTER + ADMIN SUPPORT)
    // ============================
    public DashboardSummaryResponse getSummary(LocalDate startDate, LocalDate endDate) {

        User user = securityUtils.getCurrentUser();

        log.info("Fetching summary | userId: {}, startDate: {}, endDate: {}",
                user.getId(), startDate, endDate);

        validateDateRange(startDate, endDate);

        Double income;
        Double expense;

        if (securityUtils.isAdmin(user)) {
            income = transactionRepository.getTotalIncome(null, startDate, endDate);
            expense = transactionRepository.getTotalExpense(null, startDate, endDate);
        } else {
            income = transactionRepository.getTotalIncome(user.getId(), startDate, endDate);
            expense = transactionRepository.getTotalExpense(user.getId(), startDate, endDate);
        }

        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setTotalIncome(income);
        response.setTotalExpense(expense);
        response.setNetBalance(income - expense);

        return response;
    }

    // ============================
    // CATEGORY SUMMARY (FILTERED)
    // ============================
    public List<CategorySummaryResponse> getCategorySummary(LocalDate startDate,
                                                            LocalDate endDate) {

        User user = securityUtils.getCurrentUser();

        log.info("Fetching category summary | userId: {}", user.getId());

        validateDateRange(startDate, endDate);

        List<Object[]> result = transactionRepository.getCategorySummary(user.getId());

        return result.stream()
                .map(obj -> {
                    CategorySummaryResponse res = new CategorySummaryResponse();
                    res.setCategory((String) obj[0]);
                    res.setTotal((Double) obj[1]);
                    return res;
                })
                .toList();
    }

    // ============================
    // MONTHLY TREND (FILTERED)
    // ============================
    public List<MonthlyTrendResponse> getMonthlyTrends(LocalDate startDate,
                                                       LocalDate endDate) {

        User user = securityUtils.getCurrentUser();

        log.info("Fetching trends | userId: {}", user.getId());

        validateDateRange(startDate, endDate);

        return transactionRepository.getMonthlyTrends(user.getId())
                .stream()
                .map(obj -> {
                    MonthlyTrendResponse res = new MonthlyTrendResponse();
                    res.setMonth((String) obj[0]);
                    res.setTotalIncome((Double) obj[1]);
                    res.setTotalExpense((Double) obj[2]);
                    return res;
                })
                .toList();
    }

    // ============================
    // RECENT (PAGINATION)
    // ============================
    public Page<TransactionResponse> getRecentTransactions(int page, int size) {

        User user = securityUtils.getCurrentUser();

        log.info("Fetching recent transactions | userId: {}, page: {}, size: {}",
                user.getId(), page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

        Page<Transaction> transactions;

        if (securityUtils.isAdmin(user)) {
            transactions = transactionRepository.findAll(pageable);
        } else {
            transactions = transactionRepository.findByUserId(user.getId(), pageable);
        }

        return transactions.map(transactionMapper::toResponse);
    }

    // ============================
    // VALIDATION
    // ============================
    private void validateDateRange(LocalDate start, LocalDate end) {

        if (start != null && end != null && start.isAfter(end)) {
            log.warn("Invalid date range | start: {}, end: {}", start, end);
            throw new BadRequestException("Start date cannot be after end date");
        }
    }
}