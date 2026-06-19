package br.com.avia.avia_api.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoEstimativaProjeto {
    HORAS ("Horas"),
    PONTOS ("Pontos");

    private final String tipoEstimativa;

    TipoEstimativaProjeto(String tipoEstimativa) {
        this.tipoEstimativa = tipoEstimativa;
    }

    @JsonValue
    String getEstimativaProjeto(){return tipoEstimativa;}
}
