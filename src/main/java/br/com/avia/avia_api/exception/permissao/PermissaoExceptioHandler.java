package br.com.avia.avia_api.exception.permissao;

import br.com.avia.avia_api.exception.ErroResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PermissaoExceptioHandler {
    @ExceptionHandler(PermissaoNegadaException.class)
    public ResponseEntity<ErroResponseDTO> handlePermissaoNegada(PermissaoNegadaException ex){
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErroResponseDTO(ex.getMessage()));
    }
}
