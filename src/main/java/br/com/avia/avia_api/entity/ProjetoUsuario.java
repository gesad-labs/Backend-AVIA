package br.com.avia.avia_api.entity;

import br.com.avia.avia_api.enums.CargoProjeto;
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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;

    @Enumerated(EnumType.STRING)
    @Column(name = "cargo", nullable = false)
    private CargoProjeto cargo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "papel_id")
    private Papel papel;

    public ProjetoUsuario(CargoProjeto cargo, Usuario usuario, Projeto projeto, Papel papel) {
        this.cargo = cargo;
        this.usuario = usuario;
        this.projeto = projeto;
        this.papel = papel;
    }
}
