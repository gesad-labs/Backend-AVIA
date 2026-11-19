package br.com.avia.avia_api.exception.auth;

public class UsuarioNaoEncontradoException extends RuntimeException {
    public UsuarioNaoEncontradoException() {
        super("E-mail ou senha inválidos");
    }

    public UsuarioNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
