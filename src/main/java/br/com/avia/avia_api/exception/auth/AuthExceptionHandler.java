package br.com.avia.avia_api.exception.auth;

import br.com.avia.avia_api.entity.Usuario;
import br.com.avia.avia_api.exception.ErroResponseDTO;
import br.com.avia.avia_api.exception.projeto.UsuarioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionHandler {
    // troquei o metodo para receber o exception lançado pelo authenticationManager
    // que ja faz a verificação tanto de email como de senha, de forma mais segura
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponseDTO> handleUsuarioNaoEncontrado(AuthenticationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErroResponseDTO("E-mail ou senha inválidos."));

    }
    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<ErroResponseDTO> handleEmailDuplicado(EmailDuplicadoException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErroResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(TokenRecuperacaoInvalidoException.class)
    public ResponseEntity<ErroResponseDTO> handleTokenRecuperacaoInvalido(TokenRecuperacaoInvalidoException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErroResponseDTO(ex.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> handleSenhaInvalida(MethodArgumentNotValidException ex){
        Object alvo = ex.getBindingResult().getTarget();
        List<String> ordemDosCampos = alvo == null ? List.of() : camposNaOrdem(alvo.getClass());

        Map<String, List<String>> erros = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparingInt((FieldError erro) -> posicao(ordemDosCampos, erro.getField()))
                        .thenComparing(FieldError::getField)
                        .thenComparing(erro -> String.valueOf(erro.getDefaultMessage())))
                .forEach(erro -> erros.computeIfAbsent(erro.getField(), k -> new ArrayList<>())
                        .add(erro.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ErroResponseDTO("Dados inválidos", erros));
    }

    private static List<String> camposNaOrdem(Class<?> tipo) {
        if (tipo.isRecord()) {
            return Arrays.stream(tipo.getRecordComponents()).map(RecordComponent::getName).toList();
        }
        return Arrays.stream(tipo.getDeclaredFields()).map(Field::getName).toList();
    }

    private static int posicao(List<String> ordemDosCampos, String campo) {
        int posicao = ordemDosCampos.indexOf(campo.split("[.\\[]")[0]);
        return posicao >= 0 ? posicao : Integer.MAX_VALUE;
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException ex){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErroResponseDTO(ex.getMessage()));
    }

}
