package br.com.avia.avia_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tarefas")
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Getter  private long id;

    @Column(name = "sprint_id")
    @Getter  @Setter private long sprintId;

    @Column(name = "titulo")
    @Getter  @Setter private String titulo;

    @Column(name = "estimativa")
    @Getter  @Setter private int estimativa;

    @Column(name = "status")
    @Getter  @Setter private String status;

    @Column(name = "tarefa_parente_id")
    @Getter  @Setter private int tarefaParenteId;

    @Column(name = "impedimento")
    @Getter  @Setter private boolean impedimento;

    public Tarefa() {
    }

    public Tarefa(long sprintId, String titulo, int estimativa, String status, int tarefaParenteId,
            boolean impedimento) {
        this.sprintId = sprintId;
        this.titulo = titulo;
        this.estimativa = estimativa;
        this.status = status;
        this.tarefaParenteId = tarefaParenteId;
        this.impedimento = impedimento;
    }

}
