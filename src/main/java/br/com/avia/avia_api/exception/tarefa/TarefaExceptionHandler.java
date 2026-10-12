package br.com.avia.avia_api.exception.tarefa;

import br.com.avia.avia_api.exception.ErroResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TarefaExceptionHandler {

    @ExceptionHandler(TarefaNaoEncontradaException.class)
    public ResponseEntity<ErroResponseDTO> handleTarefaNaoEncontrada(TarefaNaoEncontradaException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErroResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(TarefaNaoPertenceAoProjetoException.class)
    public ResponseEntity<ErroResponseDTO> handleTarefaNaoPertenceAoProjeto(TarefaNaoPertenceAoProjetoException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErroResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(HierarquiaTarefaInvalidaException.class)
    public ResponseEntity<ErroResponseDTO> handleHierarquiaTarefaInvalida(HierarquiaTarefaInvalidaException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErroResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(TarefaPaiConcluidaException.class)
    public ResponseEntity<ErroResponseDTO> handleTarefaPaiConcluida(TarefaPaiConcluidaException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErroResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(SubtarefasPendentesException.class)
    public ResponseEntity<ErroResponseDTO> handleSubtarefasPendentes(SubtarefasPendentesException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErroResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(TarefaPossuiSubtarefasException.class)
    public ResponseEntity<ErroResponseDTO> handleTarefaPossuiSubtarefas(TarefaPossuiSubtarefasException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErroResponseDTO(ex.getMessage()));
    }
}