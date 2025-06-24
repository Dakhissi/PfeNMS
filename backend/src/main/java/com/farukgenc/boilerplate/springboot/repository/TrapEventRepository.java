package com.farukgenc.boilerplate.springboot.repository;

import com.farukgenc.boilerplate.springboot.model.TrapEvent;
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
public interface TrapEventRepository extends JpaRepository<TrapEvent, Long> {

    /**
     * Find trap event by hash key (for duplicate detection)
     */
    Optional<TrapEvent> findByHashKey(String hashKey);

    /**
     * Find trap events by source IP
     */
    List<TrapEvent> findBySourceIpOrderByCreatedAtDesc(String sourceIp);

    /**
     * Find trap events by device
     */
    List<TrapEvent> findByDeviceIdOrderByCreatedAtDesc(Long deviceId);

    /**
     * Find trap events by user
     */
    List<TrapEvent> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find trap events by user with pagination
     */
    Page<TrapEvent> findByUser(User user, Pageable pageable);

    /**
     * Find unprocessed trap events
     */
    List<TrapEvent> findByProcessedFalseOrderByCreatedAtAsc();

    /**
     * Find trap events by trap type
     */
    List<TrapEvent> findByTrapTypeOrderByCreatedAtDesc(TrapEvent.TrapType trapType);

    /**
     * Find trap events by severity
     */
    List<TrapEvent> findBySeverityOrderByCreatedAtDesc(TrapEvent.TrapSeverity severity);

    /**
     * Find trap events within time range
     */
    @Query("SELECT t FROM TrapEvent t WHERE t.createdAt >= :startTime AND t.createdAt <= :endTime ORDER BY t.createdAt DESC")
    List<TrapEvent> findByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * Find trap events by user within time range
     */
    @Query("SELECT t FROM TrapEvent t WHERE t.user = :user AND t.createdAt >= :startTime ORDER BY t.createdAt DESC")
    List<TrapEvent> findByUserAndCreatedAtAfter(@Param("user") User user, 
                                                @Param("startTime") LocalDateTime startTime);

    /**
     * Count trap events by user
     */
    long countByUser(User user);

    /**
     * Count unprocessed trap events by user
     */
    long countByUserAndProcessedFalse(User user);

    /**
     * Count trap events by severity and user
     */
    long countByUserAndSeverity(User user, TrapEvent.TrapSeverity severity);

    /**
     * Delete old trap events (cleanup)
     */
    @Query("DELETE FROM TrapEvent t WHERE t.createdAt < :cutoffDate")
    void deleteOldTrapEvents(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find recent trap events that might be duplicates
     */
    @Query("SELECT t FROM TrapEvent t WHERE t.sourceIp = :sourceIp AND t.trapOid = :trapOid " +
           "AND t.createdAt >= :since ORDER BY t.createdAt DESC")
    List<TrapEvent> findPotentialDuplicates(@Param("sourceIp") String sourceIp,
                                           @Param("trapOid") String trapOid,
                                           @Param("since") LocalDateTime since);
}
