package br.com.avia.avia_api.exception.tarefa;

public class TarefaBloqueadaException extends RuntimeException {

    public TarefaBloqueadaException(String tituloPredecessora) {
        super("A tarefa está bloqueada por \"" + tituloPredecessora + "\" e só pode ir para FAZENDO depois que ela for concluída");
    }
}