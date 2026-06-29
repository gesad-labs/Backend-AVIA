package br.com.avia.avia_api.exception.tarefa;

public class TarefaNaoPertenceAoProjetoException extends RuntimeException {

    public TarefaNaoPertenceAoProjetoException() {
        super("Tarefa não pertence a esse projeto");
    }
}