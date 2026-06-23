package br.com.avia.avia_api.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tarefas")
@Setter
@Getter
@NoArgsConstructor
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(name = "projeto_id")
    private long projetoId;

    @Column(name = "sprint_id")
    private long sprintId;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "estimativa")
    private int estimativa;

    @Column(name = "status")
    private String status;

    @Column(name = "prioridade")
    private String prioridade;

    @Column(name = "tarefa_parente_id")
    private Integer tarefaParenteId;

    @Column(name = "impedimento")
    private boolean impedimento;

    public Tarefa(long projetoId,
                  long sprintId,
                  String titulo,
                  String descricao,
                  int estimativa,
                  String status,
                  String prioridade,
                  Integer tarefaParenteId,
                  boolean impedimento) {
        this.projetoId = projetoId;
        this.sprintId = sprintId;
        this.titulo = titulo;
        this.descricao = descricao;
        this.estimativa = estimativa;
        this.status = status;
        this.prioridade = prioridade;
        this.tarefaParenteId = tarefaParenteId;
        this.impedimento = impedimento;
    }
}
