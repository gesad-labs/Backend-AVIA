package br.com.avia.avia_api.exception;

import org.springframework.http.HttpStatus;

public class RequisicaoInvalidaException extends AviaException {

    public RequisicaoInvalidaException(String mensagem) {
        super(HttpStatus.BAD_REQUEST, mensagem);
    }
}
