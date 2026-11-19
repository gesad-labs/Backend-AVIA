package br.com.avia.avia_api.exception.tarefa;

public class HierarquiaTarefaInvalidaException extends RuntimeException {

    public HierarquiaTarefaInvalidaException(String mensagem) {
        super(mensagem);
    }
}