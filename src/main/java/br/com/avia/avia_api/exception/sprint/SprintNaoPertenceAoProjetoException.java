package br.com.avia.avia_api.exception.sprint;

public class SprintNaoPertenceAoProjetoException extends RuntimeException {

    public SprintNaoPertenceAoProjetoException() {
        super("Sprint não pertence a esse projeto");
    }
}