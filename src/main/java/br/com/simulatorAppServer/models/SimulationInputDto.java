package br.com.simulatorAppServer.models;

import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Getter
@ToString
public class SimulationInputDto {
    @NotNull(message = "Taxa de chegada deve ser preenchida.")
    private Double arrivalRate;// quantidade de requisições que chegam por segundo

    @NotNull(message = "Taxa de chegada deve ser preenchida.")
    private Double warmResponseTime;//tempo médio de resposta de uma req warm

    @NotNull(message = "Taxa de chegada deve ser preenchida.")
    private Double coldResponseTime;//tempo médio de resposta de uma req cold

    @NotNull(message = "Taxa de chegada deve ser preenchida.")
    private Long threshold; //tempo até função sair de idle para finalizada

    @NotNull(message = "Taxa de chegada deve ser preenchida.")
    private Long simulationTime;//tempo "maximo" de simulação
}
