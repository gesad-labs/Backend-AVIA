package br.com.avia.avia_api.Model;

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
    @Getter  private int id;

    @Column(name = "sprint_id")
    @Getter  @Setter private int sprint_id;

    @Column(name = "titulo")
    @Getter  @Setter private String titulo;

    @Column(name = "estimativa")
    @Getter  @Setter private int estimativa;

    @Column(name = "status")
    @Getter  @Setter private String status;

    @Column(name = "tarefa_parente_id")
    @Getter  @Setter private int tarefa_parente_id;

    @Column(name = "impedimento")
    @Getter  @Setter private boolean impedimento;

    public Tarefa() {
    }

    public Tarefa(int sprint_id, String titulo, int estimativa, String status, int tarefa_parente_id,
            boolean impedimento) {
        this.sprint_id = sprint_id;
        this.titulo = titulo;
        this.estimativa = estimativa;
        this.status = status;
        this.tarefa_parente_id = tarefa_parente_id;
        this.impedimento = impedimento;
    }

}
