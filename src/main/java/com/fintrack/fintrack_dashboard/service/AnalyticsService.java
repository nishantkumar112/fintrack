package com.fintrack.fintrack_dashboard.service;

import com.fintrack.fintrack_dashboard.constant.RecordType;
import com.fintrack.fintrack_dashboard.dto.dashboard.CategorySummaryResponse;
import com.fintrack.fintrack_dashboard.dto.dashboard.DashboardSummaryResponse;
import com.fintrack.fintrack_dashboard.dto.dashboard.MonthlyTrendResponse;
import com.fintrack.fintrack_dashboard.dto.transaction.TransactionResponse;
import com.fintrack.fintrack_dashboard.entity.Transaction;
import com.fintrack.fintrack_dashboard.entity.User;
import com.fintrack.fintrack_dashboard.exception.BadRequestException;
import com.fintrack.fintrack_dashboard.mapper.TransactionMapper;
import com.fintrack.fintrack_dashboard.respository.TransactionRepository;
import com.fintrack.fintrack_dashboard.utils.SecurityUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final SecurityUtils securityUtils;
    private final TransactionMapper transactionMapper;

    public AnalyticsService(
            TransactionRepository transactionRepository,
            SecurityUtils securityUtils,
            TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.securityUtils = securityUtils;
        this.transactionMapper = transactionMapper;
    }

    private Long getScopeUserId(User user) {
        if (securityUtils.isEmployee(user)) {
            return user.getId();
        }
        return null;
    }

    public DashboardSummaryResponse getSummary(LocalDate startDate, LocalDate endDate) {

        User user = securityUtils.getCurrentUser();
        validateDateRange(startDate, endDate);

        Long scopeId = getScopeUserId(user);

        Double income = transactionRepository.getTotalIncome(scopeId, startDate, endDate);
        Double expense = transactionRepository.getTotalExpense(scopeId, startDate, endDate);

        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setTotalIncome(defaultZero(income));
        response.setTotalExpense(defaultZero(expense));
        response.setNetBalance(defaultZero(income) - defaultZero(expense));

        return response;
    }

    public List<CategorySummaryResponse> getCategorySummary(LocalDate startDate, LocalDate endDate) {

        User user = securityUtils.getCurrentUser();
        validateDateRange(startDate, endDate);

        Long scopeId = getScopeUserId(user);

        return transactionRepository.getCategorySummary(scopeId, startDate, endDate)
                .stream()
                .map(this::mapCategoryRow)
                .toList();
    }

    public List<MonthlyTrendResponse> getMonthlyTrends(LocalDate startDate, LocalDate endDate) {

        User user = securityUtils.getCurrentUser();
        validateDateRange(startDate, endDate);

        Long scopeId = getScopeUserId(user);

        return transactionRepository.getMonthlyTrends(scopeId, startDate, endDate)
                .stream()
                .map(this::mapTrendRow)
                .toList();
    }

    public Page<TransactionResponse> getRecentTransactions(int page, int size) {

        User user = securityUtils.getCurrentUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

        Page<Transaction> transactions;

        if (securityUtils.isEmployee(user)) {
            transactions = transactionRepository.findByUserId(user.getId(), pageable);
        } else {
            transactions = transactionRepository.findAll(pageable);
        }

        return transactions.map(transactionMapper::toResponse);
    }

    private CategorySummaryResponse mapCategoryRow(Object[] row) {
        CategorySummaryResponse res = new CategorySummaryResponse();
        res.setCategory((String) row[0]);

        if (row[1] instanceof RecordType rt) {
            res.setType(rt);
        }

        res.setTotal(toDouble(row[2]));
        return res;
    }

    private MonthlyTrendResponse mapTrendRow(Object[] row) {
        int year = ((Number) row[0]).intValue();
        int month = ((Number) row[1]).intValue();

        MonthlyTrendResponse res = new MonthlyTrendResponse();
        res.setMonth(String.format("%d-%02d", year, month));
        res.setTotalIncome(toDouble(row[2]));
        res.setTotalExpense(toDouble(row[3]));

        return res;
    }

    private static double defaultZero(Double value) {
        return value == null ? 0d : value;
    }

    private static double toDouble(Object value) {
        if (value == null) return 0d;
        if (value instanceof Number n) return n.doubleValue();
        return 0d;
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new BadRequestException("Start date cannot be after end date");
        }
    }
}