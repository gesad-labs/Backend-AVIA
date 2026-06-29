package br.com.avia.avia_api.exception.permissao;

public class PermissaoNegadaException extends RuntimeException {
    public PermissaoNegadaException() {
        super("Você não tem permissão para executar essa ação.");
    }
}
