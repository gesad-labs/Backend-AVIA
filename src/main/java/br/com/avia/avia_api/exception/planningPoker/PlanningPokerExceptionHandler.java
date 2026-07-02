package br.com.avia.avia_api.exception.planningPoker;

import br.com.avia.avia_api.exception.ErroResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PlanningPokerExceptionHandler {

    @ExceptionHandler(PlanningPokerException.class)
    public ResponseEntity<ErroResponseDTO> handlePlanningPokerException(PlanningPokerException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErroResponseDTO(ex.getMessage()));
    }
}