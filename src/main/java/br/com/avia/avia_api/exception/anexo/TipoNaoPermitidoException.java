package br.com.avia.avia_api.exception.anexo;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TipoNaoPermitidoException extends RuntimeException {
    public TipoNaoPermitidoException(String message) {
        super(message);
    }
}
