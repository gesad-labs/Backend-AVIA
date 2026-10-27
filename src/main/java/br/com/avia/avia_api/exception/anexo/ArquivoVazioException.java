package br.com.avia.avia_api.exception.anexo;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ArquivoVazioException extends RuntimeException {
    public ArquivoVazioException(String message) {
        super(message);
    }
}
