package com.fintrack.fintrack_dashboard.respository;

import com.fintrack.fintrack_dashboard.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {
    Page<Transaction> findByUserId(Long userId, Pageable pageable);
    @Query("""
SELECT COALESCE(SUM(t.amount), 0)
FROM Transaction t
WHERE t.type = 'INCOME'
  AND (:userId IS NULL OR t.user.id = :userId)
  AND (CAST(:startDate AS date) IS NULL OR t.date >= :startDate)
  AND (CAST(:endDate AS date) IS NULL OR t.date <= :endDate)
""")
    Double getTotalIncome(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("""
SELECT COALESCE(SUM(t.amount), 0)
FROM Transaction t
WHERE t.type = 'EXPENSE'
  AND (:userId IS NULL OR t.user.id = :userId)
  AND (CAST(:startDate AS date) IS NULL OR t.date >= :startDate)
  AND (CAST(:endDate AS date) IS NULL OR t.date <= :endDate)
""")
    Double getTotalExpense(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT COALESCE(NULLIF(TRIM(t.category), ''), 'Uncategorized'), t.type,
                   COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE (:userId IS NULL OR t.user.id = :userId)
              AND (CAST(:startDate AS date) IS NULL OR t.date >= :startDate)
              AND (CAST(:endDate AS date) IS NULL OR t.date <= :endDate)
            GROUP BY COALESCE(NULLIF(TRIM(t.category), ''), 'Uncategorized'), t.type
            ORDER BY t.type, SUM(t.amount) DESC
            """)
    List<Object[]> getCategorySummary(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT YEAR(t.date), MONTH(t.date),
                   COALESCE(SUM(CASE WHEN t.type = com.fintrack.fintrack_dashboard.constant.RecordType.INCOME
                       THEN t.amount ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN t.type = com.fintrack.fintrack_dashboard.constant.RecordType.EXPENSE
                       THEN t.amount ELSE 0 END), 0)
            FROM Transaction t
            WHERE (:userId IS NULL OR t.user.id = :userId)
              AND (CAST(:startDate AS date) IS NULL OR t.date >= :startDate)
              AND (CAST(:endDate AS date) IS NULL OR t.date <= :endDate)
            GROUP BY YEAR(t.date), MONTH(t.date)
            ORDER BY YEAR(t.date), MONTH(t.date)
            """)
    List<Object[]> getMonthlyTrends(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("""
    SELECT t
    FROM Transaction t
    WHERE t.status = 'PENDING'
    AND t.createdAt <= :cutoff
""")
    List<Transaction> findOldPendingTransactions(
            @Param("cutoff")
            LocalDateTime cutoff
    );
    List<Transaction> findTop5ByUserIdOrderByDateDesc(Long userId);
}