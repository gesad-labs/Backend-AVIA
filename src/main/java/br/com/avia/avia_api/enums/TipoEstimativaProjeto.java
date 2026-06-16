package br.com.avia.avia_api.enums;

import lombok.Getter;

@Getter
public enum TipoEstimativaProjeto {
    HORAS ("Horas"),
    PONTOS ("Pontos");

    private final String tipoEstimativa;

    TipoEstimativaProjeto(String tipoEstimativa) {
        this.tipoEstimativa = tipoEstimativa;
    }
}
