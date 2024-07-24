package br.com.simulatorAppServer.models;

import br.com.simulatorAppServer.enums.ServerlessExecutionEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static br.com.simulatorAppServer.enums.ServerlessExecutionEnum.*;

@Getter
@Setter
@Slf4j
public class SimulationModel {
    private static final int MAX_RUNNING_INSTANCES = 10000;
    private SimulationInputDto input;

    private List<ServerlessFunctionModel> instances;
    private List<ServerlessFunctionModel> allRunningInstances;
    private List<ServerlessFunctionModel> allIdleInstances;
    private List<ServerlessFunctionModel> finishedInstances;
    private List<Double> allColdArrivalTimes;
    private List<Double> allWarmArrivalTimes;
    private List<ServerlessFunctionModel> allRejectedInstances;
    private Integer requestsCount;
    private List<Double> allHistoricTimes;
    private List<Integer> historicInstanceCount;
    private List<Integer> historicRunningCount;
    private List<Integer> historicIdleCount;

    public SimulationModel(SimulationInputDto input) {
        this.input = input;
        this.instances = new ArrayList<>();
        this.allRunningInstances = new ArrayList<>();
        this.allIdleInstances = new ArrayList<>();
        this.finishedInstances = new ArrayList<>();
        this.allColdArrivalTimes = new ArrayList<>();
        this.allWarmArrivalTimes = new ArrayList<>();
        this.allRejectedInstances = new ArrayList<>();
        this.allHistoricTimes = new ArrayList<>();
        this.historicInstanceCount = new ArrayList<>();
        this.historicRunningCount = new ArrayList<>();
        this.historicIdleCount = new ArrayList<>();
        this.requestsCount = 0;
    }

    public boolean hasServers() {
        return instances.size() > 0;
    }

    public void updateHistoricList() {
        this.historicInstanceCount.add(this.instances.size());
        this.historicRunningCount.add(getRunningInstances().size());
        this.historicIdleCount.add(getIdleInstances().size());
    }

    public void coldStartArrival(double arrivalTime, double executionTime)  {
        if(getRunningInstances().size() == MAX_RUNNING_INSTANCES) {
            log.info("INSTANCE REJECTED - executionTime {}", executionTime);
            ServerlessFunctionModel rejected = new ServerlessFunctionModel(arrivalTime, executionTime, input.getThreshold());
            this.allRejectedInstances.add(rejected);
        } else {
            log.info("COLD START - executionTime {} ", executionTime);
            this.requestsCount++;
            ServerlessFunctionModel function = new ServerlessFunctionModel(arrivalTime, executionTime, input.getThreshold(), COLD);
            this.instances.add(function);
            this.allRunningInstances.add(function);
            this.allColdArrivalTimes.add(arrivalTime);
        }
    }

    public void warmStartArrival(double arrivalTime, double executionTime) {
        log.info("WARM START - executionTime {} ", executionTime);
        this.requestsCount++;
        ServerlessFunctionModel function = this.instances.stream().filter(item -> item.getExecutionType().equals(IDLE)).min(Comparator.comparing(ServerlessFunctionModel::getCreationTime)).orElseThrow(() -> new RuntimeException("ERROR - Idle instance not found"));
        function.startWarmExecution(arrivalTime, executionTime, input.getThreshold());
        this.allRunningInstances.add(function);
        this.allWarmArrivalTimes.add(arrivalTime);
    }

    public ServerlessFunctionModel getNextEvent() {
        AtomicReference<ServerlessFunctionModel> min = new AtomicReference<>(this.instances.get(0));
        this.instances.forEach(function -> {
            if(function.getNextEventTime() < min.get().getNextEventTime())
                min.set(function);
        });
        return min.get();
    }

    public void transitionEndingInstance(ServerlessFunctionModel idx) {
        ServerlessExecutionEnum oldState = idx.transitionToNextState();
        if(oldState == IDLE) {
            log.info("TRANSITIONING {} TO FINISHED FROM IDLE", idx);
            this.finishedInstances.add(idx);
            this.instances.remove(idx);
        }
        else {
            log.info("TRANSITIONING {} TO IDLE FROM RUNNING", idx);
            this.allIdleInstances.add(idx);
        }
    }

    public boolean isWarmAvailable() {
        return this.getIdleInstances().size() > 0;
    }

    public List<ServerlessFunctionModel> getIdleInstances() {
        return this.instances.stream().filter(instance -> instance.getExecutionType().getId().equals(ServerlessExecutionEnum.IDLE.getId())).collect(Collectors.toList());
    }

    public List<ServerlessFunctionModel> getRunningInstances() {
        return this.instances.stream().filter(instance -> !instance.getExecutionType().getId().equals(ServerlessExecutionEnum.IDLE.getId())).collect(Collectors.toList());
    }
}