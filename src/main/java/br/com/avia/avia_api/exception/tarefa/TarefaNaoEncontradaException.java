package br.com.avia.avia_api.exception.tarefa;

public class TarefaNaoEncontradaException extends RuntimeException {

    public TarefaNaoEncontradaException() {
        super("Tarefa não encontrada");
    }
}