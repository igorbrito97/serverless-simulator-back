package br.com.simulatorAppServer.controllers;

import br.com.simulatorAppServer.models.SimulationInputDto;
import br.com.simulatorAppServer.models.SimulationResultsModel;
import br.com.simulatorAppServer.services.SimulationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
@RequestMapping(value = "/api/simulation")
@Slf4j
public class SimulationController {

    @Autowired
    private SimulationService simulationService;

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping()
    public ResponseEntity<SimulationResultsModel> simulate(@RequestBody @Valid SimulationInputDto simulationInput) {
        log.info("Solicitação para simulação!");
        SimulationResultsModel result = simulationService.startSimulation(simulationInput);

        return ResponseEntity.ok().body(result);
    }
}
