package br.com.avia.avia_api.exception.sprint;

public class SprintJaExisteException extends RuntimeException {

    public SprintJaExisteException() {
        super("Já existe uma sprint com esse nome neste projeto");
    }
}