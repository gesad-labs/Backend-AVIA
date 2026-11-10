package br.com.avia.avia_api.exception;

import org.springframework.http.HttpStatus;

//Base das exceções de negócio que carregam o próprio status HTTP.

public abstract class AviaException extends RuntimeException {

    private final HttpStatus status;

    protected AviaException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
