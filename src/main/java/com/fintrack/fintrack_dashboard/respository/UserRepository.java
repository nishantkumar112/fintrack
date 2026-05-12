package com.fintrack.fintrack_dashboard.respository;

import com.fintrack.fintrack_dashboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);
    @Query("""
    SELECT u
    FROM User u
    WHERE u.role IN (
        com.fintrack.fintrack_dashboard.constant.Role.ADMIN,
        com.fintrack.fintrack_dashboard.constant.Role.MANAGER
    )
    AND u.status = com.fintrack.fintrack_dashboard.constant.UserStatus.ACTIVE
""")
    List<User> findManagersAndAdmins();
}