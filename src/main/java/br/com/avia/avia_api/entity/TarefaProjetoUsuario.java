package br.com.avia.avia_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tarefa_projeto_usuarios")
@Getter
@Setter
@NoArgsConstructor
public class TarefaProjetoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tarefa_id")
    private Long tarefaId;

    @Column(name = "projeto_usuario_id")
    private Long projetoUsuarioId;

    public TarefaProjetoUsuario(Long tarefaId, Long projetoUsuarioId) {
        this.tarefaId = tarefaId;
        this.projetoUsuarioId = projetoUsuarioId;
    }
}