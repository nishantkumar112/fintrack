package com.fintrack.fintrack_dashboard.service;

import com.fintrack.fintrack_dashboard.entity.Transaction;
import com.fintrack.fintrack_dashboard.dto.transaction.TransactionFilterRequest;
import org.springframework.data.jpa.domain.Specification;

public class TransactionSpecification {

    public static Specification<Transaction> getTransactions(TransactionFilterRequest filter) {

        return (root, query, cb) -> {

            var predicate = cb.conjunction();

            if (filter.getType() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("type"), filter.getType()));
            }

            if (filter.getCategory() != null) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("category")),
                                "%" + filter.getCategory().toLowerCase() + "%"));
            }

            if (filter.getStatus() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getStartDate() != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("date"), filter.getStartDate()));
            }

            if (filter.getEndDate() != null) {
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(root.get("date"), filter.getEndDate()));
            }

            return predicate;
        };
    }
}