package com.fintrack.fintrack_dashboard.service.analytics;

import com.fintrack.fintrack_dashboard.constant.RecordType;
import com.fintrack.fintrack_dashboard.dto.common.PaginatedResponse;
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

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class AnalyticsService {

    private final TransactionRepository transactionRepository;

    private final SecurityUtils securityUtils;

    private final TransactionMapper transactionMapper;

    public AnalyticsService(
            TransactionRepository transactionRepository,
            SecurityUtils securityUtils,
            TransactionMapper transactionMapper
    ) {
        this.transactionRepository = transactionRepository;
        this.securityUtils = securityUtils;
        this.transactionMapper = transactionMapper;
    }

    private Long getScopeUserId(User user) {

        if (securityUtils.isEmployee(user)) {

            log.info(
                    "Applying employee-level analytics restriction for userId={}",
                    user.getId()
            );

            return user.getId();
        }

        log.info(
                "Admin-level analytics access granted for userId={}",
                user.getId()
        );

        return null;
    }

    public DashboardSummaryResponse getSummary(
            LocalDate startDate,
            LocalDate endDate
    ) {

        User currentUser =
                securityUtils.getCurrentUser();

        log.info(
                "Fetching dashboard summary | userId={}, startDate={}, endDate={}",
                currentUser.getId(),
                startDate,
                endDate
        );

        validateDateRange(startDate, endDate);

        Long scopeUserId =
                getScopeUserId(currentUser);

        Double income =
                transactionRepository.getTotalIncome(
                        scopeUserId,
                        startDate,
                        endDate
                );

        Double expense =
                transactionRepository.getTotalExpense(
                        scopeUserId,
                        startDate,
                        endDate
                );

        DashboardSummaryResponse response =
                new DashboardSummaryResponse();

        response.setTotalIncome(
                defaultZero(income)
        );

        response.setTotalExpense(
                defaultZero(expense)
        );

        response.setNetBalance(
                defaultZero(income)
                        - defaultZero(expense)
        );

        log.info(
                "Dashboard summary fetched successfully | userId={}, totalIncome={}, totalExpense={}, netBalance={}",
                currentUser.getId(),
                response.getTotalIncome(),
                response.getTotalExpense(),
                response.getNetBalance()
        );

        return response;
    }

    public List<CategorySummaryResponse>
    getCategorySummary(
            LocalDate startDate,
            LocalDate endDate
    ) {

        User currentUser =
                securityUtils.getCurrentUser();

        log.info(
                "Fetching category summary | userId={}, startDate={}, endDate={}",
                currentUser.getId(),
                startDate,
                endDate
        );

        validateDateRange(startDate, endDate);

        Long scopeUserId =
                getScopeUserId(currentUser);

        List<CategorySummaryResponse> response =
                transactionRepository
                        .getCategorySummary(
                                scopeUserId,
                                startDate,
                                endDate
                        )
                        .stream()
                        .map(this::mapCategoryRow)
                        .toList();

        log.info(
                "Category summary fetched successfully | userId={}, categoriesCount={}",
                currentUser.getId(),
                response.size()
        );

        return response;
    }

    public List<MonthlyTrendResponse>
    getMonthlyTrends(
            LocalDate startDate,
            LocalDate endDate
    ) {

        User currentUser =
                securityUtils.getCurrentUser();

        log.info(
                "Fetching monthly trends | userId={}, startDate={}, endDate={}",
                currentUser.getId(),
                startDate,
                endDate
        );

        validateDateRange(startDate, endDate);

        Long scopeUserId =
                getScopeUserId(currentUser);

        List<MonthlyTrendResponse> response =
                transactionRepository
                        .getMonthlyTrends(
                                scopeUserId,
                                startDate,
                                endDate
                        )
                        .stream()
                        .map(this::mapTrendRow)
                        .toList();

        log.info(
                "Monthly trends fetched successfully | userId={}, trendsCount={}",
                currentUser.getId(),
                response.size()
        );

        return response;
    }

    public PaginatedResponse<TransactionResponse>
    getRecentTransactions(
            int page,
            int size
    ) {

        User currentUser =
                securityUtils.getCurrentUser();

        log.info(
                "Fetching recent transactions | userId={}, page={}, size={}",
                currentUser.getId(),
                page,
                size
        );

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "date")
        );

        Page<Transaction> transactions;

        /*
         * EMPLOYEE users can only access
         * their own transactions.
         */
        if (securityUtils.isEmployee(currentUser)) {

            log.info(
                    "Applying employee-level restriction for recent transactions | userId={}",
                    currentUser.getId()
            );

            transactions =
                    transactionRepository.findByUserId(
                            currentUser.getId(),
                            pageable
                    );

        } else {

            log.info(
                    "Admin-level access granted for recent transactions | userId={}",
                    currentUser.getId()
            );

            transactions =
                    transactionRepository.findAll(
                            pageable
                    );
        }

        Page<TransactionResponse> responsePage =
                transactions.map(
                        transactionMapper::toResponse
                );

        log.info(
                "Recent transactions fetched successfully | userId={}, totalElements={}",
                currentUser.getId(),
                responsePage.getTotalElements()
        );

        return PaginatedResponse
                .<TransactionResponse>builder()
                .content(responsePage.getContent())
                .page(responsePage.getNumber())
                .size(responsePage.getSize())
                .totalElements(
                        responsePage.getTotalElements()
                )
                .totalPages(
                        responsePage.getTotalPages()
                )
                .first(responsePage.isFirst())
                .last(responsePage.isLast())
                .build();
    }

    private CategorySummaryResponse mapCategoryRow(
            Object[] row
    ) {

        CategorySummaryResponse response =
                new CategorySummaryResponse();

        response.setCategory(
                (String) row[0]
        );

        if (row[1] instanceof RecordType recordType) {
            response.setType(recordType);
        }

        response.setTotal(
                toDouble(row[2])
        );

        return response;
    }

    private MonthlyTrendResponse mapTrendRow(
            Object[] row
    ) {

        int year =
                ((Number) row[0]).intValue();

        int month =
                ((Number) row[1]).intValue();

        MonthlyTrendResponse response =
                new MonthlyTrendResponse();

        response.setMonth(
                String.format("%d-%02d", year, month)
        );

        response.setTotalIncome(
                toDouble(row[2])
        );

        response.setTotalExpense(
                toDouble(row[3])
        );

        return response;
    }

    private static double defaultZero(
            Double value
    ) {

        return value == null
                ? 0d
                : value;
    }

    private static double toDouble(
            Object value
    ) {

        if (value == null) {
            return 0d;
        }

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        return 0d;
    }

    private void validateDateRange(
            LocalDate start,
            LocalDate end
    ) {

        if (start != null
                && end != null
                && start.isAfter(end)) {

            log.error(
                    "Invalid date range | startDate={}, endDate={}",
                    start,
                    end
            );

            throw new BadRequestException(
                    "Start date cannot be after end date"
            );
        }
    }
}