package com.farukgenc.boilerplate.springboot.controller;

import com.farukgenc.boilerplate.springboot.dto.TrapEventDto;
import com.farukgenc.boilerplate.springboot.service.trap.TrapService;
import com.farukgenc.boilerplate.springboot.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing SNMP trap events
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/traps")
@RequiredArgsConstructor
public class TrapController {

    private final TrapService trapService;

    /**
     * Get paginated list of trap events for the authenticated user
     */
    @GetMapping
    public ResponseEntity<Page<TrapEventDto>> getTrapEvents(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<TrapEventDto> trapEvents = trapService.getTrapEventsByUser(user, pageable);
        return ResponseEntity.ok(trapEvents);
    }

    /**
     * Get trap event by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TrapEventDto> getTrapEvent(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        
        TrapEventDto trapEvent = trapService.getTrapEventById(id, user);
        return trapEvent != null ? ResponseEntity.ok(trapEvent) : ResponseEntity.notFound().build();
    }

    /**
     * Get trap events by device ID
     */
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<TrapEventDto>> getTrapEventsByDevice(
            @AuthenticationPrincipal User user,
            @PathVariable Long deviceId) {

        List<TrapEventDto> trapEvents = trapService.getTrapEventsByDevice(deviceId, user);
        return ResponseEntity.ok(trapEvents);
    }

    /**
     * Get trap events by source IP
     */
    @GetMapping("/source/{sourceIp}")
    public ResponseEntity<List<TrapEventDto>> getTrapEventsBySourceIp(
            @AuthenticationPrincipal User user,
            @PathVariable String sourceIp) {

        List<TrapEventDto> trapEvents = trapService.getTrapEventsBySourceIp(sourceIp, user);
        return ResponseEntity.ok(trapEvents);
    }

    /**
     * Get recent trap events
     */
    @GetMapping("/recent")
    public ResponseEntity<List<TrapEventDto>> getRecentTrapEvents(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) LocalDateTime since) {

        LocalDateTime sinceDate = since != null ? since : LocalDateTime.now().minusHours(24);
        List<TrapEventDto> trapEvents = trapService.getRecentTrapEvents(user, sinceDate);
        return ResponseEntity.ok(trapEvents);
    }

    /**
     * Get unprocessed trap events count
     */
    @GetMapping("/unprocessed/count")
    public ResponseEntity<Map<String, Object>> getUnprocessedTrapCount(
            @AuthenticationPrincipal User user) {

        long count = trapService.getUnprocessedTrapCount(user);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Mark trap event as processed
     */
    @PutMapping("/{id}/process")
    public ResponseEntity<Void> markTrapAsProcessed(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        
        trapService.markTrapEventAsProcessed(id, user);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete trap event
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrapEvent(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        
        // Note: Delete functionality would need to be added to TrapService interface
        // For now, just return not implemented
        return ResponseEntity.status(501).build(); // Not implemented
    }

    /**
     * Get trap statistics for the user
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getTrapStatistics(
            @AuthenticationPrincipal User user) {
        
        Map<String, Object> statistics = trapService.getTrapEventStatistics(user);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get trap receiver status
     */
    @GetMapping("/receiver/status")
    public ResponseEntity<Map<String, Object>> getTrapReceiverStatus() {
        boolean isRunning = trapService.isTrapReceiverRunning();
        return ResponseEntity.ok(Map.of("running", isRunning));
    }

    /**
     * Cleanup old trap events
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOldTrapEvents(
            @RequestParam(defaultValue = "30") int daysOld) {
        
        trapService.cleanupOldTrapEvents(daysOld);
        return ResponseEntity.ok(Map.of("message", "Cleanup completed", "daysOld", daysOld));
    }
}
