package com.farukgenc.boilerplate.springboot.service.trap;

import com.farukgenc.boilerplate.springboot.dto.TrapEventDto;
import com.farukgenc.boilerplate.springboot.mapper.TrapMapper;
import com.farukgenc.boilerplate.springboot.model.TrapEvent;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.repository.TrapEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of TrapService for handling SNMP trap events
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrapServiceImpl implements TrapService {

    private final TrapReceiver trapReceiver;
    private final TrapProcessor trapProcessor;
    private final TrapEventRepository trapEventRepository;
    private final TrapMapper trapMapper;

    @Override
    public void startTrapReceiver() {
        try {
            trapReceiver.startTrapReceiver(162, "public");
            log.info("SNMP Trap Service started successfully");
        } catch (Exception e) {
            log.error("Failed to start SNMP Trap Service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to start trap receiver", e);
        }
    }

    @Override
    public void stopTrapReceiver() {
        try {
            trapReceiver.stopTrapReceiver();
            log.info("SNMP Trap Service stopped successfully");
        } catch (Exception e) {
            log.error("Failed to stop SNMP Trap Service: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean isTrapReceiverRunning() {
        return trapReceiver.isRunning();
    }

    @Override
    public TrapEventDto processTrapEvent(String sourceIp, int sourcePort, String community,
                                        String trapOid, Map<String, Object> variableBindings) {
        try {
            trapProcessor.processTrap(sourceIp, sourcePort, community, trapOid, null,
                    null, null, System.currentTimeMillis(), variableBindings);
            
            // Find and return the created trap event
            Optional<TrapEvent> trapEvent = trapEventRepository.findBySourceIpOrderByCreatedAtDesc(sourceIp)
                    .stream().findFirst();
            
            return trapEvent.map(trapMapper::toDto).orElse(null);
            
        } catch (Exception e) {
            log.error("Failed to process trap event from {}: {}", sourceIp, e.getMessage(), e);
            throw new RuntimeException("Failed to process trap event", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrapEventDto> getTrapEventsByUser(User user) {
        List<TrapEvent> trapEvents = trapEventRepository.findByUserOrderByCreatedAtDesc(user);
        return trapEvents.stream()
                .map(trapMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TrapEventDto> getTrapEventsByUser(User user, Pageable pageable) {
        Page<TrapEvent> trapEvents = trapEventRepository.findByUser(user, pageable);
        return trapEvents.map(trapMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public TrapEventDto getTrapEventById(Long id, User user) {
        Optional<TrapEvent> trapEvent = trapEventRepository.findById(id);
        
        if (trapEvent.isEmpty()) {
            throw new IllegalArgumentException("Trap event not found with ID: " + id);
        }
        
        TrapEvent event = trapEvent.get();
        
        // Check if user has access to this trap event
        if (event.getUser() != null && !event.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied to trap event: " + id);
        }
        
        return trapMapper.toDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrapEventDto> getTrapEventsBySourceIp(String sourceIp, User user) {
        List<TrapEvent> trapEvents = trapEventRepository.findBySourceIpOrderByCreatedAtDesc(sourceIp);
        
        // Filter by user access
        return trapEvents.stream()
                .filter(event -> event.getUser() == null || event.getUser().getId().equals(user.getId()))
                .map(trapMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrapEventDto> getTrapEventsByDevice(Long deviceId, User user) {
        List<TrapEvent> trapEvents = trapEventRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId);
        
        // Filter by user access
        return trapEvents.stream()
                .filter(event -> event.getUser() == null || event.getUser().getId().equals(user.getId()))
                .map(trapMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrapEventDto> getTrapEventsByType(TrapEvent.TrapType trapType, User user) {
        List<TrapEvent> trapEvents = trapEventRepository.findByTrapTypeOrderByCreatedAtDesc(trapType);
        
        // Filter by user access
        return trapEvents.stream()
                .filter(event -> event.getUser() == null || event.getUser().getId().equals(user.getId()))
                .map(trapMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrapEventDto> getTrapEventsBySeverity(TrapEvent.TrapSeverity severity, User user) {
        List<TrapEvent> trapEvents = trapEventRepository.findBySeverityOrderByCreatedAtDesc(severity);
        
        // Filter by user access
        return trapEvents.stream()
                .filter(event -> event.getUser() == null || event.getUser().getId().equals(user.getId()))
                .map(trapMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrapEventDto> getRecentTrapEvents(User user, LocalDateTime since) {
        List<TrapEvent> trapEvents = trapEventRepository.findByUserAndCreatedAtAfter(user, since);
        return trapEvents.stream()
                .map(trapMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnprocessedTrapCount(User user) {
        return trapEventRepository.countByUserAndProcessedFalse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTrapEventStatistics(User user) {
        long totalCount = trapEventRepository.countByUser(user);
        long unprocessedCount = trapEventRepository.countByUserAndProcessedFalse(user);
        long criticalCount = trapEventRepository.countByUserAndSeverity(user, TrapEvent.TrapSeverity.CRITICAL);
        long majorCount = trapEventRepository.countByUserAndSeverity(user, TrapEvent.TrapSeverity.MAJOR);
        long minorCount = trapEventRepository.countByUserAndSeverity(user, TrapEvent.TrapSeverity.MINOR);
        
        return Map.of(
                "totalCount", totalCount,
                "unprocessedCount", unprocessedCount,
                "criticalCount", criticalCount,
                "majorCount", majorCount,
                "minorCount", minorCount,
                "processedCount", totalCount - unprocessedCount
        );
    }

    @Override
    public void markTrapEventAsProcessed(Long id, User user) {
        Optional<TrapEvent> trapEventOpt = trapEventRepository.findById(id);
        
        if (trapEventOpt.isEmpty()) {
            throw new IllegalArgumentException("Trap event not found with ID: " + id);
        }
        
        TrapEvent trapEvent = trapEventOpt.get();
        
        // Check user access
        if (trapEvent.getUser() != null && !trapEvent.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied to trap event: " + id);
        }
        
        trapEvent.setProcessed(true);
        trapEventRepository.save(trapEvent);
        
        log.info("Marked trap event {} as processed by user {}", id, user.getUsername());
    }

    @Override
    @Transactional
    public void cleanupOldTrapEvents(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        trapEventRepository.deleteOldTrapEvents(cutoffDate);
        log.info("Cleaned up trap events older than {} days", daysToKeep);
    }

    @Override
    @Transactional
    public void processUnprocessedTraps() {
        List<TrapEvent> unprocessedTraps = trapEventRepository.findByProcessedFalseOrderByCreatedAtAsc();
        
        for (TrapEvent trapEvent : unprocessedTraps) {
            try {
                // Re-process the trap to create alerts if needed
                // This is useful if the processing failed initially
                log.debug("Re-processing trap event: {}", trapEvent.getId());
                trapEvent.setProcessed(true);
                trapEventRepository.save(trapEvent);
                
            } catch (Exception e) {
                log.error("Failed to re-process trap event {}: {}", trapEvent.getId(), e.getMessage(), e);
            }
        }
        
        log.info("Processed {} unprocessed trap events", unprocessedTraps.size());
    }

    @Override
    public void configureTrapReceiver(int port, String community) {
        boolean wasRunning = isTrapReceiverRunning();
        
        if (wasRunning) {
            stopTrapReceiver();
        }
        
        try {
            trapReceiver.startTrapReceiver(port, community);
            log.info("Reconfigured trap receiver: port={}, community={}", port, community);
        } catch (Exception e) {
            log.error("Failed to reconfigure trap receiver: {}", e.getMessage(), e);
            if (wasRunning) {
                // Try to restart with default settings
                startTrapReceiver();
            }
            throw e;
        }
    }

    /**
     * Auto-start trap receiver on application startup
     */
    @PostConstruct
    public void autoStartTrapReceiver() {
        try {
            startTrapReceiver();
        } catch (Exception e) {
            log.warn("Failed to auto-start trap receiver: {}", e.getMessage());
        }
    }

    /**
     * Scheduled cleanup of old trap events (runs daily)
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM daily
    public void scheduledCleanup() {
        try {
            cleanupOldTrapEvents(30); // Keep trap events for 30 days
        } catch (Exception e) {
            log.error("Failed to perform scheduled cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Scheduled processing of unprocessed traps (runs every 10 minutes)
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void scheduledProcessUnprocessedTraps() {
        try {
            processUnprocessedTraps();
        } catch (Exception e) {
            log.error("Failed to process unprocessed traps: {}", e.getMessage(), e);
        }
    }
}
