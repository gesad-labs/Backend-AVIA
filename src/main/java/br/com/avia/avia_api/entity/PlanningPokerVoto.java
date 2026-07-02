package br.com.avia.avia_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "planning_poker_votos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"sessao_id", "usuario_id", "rodada"})
)
@Getter
@Setter
@NoArgsConstructor
public class PlanningPokerVoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessao_id", nullable = false)
    private PlanningPokerSessao sessao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "rodada", nullable = false)
    private Integer rodada;

    @Column(name = "valor", nullable = false)
    private Integer valor;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public PlanningPokerVoto(PlanningPokerSessao sessao, Usuario usuario, Integer rodada, Integer valor) {
        this.sessao = sessao;
        this.usuario = usuario;
        this.rodada = rodada;
        this.valor = valor;
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    public void atualizarValor(Integer valor) {
        this.valor = valor;
        this.atualizadoEm = LocalDateTime.now();
    }
}