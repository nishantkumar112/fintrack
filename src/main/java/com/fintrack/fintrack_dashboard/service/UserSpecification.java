package com.fintrack.fintrack_dashboard.service;

import com.fintrack.fintrack_dashboard.entity.User;
import com.fintrack.fintrack_dashboard.dto.user.UserFilterRequest;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> getUsers(UserFilterRequest filter) {

        return (root, query, cb) -> {

            var predicates = cb.conjunction();

            if (filter.getName() != null) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("name")),
                                "%" + filter.getName().toLowerCase() + "%"));
            }

            if (filter.getEmail() != null) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("email")),
                                "%" + filter.getEmail().toLowerCase() + "%"));
            }

            if (filter.getRole() != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("role"), filter.getRole()));
            }

            if (filter.getStatus() != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("status"), filter.getStatus()));
            }

            return predicates;
        };
    }
}