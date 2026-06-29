package br.com.avia.avia_api.exception.sprint;

public class SprintNaoEncontradaException extends RuntimeException {
    public SprintNaoEncontradaException() {
        super("Sprint não encontrada!");
    }
}
