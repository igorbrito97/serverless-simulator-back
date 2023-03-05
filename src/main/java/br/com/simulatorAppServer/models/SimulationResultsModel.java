package br.com.simulatorAppServer.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimulationResultsModel {
    private Long coldProbability;
    private Long rejectionProbability;
    private Long averageInstanceLifespan;
    private Long averageServerCount;
    private Long averageRunningServers;
    private Long averageIdleCount;
    private Long averageResponseTime;
}
