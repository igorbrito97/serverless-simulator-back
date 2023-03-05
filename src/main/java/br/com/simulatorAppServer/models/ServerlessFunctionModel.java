package br.com.simulatorAppServer.models;

import br.com.simulatorAppServer.enums.ServerlessExecutionEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ServerlessFunctionModel {
    private Boolean rejected;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ServerlessExecutionEnum executionType;
    private Double executionTime;
    private Double currentTime;

    public ServerlessFunctionModel(Double timeValue, Boolean rejected) {
        //rejected
        this.executionTime = timeValue;
        this.currentTime = 0.0; // ou timeValue ???? não saber ainda
        this.startTime = this.endTime = LocalDateTime.now();
        this.rejected = rejected;
    }

    public ServerlessFunctionModel(Double timeValue, ServerlessExecutionEnum executionType) {
        //normal
        this.executionTime = this.currentTime = timeValue;
        this.startTime = LocalDateTime.now();
        this.rejected = false;
        this.executionType = executionType;
    }

    public ServerlessFunctionModel(Long threshold) {
        //idle
        this.executionTime = this.currentTime = Double.valueOf(threshold);
        this.startTime = LocalDateTime.now();
        this.rejected = false;
        this.executionType = ServerlessExecutionEnum.IDLE;
    }
}
