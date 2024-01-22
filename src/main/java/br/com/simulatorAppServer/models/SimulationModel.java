package br.com.simulatorAppServer.models;

import br.com.simulatorAppServer.enums.ServerlessExecutionEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.simulatorAppServer.enums.ServerlessExecutionEnum.*;

@Getter
@Setter
@Slf4j
public class SimulationModel {
    private static final int MAX_RUNNING_INSTANCES = 10000;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SimulationInputDto input;

    private List<ServerlessFunctionModel> instances;
    private List<ServerlessFunctionModel> allRunningInstances;
    private List<ServerlessFunctionModel> allInstanceTimes;//?
    private List<ServerlessFunctionModel> allIdleInstances;
    private List<ServerlessFunctionModel> finishedInstances;
    private List<Double> allColdArrivalTimes;
    private List<Double> allWarmArrivalTimes;
    private List<ServerlessFunctionModel> allRejectedInstances;
    private Integer allInstancesCount;

    public SimulationModel(SimulationInputDto input) {
        this.input = input;
        this.instances = new ArrayList<>();
        this.allRunningInstances = new ArrayList<>();
        this.allInstanceTimes = new ArrayList<>();
        this.allIdleInstances = new ArrayList<>();
        this.finishedInstances = new ArrayList<>();
        this.allColdArrivalTimes = new ArrayList<>();
        this.allWarmArrivalTimes = new ArrayList<>();
        this.allRejectedInstances = new ArrayList<>();
        this.allInstancesCount = 0;
    }

    public boolean hasServers() {
        return instances.size() > 0;
    }

    public void coldStartArrival(double arrivalTime, double executionTime, long threshold)  {
        if(getRunningInstances().size() == MAX_RUNNING_INSTANCES) {
            log.info("REJECTING INSTANCE........");
            ServerlessFunctionModel rejected = new ServerlessFunctionModel(arrivalTime, executionTime, threshold);
            this.finishedInstances.add(rejected);
            this.allRejectedInstances.add(rejected);
        } else {
            log.info("COLD START BABY - {} ", executionTime);
            this.allInstancesCount++;
            ServerlessFunctionModel function = new ServerlessFunctionModel(arrivalTime, executionTime, threshold, COLD);
            this.instances.add(function);
            this.allRunningInstances.add(function);
            this.allColdArrivalTimes.add(arrivalTime);
        }
    }

    public void warmStartArrival(double arrivalTime, double executionTime, long threshold) {
        log.info("WAAAAAARM START HERE LETGO - {} ", executionTime);
        this.allInstancesCount++;
        ServerlessFunctionModel function = this.instances.stream().filter(item -> item.getExecutionType().equals(IDLE)).findFirst().orElseThrow(() -> new RuntimeException("ERROR - Idle instance not found"));
        function.startWarmExecution(arrivalTime, executionTime, threshold);
        this.instances.add(function);
        this.allRunningInstances.add(function);
        this.allWarmArrivalTimes.add(arrivalTime);
    }

    public int getNextEventIndex() {
        List<Double> nextEventList = this.instances.stream().map(ServerlessFunctionModel::getNextEventTime).collect(Collectors.toList());
        int idx = 0;
        double min = nextEventList.get(idx);
        for(int i = 1; i < nextEventList.size(); i++) {
            if(nextEventList.get(i) < min) {
                idx = i;
            }
        }
        log.info("FOUND MIN NEXT EVENT VALUE: {} - IDX: {} ", nextEventList.get(idx), idx);
        return idx;

        //acho que essa parte aqui de cima esta com problema....... na execução ele mostra que o tempo minimo encontrado é um que ja passou.....
//        this.instances.forEach(function -> {
//
//        });
    }

    public void transitionEndingInstance(int idx) {
        ServerlessExecutionEnum oldState = this.instances.get(idx).transitionToNextState();
        if(oldState == IDLE) {
            log.info("TRANSITIONING {} FROM IDLE TO FINISHED", idx);
            this.finishedInstances.add(this.instances.get(idx));
            this.instances.remove(idx);
        }
        else {
            log.info("TRANSITIONING {} TO IDLE FROM RUNNINGGGGGGGGG", idx);
            this.instances.get(idx).setExecutionType(IDLE);
            this.allIdleInstances.add(this.instances.get(idx));
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

    public long getRejectedInstancesSize() {
        return finishedInstances.stream().filter(ServerlessFunctionModel::getRejected).count();
    }
}