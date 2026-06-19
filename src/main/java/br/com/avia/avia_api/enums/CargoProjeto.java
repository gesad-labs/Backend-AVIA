package br.com.avia.avia_api.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CargoProjeto {
    GERENTE ("Gerente"),
    SCRUMMASTER ("Scrum Master"),
    DESENVOLVEDOR ("Desenvolvedor"),
    QA ("QA"),
    ANALISTADEREQUISISTOS ("Analista de Requisistos"),
    DESIGNERDEUIUX ("Designer de UI/UX"),
    ARQUITETODESOFTWARE ("Arquiteto de Software"),
    DEVOPS ("DevOps"),
    VISUALIZADOR ("Visualizador");

    final String nomeCargo;

    CargoProjeto(String nomeCargo) {
        this.nomeCargo = nomeCargo;
    }

    @JsonValue
    public String getNomeCargo() {
        return nomeCargo;
    }

    public static CargoProjeto deString(String valor) {
        if (valor == null) return null;
        for (CargoProjeto cargo : CargoProjeto.values()) {
            if (cargo.getNomeCargo().equalsIgnoreCase(valor)) {
                return cargo;
            }
        }
        throw new IllegalArgumentException("Cargo desconhecido: " + valor);
    }
}
