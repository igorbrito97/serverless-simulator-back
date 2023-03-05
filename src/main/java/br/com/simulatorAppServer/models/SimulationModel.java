package br.com.simulatorAppServer.models;

import br.com.simulatorAppServer.enums.ServerlessExecutionEnum;
import br.com.simulatorAppServer.utils.ServerlessFunctionSortingComparator;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;

@Getter
@Setter
@Slf4j
public class SimulationModel {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SimulationInputDto input;
    private List<ServerlessFunctionModel> runningInstances;
    private List<ServerlessFunctionModel> idleInstances;
    private List<ServerlessFunctionModel> finishedInstances;

    public SimulationModel(SimulationInputDto input) {
        this.input = input;
        this.runningInstances = new ArrayList<>();
        this.idleInstances = new ArrayList<>();
        this.finishedInstances = new ArrayList<>();
    }

    public void addRunningInstance(Double time, ServerlessExecutionEnum executionType) {
        ServerlessFunctionModel function = new ServerlessFunctionModel(time, executionType);
        int idx = Collections.binarySearch(this.runningInstances, null, new ServerlessFunctionSortingComparator());
        this.runningInstances.add(Math.max(idx, 0), function);

        log.info(format("Add new instance! Execution time: %s - type: %s. Total number of runningInstances: %s", time, executionType.getDescription(), runningInstances.size()));
    }

    public void addRejectedInstance(Double time) {
        ServerlessFunctionModel function = new ServerlessFunctionModel(time, true);
        this.finishedInstances.add(function);
    }

    public void addIdleInstance() {
        ServerlessFunctionModel function = new ServerlessFunctionModel(input.getThreshold());
        this.idleInstances.add(this.idleInstances.size(), function);
        log.info(format("Add idle instance! Threshold: %s. Total number of idleInstances: %s", input.getThreshold(), idleInstances.size()));
    }

    public void removeIdle() {
        ServerlessFunctionModel function = this.idleInstances.remove(0);
        log.info(format("Removing IDLE function: TYPE: %s. EXEC TIME: %s. CURRENT TIME: %s", function.getExecutionType().getDescription(), function.getExecutionTime(), function.getCurrentTime()));
    }

    public void updateInstancesCurrentTime(Double time) {
        List<ServerlessFunctionModel> runningToRemove = new ArrayList<>();
        for (ServerlessFunctionModel function : runningInstances) {
            double newCurrentTime = function.getCurrentTime() - time;
            function.setCurrentTime(newCurrentTime);
            if (newCurrentTime <= 0.0) {
                addIdleInstance();
                finishedInstances.add(function);
                runningToRemove.add(function);
            }
        }
        if(!runningToRemove.isEmpty()) {
            log.info(format("Removing running instance! Total number of instances to remove: %s", runningToRemove.size()));
            runningInstances.removeAll(runningToRemove);
        }

        List<ServerlessFunctionModel> idleToRemove = new ArrayList<>();
        for (ServerlessFunctionModel function : idleInstances) {
            double newCurrentTime = function.getCurrentTime() - time;
            function.setCurrentTime(newCurrentTime);
            if (newCurrentTime <= 0.0) {
                finishedInstances.add(function);
                idleToRemove.add(function);
            }
        }

        if(!idleToRemove.isEmpty()){
            log.info(format("Removing idle instance! Total number of instances to remove: %s", idleToRemove.size()));
            idleInstances.removeAll(idleToRemove);
        }
    }

    public long getRejectedInstancesSize() {
        return finishedInstances.stream().filter(ServerlessFunctionModel::getRejected).count();
    }
}
/*
    P

*/