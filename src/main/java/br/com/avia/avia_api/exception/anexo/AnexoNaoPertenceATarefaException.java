package br.com.avia.avia_api.exception.anexo;

public class AnexoNaoPertenceATarefaException extends RuntimeException {

    public AnexoNaoPertenceATarefaException() {
        super("Anexo não pertence a essa tarefa");
    }
}