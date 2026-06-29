package br.com.avia.avia_api.exception.anexo;

public class AnexoNaoEncontradoException extends RuntimeException {

    public AnexoNaoEncontradoException() {
        super("Anexo não encontrado");
    }
}