package br.com.simulatorAppServer.enums;

import lombok.Getter;

@Getter
public enum ServerlessExecutionEnum {
    COLD(1L, "Warm"),
    WARM(2L, "Cold"),
    IDLE(3L, "Idle time");

    Long id;
    String description;

    ServerlessExecutionEnum(Long id, String description) {
        this.id = id;
        this.description = description;
    }
}
