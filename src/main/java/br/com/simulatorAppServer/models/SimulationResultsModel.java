package br.com.simulatorAppServer.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class SimulationResultsModel {
    private int coldInstancesCount;
    private int warmInstancesCount;
    private int requestsCount;
    private int rejectedCount;
    private double coldProbability;
    private double rejectionProbability;
    private double warmProbability;
    private double averageInstanceLifespan;
    private double averageServerCount;
    private double averageRunningServers;
    private double averageIdleCount;
}
