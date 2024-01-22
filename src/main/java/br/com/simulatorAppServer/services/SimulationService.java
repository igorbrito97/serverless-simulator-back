package br.com.simulatorAppServer.services;

import br.com.simulatorAppServer.enums.SimulationEventTypeEnum;
import br.com.simulatorAppServer.models.SimulationInputDto;
import br.com.simulatorAppServer.models.SimulationModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.AbstractMap;

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
        AbstractMap.SimpleEntry<SimulationEventTypeEnum, Double> nextEvent;
        initExponentialDistribution(1/dto.getArrivalRate(), 1/dto.getColdResponseTime(), 1/dto.getWarmResponseTime());

        double nextArrival = distributionRequest.sample();
        double time = 0.0;
        while(time < dto.getSimulationTime()) {
            log.info("Current simulation time: {} - Number of running servers: {} - Number of idle servers: {}", time, simulation.getRunningInstances().size(), simulation.getIdleInstances().size());

            if(!simulation.hasServers()) {
                double coldExecutionTime = distributionCold.sample();
                time = nextArrival;//att o tempo
                nextArrival = time + distributionRequest.sample();//aqui é t + sample() ?
                simulation.coldStartArrival(time, coldExecutionTime, simulation.getInput().getThreshold());
            } else {
                int nextEventIdx = simulation.getNextEventIndex();
                if(simulation.getInstances().get(nextEventIdx).getNextEventTime() > (nextArrival - time)) {
                    //chegada proximo
                    time = nextArrival;//att o tempo
                    nextArrival = time + distributionRequest.sample();//aqui é t + sample() ?
                    if(simulation.isWarmAvailable()) {
                        double warmExecutionTime = distributionWarm.sample();
                        simulation.warmStartArrival(time, warmExecutionTime, simulation.getInput().getThreshold());
                    } else {
                        double coldExecutionTime = distributionCold.sample();
                        simulation.coldStartArrival(time, coldExecutionTime, simulation.getInput().getThreshold());
                    }
                } else {
                    //mudança de estado de alguma atual
                    time = simulation.getInstances().get(nextEventIdx).getNextEventTime();
                    simulation.transitionEndingInstance(nextEventIdx);
                }
            }
        }

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
    }

    private void validateInputs() {
        //o cold tem que ser maior que warm
        //simulationTime com valor minimo
    }

}
