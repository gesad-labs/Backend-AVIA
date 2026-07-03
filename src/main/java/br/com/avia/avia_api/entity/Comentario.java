package br.com.avia.avia_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Getter private Long id;

    @Column(name = "tarefa_id")
    @Getter @Setter private Long tarefaId;

    @Column(name = "descricao", columnDefinition = "TEXT")
    @Getter @Setter private String descricao;

    @Column(name = "projeto_usuario_id")
    @Getter @Setter private Long projetoUsuarioId;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    @Getter private LocalDateTime criadoEm;

    public Comentario() {}

    public Comentario(Long tarefaId, String descricao, Long projetoUsuarioId) {
        this.tarefaId = tarefaId;
        this.descricao = descricao;
        this.projetoUsuarioId = projetoUsuarioId;
    }
}
