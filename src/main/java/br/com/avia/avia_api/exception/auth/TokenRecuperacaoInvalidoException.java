package br.com.avia.avia_api.exception.auth;

public class TokenRecuperacaoInvalidoException extends RuntimeException {
    public TokenRecuperacaoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
