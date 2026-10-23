package br.com.avia.avia_api.exception.sprint;

import br.com.avia.avia_api.exception.ErroResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SprintExceptionHandler {

    @ExceptionHandler(SprintNaoEncontradaException.class)
    public ResponseEntity<ErroResponseDTO> handleSprintNaoEncontrada(SprintNaoEncontradaException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErroResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(SprintJaExisteException.class)
    public ResponseEntity<ErroResponseDTO> handleSprintExiste(SprintJaExisteException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErroResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(SprintNaoPertenceAoProjetoException.class)
    public ResponseEntity<ErroResponseDTO> handleSprintNaoPertenceAoProjeto(SprintNaoPertenceAoProjetoException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErroResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(IntervaloSprintsInvalidoException.class)
    public ResponseEntity<ErroResponseDTO> handleIntervaloSprintsInvalido(IntervaloSprintsInvalidoException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErroResponseDTO(ex.getMessage()));
    }
}