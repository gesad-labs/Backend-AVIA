package br.com.avia.avia_api.exception.sprint;

public class IntervaloSprintsInvalidoException extends RuntimeException {

    public IntervaloSprintsInvalidoException() {
        super("A sprint inicial do intervalo deve ter sido criada antes da sprint final");
    }
}
