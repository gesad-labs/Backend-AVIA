package br.com.avia.avia_api.exception;

import org.springframework.http.HttpStatus;


 //O token é válido, mas o usuário dele não existe mais no banco
public class SessaoInvalidaException extends AviaException {

    public SessaoInvalidaException() {
        super(HttpStatus.UNAUTHORIZED, "Sessão expirada ou inválida. Faça login novamente.");
    }
}
