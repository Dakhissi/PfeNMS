package com.farukgenc.boilerplate.springboot.dto;

import lombok.*;

/**
 * DTO for acknowledging alerts
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertAcknowledgeRequest {

    private String comment;
}
