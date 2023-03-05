package br.com.simulatorAppServer.enums;

import lombok.Getter;

@Getter
public enum SimulationEventTypeEnum {
    ARRIVAL(1L, "Arrival", "Request arrival"),
    FUNCTION(2L, "Function", "End of function execution"),
    Idle(2L, "Idle", "End of idle threshold");

    final Long id;
    final String name;
    final String description;

    SimulationEventTypeEnum(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
