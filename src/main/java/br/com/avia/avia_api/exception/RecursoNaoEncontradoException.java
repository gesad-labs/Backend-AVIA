package br.com.avia.avia_api.exception;

import org.springframework.http.HttpStatus;

public class RecursoNaoEncontradoException extends AviaException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(HttpStatus.NOT_FOUND, mensagem);
    }
}
