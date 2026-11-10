package br.com.avia.avia_api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.StreamReadException;

import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    static final String MENSAGEM_ERRO_INESPERADO = "Ocorreu um erro inesperado.";

    @ExceptionHandler(AviaException.class)
    public ResponseEntity<ErroResponseDTO> handleErroDeNegocio(AviaException ex) {
        return erro(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> handleCorpoInvalido(HttpMessageNotReadableException ex) {
        String campo = campoComErro(ex);
        String mensagem = campo == null
                ? "Corpo da requisição ausente ou mal formatado."
                : "Valor inválido para o campo '" + campo + "'.";
        return erro(HttpStatus.BAD_REQUEST, mensagem);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> handleParametroComTipoErrado(MethodArgumentTypeMismatchException ex) {
        return erro(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro '" + ex.getName() + "'.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponseDTO> handleParametroAusente(MissingServletRequestParameterException ex) {
        return erro(HttpStatus.BAD_REQUEST, "O parâmetro '" + ex.getParameterName() + "' é obrigatório.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponseDTO> handleRotaInexistente(NoResourceFoundException ex) {
        return erro(HttpStatus.NOT_FOUND, "Recurso não encontrado.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> handleMetodoNaoPermitido(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(ex.getHeaders())
                .body(new ErroResponseDTO("Método não permitido para este recurso."));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> handleTipoDeConteudoNaoSuportado(HttpMediaTypeNotSupportedException ex) {
        return erro(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de conteúdo não suportado. Envie os dados em JSON.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponseDTO> handleIntegridadeDosDados(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade dos dados", ex);
        return erro(HttpStatus.CONFLICT,
                "Não foi possível concluir a operação porque ela conflita com os dados existentes.");
    }

    // Evita que as exceções do Spring Security lançadas dentro do controller
    // caiam no handler e virem 500
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponseDTO> handleFalhaDeAutenticacao(AuthenticationException ex) {
        return erro(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponseDTO> handleAcessoNegado(AccessDeniedException ex) {
        return erro(HttpStatus.FORBIDDEN, "Você não tem permissão para executar essa ação.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> handleErroInesperado(Exception ex) {
        if (ex instanceof ErrorResponse erroDoFramework && !erroDoFramework.getStatusCode().is5xxServerError()) {
            return erro(erroDoFramework.getStatusCode(), mensagemPadrao(erroDoFramework.getStatusCode()));
        }

        log.error("Erro inesperado", ex);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, MENSAGEM_ERRO_INESPERADO);
    }

    private static ResponseEntity<ErroResponseDTO> erro(HttpStatusCode status, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResponseDTO(mensagem));
    }

    private static String mensagemPadrao(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "Requisição inválida.";
            case 401 -> "Sessão expirada ou inválida. Faça login novamente.";
            case 403 -> "Você não tem permissão para executar essa ação.";
            case 404 -> "Recurso não encontrado.";
            case 405 -> "Método não permitido para este recurso.";
            case 409 -> "A operação entra em conflito com o estado atual dos dados.";
            case 413 -> "O arquivo excede o tamanho máximo de 25 MB.";
            default -> "Não foi possível processar a requisição.";
        };
    }

    // Monta o caminho do campo a partir da exceção do Jackson
    private static String campoComErro(HttpMessageNotReadableException ex) {
        Throwable causa = ex.getCause();
        while (causa != null && !(causa instanceof JacksonException)) {
            causa = causa.getCause();
        }
        // Erro de sintaxe também pode vir com o caminho do campo, mas o problema é o JSON inteiro
        if (!(causa instanceof JacksonException erroJackson)
                || erroJackson instanceof StreamReadException
                || erroJackson.getPath().isEmpty()) {
            return null;
        }

        String campo = erroJackson.getPath().stream()
                .map(JacksonException.Reference::getPropertyName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("."));
        return campo.isBlank() ? null : campo;
    }
}
