package br.com.avia.avia_api.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoEstimativaProjeto {
    HORAS ("Horas"),
    PONTOS ("Pontos");

    private final String tipoEstimativa;

    TipoEstimativaProjeto(String tipoEstimativa) {
        this.tipoEstimativa = tipoEstimativa;
    }

    @JsonValue
    public String getEstimativaProjeto(){return tipoEstimativa;}

    @JsonCreator
    public static TipoEstimativaProjeto deString(String valor) {
        if (valor == null) return null;
        for (TipoEstimativaProjeto tipo : TipoEstimativaProjeto.values()) {
            if (tipo.getEstimativaProjeto().equalsIgnoreCase(valor) || tipo.name().equalsIgnoreCase(valor)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de estimativa desconhecido: " + valor);
    }
}
