package br.com.avia.avia_api.exception;

import org.springframework.http.HttpStatus;

public class ConflitoException extends AviaException {

    public ConflitoException(String mensagem) {
        super(HttpStatus.CONFLICT, mensagem);
    }
}
