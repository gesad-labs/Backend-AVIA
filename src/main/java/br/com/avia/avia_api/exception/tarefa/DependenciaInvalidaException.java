package br.com.avia.avia_api.exception.tarefa;

public class DependenciaInvalidaException extends RuntimeException {

    public DependenciaInvalidaException(String mensagem) {
        super(mensagem);
    }
}