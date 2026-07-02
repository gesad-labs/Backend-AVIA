package br.com.avia.avia_api.entity;

import br.com.avia.avia_api.enums.StatusPlanningPokerSessao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "planning_poker_sessoes")
@Getter
@Setter
@NoArgsConstructor
public class PlanningPokerSessao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "projeto_id", nullable = false)
    private Long projetoId;

    @Column(name = "tarefa_id", nullable = false)
    private Long tarefaId;

    @Column(name = "gerente_id", nullable = false)
    private Long gerenteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusPlanningPokerSessao status;

    @Column(name = "rodada_atual", nullable = false)
    private Integer rodadaAtual;

    @Column(name = "estimativa_final")
    private Integer estimativaFinal;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "revelado_em")
    private LocalDateTime reveladoEm;

    @Column(name = "finalizado_em")
    private LocalDateTime finalizadoEm;

    public PlanningPokerSessao(Long projetoId, Long tarefaId, Long gerenteId) {
        this.projetoId = projetoId;
        this.tarefaId = tarefaId;
        this.gerenteId = gerenteId;
        this.status = StatusPlanningPokerSessao.VOTANDO;
        this.rodadaAtual = 1;
        this.criadoEm = LocalDateTime.now();
    }
}