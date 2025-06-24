package com.farukgenc.boilerplate.springboot.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utility class for parsing hex and SNMP data formats
 */
@Slf4j
@Component
public class SnmpDataParser {

    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-fA-F\\s:.-]+$");
    private static final Pattern MAC_PATTERN = Pattern.compile("^([0-9a-fA-F]{2}[:-]){5}[0-9a-fA-F]{2}$");

    /**
     * Parse hex string to normal string representation
     */
    public String parseHexToString(String hexValue) {
        if (hexValue == null || hexValue.trim().isEmpty()) {
            return null;
        }

        try {
            // Remove common hex prefixes and separators
            String cleaned = hexValue.replaceAll("^(0x|0X)", "")
                                   .replaceAll("[\\s:-]", "");

            // Check if it's a valid hex string
            if (!cleaned.matches("^[0-9a-fA-F]+$")) {
                log.debug("Not a valid hex string: {}", hexValue);
                return hexValue; // Return original if not hex
            }

            // Convert to ASCII if possible
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < cleaned.length() - 1; i += 2) {
                String hex = cleaned.substring(i, i + 2);
                int decimal = Integer.parseInt(hex, 16);
                
                // Only convert printable ASCII characters (32-126)
                if (decimal >= 32 && decimal <= 126) {
                    result.append((char) decimal);
                } else {
                    // For non-printable chars, keep as hex
                    result.append("\\x").append(hex.toUpperCase());
                }
            }

            return result.toString();
        } catch (Exception e) {
            log.warn("Failed to parse hex string '{}': {}", hexValue, e.getMessage());
            return hexValue; // Return original on error
        }
    }

    /**
     * Format MAC address from various input formats
     */
    public String formatMacAddress(String macInput) {
        if (macInput == null || macInput.trim().isEmpty()) {
            return null;
        }

        try {
            // Remove any existing formatting and convert to uppercase
            String cleaned = macInput.replaceAll("[^0-9a-fA-F]", "").toUpperCase();
            
            if (cleaned.length() != 12) {
                log.debug("Invalid MAC address length: {}", macInput);
                return macInput; // Return original if not standard length
            }

            // Format as XX:XX:XX:XX:XX:XX
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < cleaned.length(); i += 2) {
                if (i > 0) formatted.append(":");
                formatted.append(cleaned.substring(i, i + 2));
            }

            return formatted.toString();
        } catch (Exception e) {
            log.warn("Failed to format MAC address '{}': {}", macInput, e.getMessage());
            return macInput;
        }
    }

    /**
     * Parse IP address from hex format
     */
    public String parseHexToIpAddress(String hexIp) {
        if (hexIp == null || hexIp.trim().isEmpty()) {
            return null;
        }

        try {
            String cleaned = hexIp.replaceAll("[^0-9a-fA-F]", "");
            
            if (cleaned.length() != 8) { // 4 bytes = 8 hex chars for IPv4
                log.debug("Invalid hex IP address length: {}", hexIp);
                return hexIp;
            }

            StringBuilder ip = new StringBuilder();
            for (int i = 0; i < cleaned.length(); i += 2) {
                if (i > 0) ip.append(".");
                String hex = cleaned.substring(i, i + 2);
                ip.append(Integer.parseInt(hex, 16));
            }

            return ip.toString();
        } catch (Exception e) {
            log.warn("Failed to parse hex IP address '{}': {}", hexIp, e.getMessage());
            return hexIp;
        }
    }

    /**
     * Convert SNMP TimeTicks to human readable duration
     */
    public String parseTimeTicks(String timeTicks) {
        if (timeTicks == null || timeTicks.trim().isEmpty()) {
            return null;
        }

        try {
            long ticks = Long.parseLong(timeTicks);
            long seconds = ticks / 100; // TimeTicks are in centiseconds
            
            long days = seconds / 86400;
            seconds %= 86400;
            long hours = seconds / 3600;
            seconds %= 3600;
            long minutes = seconds / 60;
            seconds %= 60;

            if (days > 0) {
                return String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);
            } else if (hours > 0) {
                return String.format("%d hours, %d minutes, %d seconds", hours, minutes, seconds);
            } else if (minutes > 0) {
                return String.format("%d minutes, %d seconds", minutes, seconds);
            } else {
                return String.format("%d seconds", seconds);
            }
        } catch (Exception e) {
            log.warn("Failed to parse TimeTicks '{}': {}", timeTicks, e.getMessage());
            return timeTicks;
        }
    }

    /**
     * Parse SNMP Gauge32 value
     */
    public Long parseGauge32(String gauge) {
        if (gauge == null || gauge.trim().isEmpty()) {
            return null;
        }

        try {
            return Long.parseLong(gauge);
        } catch (Exception e) {
            log.warn("Failed to parse Gauge32 '{}': {}", gauge, e.getMessage());
            return null;
        }
    }

    /**
     * Parse SNMP Counter32/Counter64 value
     */
    public Long parseCounter(String counter) {
        if (counter == null || counter.trim().isEmpty()) {
            return null;
        }

        try {
            return Long.parseLong(counter);
        } catch (Exception e) {
            log.warn("Failed to parse Counter '{}': {}", counter, e.getMessage());
            return null;
        }
    }

    /**
     * Check if string appears to be in hex format
     */
    public boolean isHexFormat(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        String cleaned = input.replaceAll("[\\s:-]", "");
        return HEX_PATTERN.matcher(cleaned).matches() && cleaned.length() % 2 == 0;
    }

    /**
     * Check if string is a valid MAC address format
     */
    public boolean isMacAddress(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        return MAC_PATTERN.matcher(input).matches() || 
               input.replaceAll("[^0-9a-fA-F]", "").length() == 12;
    }
}
