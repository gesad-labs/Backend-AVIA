package br.com.avia.avia_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "votos_estimativa",
       uniqueConstraints = @UniqueConstraint(columnNames = {"tarefa_id", "usuario_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tarefa_id", nullable = false)
    private Long tarefaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "valor", nullable = false)
    private int valor;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    public Voto(Long tarefaId, Usuario usuario, int valor) {
        this.tarefaId = tarefaId;
        this.usuario = usuario;
        this.valor = valor;
        this.criadoEm = LocalDateTime.now();
    }
}
