package br.com.avia.avia_api.exception.tarefa;

public class TarefaPossuiSubtarefasException extends RuntimeException {

    public TarefaPossuiSubtarefasException() {
        super("A tarefa possui subtarefas e não pode ser excluída. Exclua ou desvincule as subtarefas antes");
    }
}