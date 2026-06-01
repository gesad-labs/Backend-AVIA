package br.com.avia.avia_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Column(name = "sprint_id")
    private long sprintId;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "estimativa")
    private int estimativa;

    @Column(name = "status")
    private String status;

    @Column(name = "tarefa_parente_id")
    private int tarefaParenteId;

    @Column(name = "impedimento")
    private boolean impedimento;

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
