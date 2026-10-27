package br.com.avia.avia_api.exception.tarefa;

public class SubtarefasPendentesException extends RuntimeException {

    public SubtarefasPendentesException() {
        super("A tarefa possui subtarefas pendentes e não pode ser concluída");
    }
}