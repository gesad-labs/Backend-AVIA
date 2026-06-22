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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notificacoes_projeto_usuario")
@Getter
@Setter
@NoArgsConstructor
public class NotificacaoProjetoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_usuario_id", nullable = false)
    private ProjetoUsuario projetoUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacao_id", nullable = false)
    private Notificacao notificacao;

    @Column(name = "lida", nullable = false)
    private Boolean lida = false;
}