package br.com.simulatorAppServer.enums;

import lombok.Getter;

@Getter
public enum ServerlessExecutionEnum {
    COLD(1L, "Cold"),
    WARM(2L, "Warm"),
    IDLE(3L, "Idle");

    final Long id;
    final String description;

    ServerlessExecutionEnum(Long id, String description) {
        this.id = id;
        this.description = description;
    }
}
