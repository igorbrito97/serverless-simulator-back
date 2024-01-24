package br.com.simulatorAppServer.services;

import br.com.simulatorAppServer.models.ServerlessFunctionModel;
import br.com.simulatorAppServer.models.SimulationInputDto;
import br.com.simulatorAppServer.models.SimulationModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static java.lang.String.format;

@Service
@Slf4j
public class SimulationService {
    private SimulationModel simulation;
    private ExponentialDistribution distributionRequest, distributionWarm, distributionCold;

    public void startSimulation(SimulationInputDto dto) {
        //fazer uma validação com alguns valores --- verificar quais
        //criar instancias da função - verificar se chegou no limite - verificar se é warm ou cold
        simulation = new SimulationModel(dto);
        simulation.setStartTime(LocalDateTime.now());
        initExponentialDistribution(1/dto.getArrivalRate(), 1/dto.getColdResponseTime(), 1/dto.getWarmResponseTime());

        double nextArrival = distributionRequest.sample();
        double time = 0.0;
        while(time < dto.getSimulationTime()) {
            log.info("Current simulation time: {} - Next arrival: {} - Number of running servers: {} - Number of idle servers: {}", time, nextArrival, simulation.getRunningInstances().size(), simulation.getIdleInstances().size());

            if(!simulation.hasServers()) {
                double coldExecutionTime = distributionCold.sample();
                time = nextArrival;
                nextArrival = time + distributionRequest.sample();
                simulation.coldStartArrival(time, coldExecutionTime, simulation.getInput().getThreshold());
            } else {
                ServerlessFunctionModel nextEvent = simulation.getNextEvent();
                log.info("FOUND NEXT EVENT -> {} - status: {}", nextEvent.getNextEventTime(), nextEvent.getExecutionType().getDescription());
                if(nextEvent.getNextEventTime() > nextArrival) {
                    //chegada proximo
                    time = nextArrival;
                    nextArrival = time + distributionRequest.sample();
                    if(simulation.isWarmAvailable()) {
                        double warmExecutionTime = distributionWarm.sample();
                        simulation.warmStartArrival(time, warmExecutionTime, simulation.getInput().getThreshold());
                    } else {
                        double coldExecutionTime = distributionCold.sample();
                        simulation.coldStartArrival(time, coldExecutionTime, simulation.getInput().getThreshold());
                    }
                } else {
                    //mudança de estado de alguma atual
                    simulation.transitionEndingInstance(nextEvent);
                    time = nextEvent.getNextEventTime();
                }
            }
        }

        simulation.setEndTime(LocalDateTime.now());
        generateReport();
    }

    private void initExponentialDistribution(double requestValue, double coldValue, double warmValue) {
        this.distributionRequest = new ExponentialDistribution(requestValue);
        this.distributionCold = new ExponentialDistribution(coldValue);
        this.distributionWarm = new ExponentialDistribution(warmValue);
    }

    private void generateReport() {
        log.info(format("TOTAL FINISHED INSTANCES: %s. TOTAL REJECTED INSTANCES: %s. ", simulation.getFinishedInstances().size(), simulation.getRejectedInstancesSize()));
        log.info(format("TOTAL RUNNING INSTANCES: %s. TOTAL IDLE INSTANCES: %s. ", simulation.getRunningInstances().size(), simulation.getIdleInstances().size()));
        log.info("-------");
        log.info("QNTD COLDS: {}", simulation.getAllColdArrivalTimes().size());
        log.info("QNTD RUNNING: {}", simulation.getAllRunningInstances().size());
        log.info("REQS TOTAL: {}", simulation.getAllInstancesCount());//aqui acho que não esta ok - fazer variavel para isso????
        log.info("PROB COLD: {}", simulation.getAllColdArrivalTimes().size()*100/(simulation.getAllColdArrivalTimes().size() + simulation.getAllWarmArrivalTimes().size()));
        log.info("PROB COLD (getAll): {}", simulation.getAllColdArrivalTimes().size()*100/simulation.getAllInstancesCount());
        log.info("PROB REJECTED: {}", simulation.getAllRejectedInstances().size()*100/simulation.getAllInstancesCount());
        log.info("TOTAL REJECTED: {}", simulation.getAllRejectedInstances().size());
        //média de vida das instancias
        //média de instancias (total) / em execução / em idle
    }

    private void validateInputs() {
        //o cold tem que ser maior que warm
        //simulationTime com valor minimo
    }

}
