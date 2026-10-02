package br.com.avia.avia_api.exception.auth;

import br.com.avia.avia_api.exception.ErroResponseDTO;
import br.com.avia.avia_api.exception.ErroValidacaoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class AuthExceptionHandler {
    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErroResponseDTO(ex.getMessage()));

    }
    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<ErroResponseDTO> handleEmailDuplicado(EmailDuplicadoException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErroResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> handleSenhaInvalida(MethodArgumentNotValidException ex){
        Map<String, List<String>> erros = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                erros.computeIfAbsent(erro.getField(), k -> new ArrayList<>())
                        .add(erro.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ErroResponseDTO("Dados inválidos", erros));
    }

}
