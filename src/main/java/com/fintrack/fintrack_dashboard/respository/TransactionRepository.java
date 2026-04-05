package com.fintrack.fintrack_dashboard.respository;

import com.fintrack.fintrack_dashboard.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;


import java.time.LocalDate;
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
    SELECT t.category, SUM(t.amount)
    FROM Transaction t
    WHERE t.user.id = :userId
    GROUP BY t.category
""")
    List<Object[]> getCategorySummary(Long userId);

    @Query("""
    SELECT FUNCTION('TO_CHAR', t.date, 'YYYY-MM'),
           SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END),
           SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END)
    FROM Transaction t
    WHERE t.user.id = :userId
    GROUP BY FUNCTION('TO_CHAR', t.date, 'YYYY-MM')
    ORDER BY 1
""")
    List<Object[]> getMonthlyTrends(Long userId);

    List<Transaction> findTop5ByUserIdOrderByDateDesc(Long userId);
}