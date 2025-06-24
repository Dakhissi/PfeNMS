package com.farukgenc.boilerplate.springboot.event;

import com.farukgenc.boilerplate.springboot.dto.AlertDto;
import com.farukgenc.boilerplate.springboot.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event that is published when an alert is created or updated
 */
@Getter
public class AlertEvent extends ApplicationEvent {

    private final AlertDto alert;
    private final User user;
    private final EventType eventType;

    public AlertEvent(Object source, AlertDto alert, User user, EventType eventType) {
        super(source);
        this.alert = alert;
        this.user = user;
        this.eventType = eventType;
    }

    public enum EventType {
        NEW_ALERT,
        UPDATED_ALERT,
        STATISTICS_UPDATE
    }
}
