package br.com.simulatorAppServer.models;

import br.com.simulatorAppServer.enums.ServerlessExecutionEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ServerlessFunctionModel {
    private Boolean rejected;
    private ServerlessExecutionEnum executionType;
    private double startTime;
    private double executionTime;
    private double endOfExecutionTime;
    private double terminationTime;

    public ServerlessFunctionModel(double timeValue, double executionTime, Long threshold) {
        //rejected
        this.executionTime = executionTime;
        this.startTime = timeValue;
        this.endOfExecutionTime = timeValue + executionTime;
        this.terminationTime = timeValue + executionTime + threshold;
        this.rejected = true;
    }

    public ServerlessFunctionModel(double timeValue, double executionTime, Long threshold, ServerlessExecutionEnum executionType) {
        //normal
        this.executionTime = executionTime;
        this.startTime = timeValue;
        this.endOfExecutionTime = timeValue + executionTime;
        this.terminationTime = timeValue + executionTime + threshold;
        this.rejected = false;
        this.executionType = executionType;
    }

    public double getNextEventTime() {
        if(this.executionType.equals(ServerlessExecutionEnum.IDLE))
            return this.terminationTime;
        else
            return this.endOfExecutionTime;
    }

    public void startWarmExecution(double timeValue, double executionTime, Long threshold) {
        this.executionTime = executionTime;
        this.startTime = timeValue;
        this.endOfExecutionTime = timeValue + executionTime;
        this.terminationTime = timeValue + executionTime + threshold;
        this.rejected = false;
        this.executionType = ServerlessExecutionEnum.WARM;
    }

    public ServerlessExecutionEnum transitionToNextState() {
        if(this.executionType.equals(ServerlessExecutionEnum.WARM) || this.executionType.equals(ServerlessExecutionEnum.COLD)) {
            ServerlessExecutionEnum old = this.executionType;
            this.executionType = ServerlessExecutionEnum.IDLE;
            return old;
        }
        else if(this.executionType.equals(ServerlessExecutionEnum.IDLE)) {
            return ServerlessExecutionEnum.IDLE;
        }
        else
            throw new RuntimeException("Erro ao fazer transição - tipo de execução inválido");
    }
}
