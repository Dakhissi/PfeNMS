package com.farukgenc.boilerplate.springboot.repository;

import com.farukgenc.boilerplate.springboot.model.Alert;
import com.farukgenc.boilerplate.springboot.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByUser(User user);

    Page<Alert> findByUser(User user, Pageable pageable);

    List<Alert> findByUserAndStatus(User user, Alert.AlertStatus status);

    List<Alert> findByUserAndSeverity(User user, Alert.AlertSeverity severity);

    List<Alert> findByUserAndType(User user, Alert.AlertType type);

    Optional<Alert> findByIdAndUser(Long id, User user);

    Optional<Alert> findByAlertKeyAndUser(String alertKey, User user);

    @Query("SELECT a FROM Alert a WHERE a.user = :user AND a.status = :status AND a.createdAt >= :since")
    List<Alert> findByUserAndStatusSince(@Param("user") User user, 
                                        @Param("status") Alert.AlertStatus status, 
                                        @Param("since") LocalDateTime since);

    @Query("SELECT a FROM Alert a WHERE a.user = :user AND a.acknowledged = false")
    List<Alert> findUnacknowledgedByUser(@Param("user") User user);

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.user = :user AND a.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") Alert.AlertStatus status);

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.user = :user AND a.severity = :severity AND a.status = 'ACTIVE'")
    long countActiveByUserAndSeverity(@Param("user") User user, @Param("severity") Alert.AlertSeverity severity);
}
