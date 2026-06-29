package br.com.avia.avia_api.exception.anexo;

import br.com.avia.avia_api.exception.ErroResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AnexoExceptionHandler {

    @ExceptionHandler(AnexoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> handleAnexoNaoEncontrado(AnexoNaoEncontradoException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErroResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(AnexoNaoPertenceATarefaException.class)
    public ResponseEntity<ErroResponseDTO> handleAnexoNaoPertenceATarefa(AnexoNaoPertenceATarefaException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErroResponseDTO(ex.getMessage()));
    }
}