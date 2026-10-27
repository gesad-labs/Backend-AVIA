package br.com.avia.avia_api.exception.anexo;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ArquivoMuitoGrandeException extends RuntimeException {
    public ArquivoMuitoGrandeException(String message) {
        super(message);
    }
}
