package com.farukgenc.boilerplate.springboot.service.trap;

import com.farukgenc.boilerplate.springboot.dto.TrapEventDto;
import com.farukgenc.boilerplate.springboot.model.TrapEvent;
import com.farukgenc.boilerplate.springboot.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface for handling SNMP trap events
 */
public interface TrapService {

    /**
     * Start the SNMP trap receiver
     */
    void startTrapReceiver();

    /**
     * Stop the SNMP trap receiver
     */
    void stopTrapReceiver();

    /**
     * Check if trap receiver is running
     */
    boolean isTrapReceiverRunning();

    /**
     * Process received trap event
     */
    TrapEventDto processTrapEvent(String sourceIp, int sourcePort, String community,
                                  String trapOid, Map<String, Object> variableBindings);

    /**
     * Get trap events by user
     */
    List<TrapEventDto> getTrapEventsByUser(User user);

    /**
     * Get trap events by user with pagination
     */
    Page<TrapEventDto> getTrapEventsByUser(User user, Pageable pageable);

    /**
     * Get trap event by ID
     */
    TrapEventDto getTrapEventById(Long id, User user);

    /**
     * Get trap events by source IP
     */
    List<TrapEventDto> getTrapEventsBySourceIp(String sourceIp, User user);

    /**
     * Get trap events by device
     */
    List<TrapEventDto> getTrapEventsByDevice(Long deviceId, User user);

    /**
     * Get trap events by trap type
     */
    List<TrapEventDto> getTrapEventsByType(TrapEvent.TrapType trapType, User user);

    /**
     * Get trap events by severity
     */
    List<TrapEventDto> getTrapEventsBySeverity(TrapEvent.TrapSeverity severity, User user);

    /**
     * Get recent trap events
     */
    List<TrapEventDto> getRecentTrapEvents(User user, LocalDateTime since);

    /**
     * Get unprocessed trap events count
     */
    long getUnprocessedTrapCount(User user);

    /**
     * Get trap event statistics
     */
    Map<String, Object> getTrapEventStatistics(User user);

    /**
     * Mark trap event as processed
     */
    void markTrapEventAsProcessed(Long id, User user);

    /**
     * Delete old trap events (cleanup)
     */
    void cleanupOldTrapEvents(int daysToKeep);

    /**
     * Process all unprocessed trap events
     */
    void processUnprocessedTraps();

    /**
     * Configure trap receiver settings
     */
    void configureTrapReceiver(int port, String community);
}
