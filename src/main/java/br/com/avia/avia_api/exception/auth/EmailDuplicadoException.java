package br.com.avia.avia_api.exception.auth;

public class EmailDuplicadoException extends RuntimeException {
    public EmailDuplicadoException() {
        super("Esse e-mail já está cadastrado");
    }
}
