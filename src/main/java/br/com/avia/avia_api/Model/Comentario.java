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
@Table(name = "comentarios")
public class Comentario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Getter private int id;

    @Column(name = "tarefa_id")
    @Getter @Setter private long tarefaId;

    @Column(name = "descricao")
    @Getter @Setter private String descricao;

    @Column(name = "projeto_usuario_id")
    @Getter @Setter private int projetoUsuarioId;

    public Comentario() {
    }

    public Comentario(long tarefaId, String descricao, int projetoUsuarioId) {
        this.tarefaId = tarefaId;
        this.descricao = descricao;
        this.projetoUsuarioId = projetoUsuarioId;
    }
}
