package br.com.avia.avia_api.entity;

import br.com.avia.avia_api.enums.PrioridadeTarefa;
import br.com.avia.avia_api.enums.StatusTarefa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private Long id;

    @Column(name = "projeto_id")
    private Long projetoId;

    @Column(name = "sprint_id")
    private Long sprintId;

    @NotNull
    @Column(name = "titulo")
    private String titulo;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "estimativa")
    private Integer estimativa;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusTarefa status;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridade")
    private PrioridadeTarefa prioridade;

    @Column(name = "tarefa_parente_id")
    private Long tarefaParenteId;

    @Column(name = "impedimento")
    private Boolean impedimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

    public Tarefa(Long projetoId,
                  Long sprintId,
                  String titulo,
                  String descricao,
                  Integer estimativa,
                  StatusTarefa status,
                  PrioridadeTarefa prioridade,
                  Long tarefaParenteId,
                  Boolean impedimento) {
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
