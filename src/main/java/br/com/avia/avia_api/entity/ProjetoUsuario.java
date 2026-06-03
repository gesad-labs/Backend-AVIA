package br.com.avia.avia_api.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "projeto_usuario")
@Getter
@Setter
@NoArgsConstructor
public class ProjetoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Setter(AccessLevel.NONE)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;

    @Column(name = "cargo")
    private String cargo;

    @Column(name = "papel_id")
    private int papelId;

    public ProjetoUsuario(String cargo, Usuario usuario, Projeto projeto) {
        this.cargo = cargo;
        this.usuario = usuario;
        this.projeto = projeto;
    }
}
