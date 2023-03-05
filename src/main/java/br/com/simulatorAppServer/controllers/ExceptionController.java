package br.com.simulatorAppServer.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

//    @ExceptionHandler()
//    public ResponseEntity handleValidationError() {
//        //aqui ver se vai ter validação mesmo - talvez não seja interessante
//        //para iniciar a simulação tem que fazer umas validações de valor - da pegar aqui o erro lançado e ai passar mensagem ???? mas ai precisa ter um atributo para saber a linguagem
//        log.info("PROBLEMA AQUI NA EXCEPTION CONTROLLER");
//        return ResponseEntity.ok().build();
//    }
}
