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
@Table(name = "notificacoes")
public class Notificacao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Getter private long id;

    @Column(name = "tarefa_id")
    @Getter @Setter private long tarefaId;

    @Column(name = "tipo")
    @Getter @Setter private String tipo;

    @Column(name = "comentario_id")
    @Getter @Setter private int comentarioId;

    public Notificacao() {
    }

    public Notificacao(long tarefaId, String tipo, int comentarioId) {
        this.tarefaId = tarefaId;
        this.tipo = tipo;
        this.comentarioId = comentarioId;
    }
    
}
