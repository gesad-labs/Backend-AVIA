package br.com.avia.avia_api.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CargoProjeto {
    GERENTE ("gerente"),
    PROJECTOWNER ("project owner"),
    MEMBRO ("membro"),
    VISUALIZADOR ("visualizador");

    final String nomeCargo;

    CargoProjeto(String nomeCargo) {
        this.nomeCargo = nomeCargo;
    }

    @JsonValue
    public String getNomeCargo() {
        return nomeCargo;
    }
}
