package br.com.simulatorAppServer.services;

import br.com.simulatorAppServer.models.ServerlessFunctionModel;
import br.com.simulatorAppServer.models.SimulationModel;
import br.com.simulatorAppServer.models.SimulationResultsModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.Math.min;

@Service
@Slf4j
public class ResultAnalysisService {

    public SimulationResultsModel generateReport(SimulationModel simulation) {
        log.info("RESULTS\n-------");
        List<Double> diffList = getDiffList(simulation.getAllHistoricTimes());
        double lastEventTime = simulation.getAllHistoricTimes().get(simulation.getAllHistoricTimes().size() - 1);

        SimulationResultsModel result = SimulationResultsModel.builder()
                .averageIdleCount(getAverageIdleCount(simulation.getHistoricIdleCount(), diffList, lastEventTime))
                .averageInstanceLifespan(getAverageInstanceLifespan(simulation.getFinishedInstances()))
                .averageRunningServers(getAverageRunningServers(simulation.getHistoricRunningCount(), diffList, lastEventTime))
                .averageServerCount(getAverageServerCount(simulation.getHistoricInstanceCount(), diffList, lastEventTime))
                .coldInstancesCount(simulation.getAllColdArrivalTimes().size())
                .coldProbability(getColdProbability(simulation.getAllColdArrivalTimes(), simulation.getRequestsCount()))
                .rejectedCount(simulation.getAllRejectedInstances().size())
                .warmProbability(getWarmProbability(simulation.getAllWarmArrivalTimes(), simulation.getRequestsCount()))
                .rejectionProbability(getRejectionProbability(simulation.getAllRejectedInstances(), simulation.getRequestsCount()))
                .requestsCount(simulation.getRequestsCount())
                .warmInstancesCount(simulation.getAllWarmArrivalTimes().size())
                .build();

        log.info("REQS TOTAL: {}", result.getRequestsCount());
        log.info("QNT COLDS: {}", result.getColdInstancesCount());
        log.info("QNT WARM: {}", result.getWarmInstancesCount());
        log.info("PROB COLD: {}", result.getColdProbability());
        log.info("WARM PROB: {}", result.getWarmProbability());
        log.info("PROB REJECTED: {}", result.getRejectionProbability());
        log.info("QNT REJECTED: {}", result.getRejectedCount());
        log.info("AVG IDLE COUNT: {}", result.getAverageIdleCount());
        log.info("AVG INSTANCE LIFESPAN: {}", result.getAverageInstanceLifespan());
        log.info("AVG RUNNING SERVERS: {}", result.getAverageRunningServers());
        log.info("AVG SERVER COUNT: {}", result.getAverageServerCount());
        log.info("QNT RUNNING: {}", simulation.getAllRunningInstances().size());

        return result;
    }

    private List<Double> getDiffList(List<Double> allTimesList) {
        List<Double> diffList = new ArrayList<>();
        for (int i = 1; i < allTimesList.size(); i++) {
            diffList.add(allTimesList.get(i) - allTimesList.get(i - 1));
        }
        return diffList;
    }

    private double getAverageIdleCount(List<Integer> historicIdle, List<Double> getDiffList, double lastInstanceTime) {
        double arraySum = IntStream.range(0, min(historicIdle.size(), getDiffList.size()))
                .mapToDouble(i -> historicIdle.get(i) * getDiffList.get(i))
                .sum();
        return arraySum / lastInstanceTime;
    }

    private double getAverageRunningServers(List<Integer> historicRunning, List<Double> getDiffList, double lastInstanceTime) {
        double arraySum = IntStream.range(0, min(historicRunning.size(), getDiffList.size()))
                .mapToDouble(i -> historicRunning.get(i) * getDiffList.get(i))
                .sum();
        return arraySum / lastInstanceTime;
    }

    private double getAverageServerCount(List<Integer> historicInstances, List<Double> getDiffList, double lastInstanceTime) {
        double arraySum = IntStream.range(0, min(historicInstances.size(), getDiffList.size()))
                .mapToDouble(i -> historicInstances.get(i) * getDiffList.get(i))
                .sum();
        return arraySum / lastInstanceTime;
    }

    private double getAverageInstanceLifespan(List<ServerlessFunctionModel> finishedInstances) {
        return finishedInstances.stream().mapToDouble(ServerlessFunctionModel::getLifeSpan).sum() / finishedInstances.size();
    }

    private double getColdProbability(List<Double> allColdArrivalTimes, int requestsCount) {
        return allColdArrivalTimes.size() * 100D / requestsCount;
    }

    private double getRejectionProbability(List<ServerlessFunctionModel> allRejectedInstances, int requestsCount) {
        return allRejectedInstances.size() * 100D / requestsCount;
    }

    private double getWarmProbability(List<Double> allWarmArrivalTimes, int requestsCount) {
        return allWarmArrivalTimes.size() * 100D / requestsCount;
    }
}
