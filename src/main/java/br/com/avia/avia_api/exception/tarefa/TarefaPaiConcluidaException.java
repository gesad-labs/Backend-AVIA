package br.com.avia.avia_api.exception.tarefa;

public class TarefaPaiConcluidaException extends RuntimeException {

    public TarefaPaiConcluidaException() {
        super("A tarefa pai está concluída. Reabra a tarefa pai antes de adicionar ou reabrir subtarefas");
    }
}