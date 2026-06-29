package br.com.avia.avia_api.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PermissoesUsuarioProjeto {
    CRIARTAREFAS("Criar Tarefas"),
    EDITARTAREFAS("Editar Tarefas"),
    EXCLUIRTAREFAS("Excluir Tarefas"),
    MOVERTAREFAS("Mover Tarefas"),
    CRIARCOLUNAS("Criar Colunas"),
    EDITARESTRUTURA("Editar Estrutura"),
    GERENCIARSPRINTS("Gerenciar Sprints"),
    ADICIONARMEMBROS("Adicionar Membros"),
    REMOVERMEMBROS("Remover Membros"),
    VISUALIZARPROJETO("Visualizar Projeto"); 

    private final String nome;


    PermissoesUsuarioProjeto(String nome) {
        this.nome = nome;
    }

    @JsonValue
    public String getNome() {return nome;}

    @JsonCreator
    public static PermissoesUsuarioProjeto deString(String valor){
        if (valor == null) return null;
        for (PermissoesUsuarioProjeto permissoao : PermissoesUsuarioProjeto.values()) {
            if (permissoao.getNome().equalsIgnoreCase(valor) || permissoao.name().equalsIgnoreCase(valor)) {
                return permissoao;
            }
        }
        throw new IllegalArgumentException("Permissão desconhecido: " + valor);
    }
}
