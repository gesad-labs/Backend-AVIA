package br.com.avia.avia_api.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TipoEstimativaProjeto {
    HORAS ("Horas"),
    PONTOS ("Pontos");

    private final String tipoEstimativa;

    TipoEstimativaProjeto(String tipoEstimativa) {
        this.tipoEstimativa = tipoEstimativa;
    }
}
