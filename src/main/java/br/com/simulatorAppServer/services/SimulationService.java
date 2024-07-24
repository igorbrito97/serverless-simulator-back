package br.com.simulatorAppServer.services;

import br.com.simulatorAppServer.models.ServerlessFunctionModel;
import br.com.simulatorAppServer.models.SimulationInputDto;
import br.com.simulatorAppServer.models.SimulationModel;
import br.com.simulatorAppServer.models.SimulationResultsModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SimulationService {
    @Autowired
    ResultAnalysisService resultAnalysisService;

    private ExponentialDistribution distributionRequest, distributionWarm, distributionCold;

    public SimulationResultsModel startSimulation(SimulationInputDto dto) {
        SimulationModel simulation = new SimulationModel(dto);
        initExponentialDistribution(1/dto.getArrivalRate(), 1/dto.getColdResponseTime(), 1/dto.getWarmResponseTime());

        double nextArrival = distributionRequest.sample();
        double time = 0.0;
        while(time < dto.getSimulationTime()) {
            log.info("Current simulation time: {} - Next arrival: {} - Number of running servers: {} - Number of idle servers: {}", time, nextArrival, simulation.getRunningInstances().size(), simulation.getIdleInstances().size());

            simulation.getAllHistoricTimes().add(time);
            simulation.updateHistoricList();
            if(!simulation.hasServers()) {
                double coldExecutionTime = distributionCold.sample();
                time = nextArrival;
                nextArrival = time + distributionRequest.sample();
                simulation.coldStartArrival(time, coldExecutionTime);
            } else {
                ServerlessFunctionModel nextEvent = simulation.getNextEvent();
                if(nextEvent.getNextEventTime() > nextArrival) {
                    //arrival next
                    time = nextArrival;
                    nextArrival = time + distributionRequest.sample();
                    if(simulation.isWarmAvailable()) {
                        double warmExecutionTime = distributionWarm.sample();
                        simulation.warmStartArrival(time, warmExecutionTime);
                    } else {
                        double coldExecutionTime = distributionCold.sample();
                        simulation.coldStartArrival(time, coldExecutionTime);
                    }
                } else {
                    //state transition
                    time = nextEvent.getNextEventTime();
                    simulation.transitionEndingInstance(nextEvent);
                }
            }
        }
        simulation.getAllHistoricTimes().add(time);

        return resultAnalysisService.generateReport(simulation);
    }

    private void initExponentialDistribution(double requestValue, double coldValue, double warmValue) {
        this.distributionRequest = new ExponentialDistribution(requestValue);
        this.distributionCold = new ExponentialDistribution(coldValue);
        this.distributionWarm = new ExponentialDistribution(warmValue);
    }

}
