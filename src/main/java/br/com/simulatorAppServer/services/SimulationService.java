package br.com.simulatorAppServer.services;

import br.com.simulatorAppServer.enums.SimulationEventTypeEnum;
import br.com.simulatorAppServer.models.SimulationInputDto;
import br.com.simulatorAppServer.models.SimulationModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.AbstractMap;

import static br.com.simulatorAppServer.enums.SimulationEventTypeEnum.*;
import static br.com.simulatorAppServer.enums.ServerlessExecutionEnum.*;
import static java.lang.String.format;

@Service
@Slf4j
public class SimulationService {
    private static final int MAX_RUNNING_INSTANCES = 10000;
    private static final int LISTS_INSTANCE_SIZE = 1000000;

    private SimulationModel simulation;
    private Integer requestIdx, warmIdx, coldIdx;
    private Double time, nextEventTime;

    public void startSimulation(SimulationInputDto dto) {
        //fazer uma validação com alguns valores --- verificar quais
        //criar instancias da função - verificar se chegou no limite - verificar se é warm ou cold
        simulation = new SimulationModel(dto);
        time = nextEventTime = 0.0;
        requestIdx = warmIdx = coldIdx = 0;
        AbstractMap.SimpleEntry<SimulationEventTypeEnum, Double> nextEvent;

        double[] list = getExponentialDistributionList(1/dto.getArrivalRate(), LISTS_INSTANCE_SIZE);//lista com os tempos de chegada
        double[] warmList = getExponentialDistributionList(dto.getWarmResponseTime(), LISTS_INSTANCE_SIZE);//lista de tempos médios - tempo que demora para executar
        double[] coldList = getExponentialDistributionList(dto.getColdResponseTime(), LISTS_INSTANCE_SIZE);//lista de tempos médios - tempo que demora para executar

        //pensar em transformar esse array em um List<> porque ai consegue remover sempre a pos 0 e não precisa de idx
        simulation.setStartTime(LocalDateTime.now());
        while(time < dto.getSimulationTime()) {
            log.info(format("Current simulation time: %s - Number of running servers: %s", time, simulation.getRunningInstances().size()));
            nextEvent = getNextEvent(list);
            simulation.updateInstancesCurrentTime(nextEvent.getValue());// o update remove as que acabaram tbm - ou fazer função só para isso ????
            updateNexRequestTime(list, nextEvent.getValue());//update tbm as horas da lista de chegada????

            if(nextEvent.getKey().equals(ARRIVAL)) {
                //chegada -> ver se tem maquina idle para rodar (warm), senão cria nova
                log.info("New function arrival!!!");
                checkForWarmOrColdAndAddInstance(coldList, warmList);
                requestIdx++;
            }
            else {
                //ver se tem diferença entre arrival, execução finalizada ou idle que parou

                /*
                no updateTIme: diminuir de todas running (se 0 vai para idle), de todas idle (se 0 - thrshold ai vai para finished)

                 */
            }

            time = time + nextEvent.getValue();
            log.info(format("Time added: %s - Event: %s", nextEvent.getValue(), nextEvent.getKey().getName()));
        }

        generateReport();


        //reunião 30/06 simulação de eventos discretos ->
        //distribuição exponencial -> chegada de clientes do sistema
        //atualizo o relogio da simulação quando retira coisa da lista de eventos futuros

    }

    private AbstractMap.SimpleEntry<SimulationEventTypeEnum, Double> getNextEvent(double[] list) {
        if(requestIdx == LISTS_INSTANCE_SIZE) {
            requestIdx = 0;//nova lista??
            log.info("CHEGOU NO FINAL DA LISTA PRINCIPAL DE CHEGADA. COMEÇANDO UMA NOVA. SERÀ QUE È ISSO MESMO?!?!?");
        }
        //ver se o mais proximo é uma chegada ou a finalização de alguma rodando ou finalização de idle(cold)
        //running instances sempre ordenado

        //????? aqui ver o max
        SimulationEventTypeEnum maxType = ARRIVAL;
        Double maxValue = list[requestIdx];
//        if()



        if(simulation.getRunningInstances().isEmpty()) {
            return new AbstractMap.SimpleEntry<>(ARRIVAL, list[requestIdx]);
        } else {
            if(simulation.getRunningInstances().get(0).getCurrentTime() < list[requestIdx]) {
                //finalizar primeiro que chegar
                return new AbstractMap.SimpleEntry<>(FUNCTION, simulation.getRunningInstances().get(0).getCurrentTime());
            }
            else if(list[requestIdx] < simulation.getRunningInstances().get(0).getCurrentTime()) {
                //chegada de uma nova
                return new AbstractMap.SimpleEntry<>(ARRIVAL, list[requestIdx]);
            }
            else {
                // é igual -> o que fazer ????
                log.info("ENTROU NO VALOR NULL MEU TRYUTSSSS");
                return null;
            }
        }
    }

    private void checkForWarmOrColdAndAddInstance(double[] coldList, double[] warmList) {
        if(simulation.getIdleInstances().size() > 0) { //warm
            if(warmIdx == LISTS_INSTANCE_SIZE) {
                warmIdx = 0;//nova lista???
                log.info("CHEGOU NO FINAL DA LISTA QUENTE. COMEÇANDO UMA NOVA. SERÀ QUE È ISSO MESMO?!?!?");
            }
            log.info("It's warm!! Idx: " + warmIdx);
            simulation.removeIdle();
            simulation.addRunningInstance(warmList[warmIdx], WARM);
            warmIdx++;
        }
        else { //cold
            if(simulation.getRunningInstances().size() + simulation.getIdleInstances().size() == MAX_RUNNING_INSTANCES) {// atingiu limite de instancias rodando
                simulation.addRejectedInstance(time);
            }
            else {
                if(coldIdx == LISTS_INSTANCE_SIZE) {
                    coldIdx = 0; //volto no comeco do array e gero uma lista nova??
                    log.info("CHEGOU NO FINAL DA LISTA FRIA. COMEÇANDO UMA NOVA. SERÀ QUE È ISSO MESMO?!?!?");
                }
                log.info("It's cold!! Idx: " + coldIdx);
                simulation.addRunningInstance(coldList[coldIdx], COLD);
                coldIdx++;
            }
        }
    }

    private void updateNexRequestTime(double[] list, double time) {


        if(list[requestIdx] - time == 0.0) {
            requestIdx++;
        }
    }

    private void generateReport() {
        log.info(format("TOTAL FINISHED INSTANCES: %s. TOTAL REJECTED INSTANCES: %s. ", simulation.getFinishedInstances().size(), simulation.getRejectedInstancesSize()));
        log.info(format("TOTAL RUNNING INSTANCES: %s. TOTAL IDLE INSTANCES: %s. ", simulation.getRunningInstances().size(), simulation.getIdleInstances().size()));
    }

    private double[] getExponentialDistributionList(double expValue, int size) {
        ExponentialDistribution exp = new ExponentialDistribution(expValue);
        return exp.sample(size);
    }

    private void validateInputs() {
        //o cold tem que ser maior que warm
        //simulationTime com valor minimo
    }

}
