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
@Table(name = "projeto_usuario")
public class ProjetoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Getter private long id;

    @Column(name = "usuario_id")
    @Getter @Setter private long usuarioId;

    @Column(name = "projeto_id")
    @Getter @Setter private long projetoId;

    @Column(name = "cargo")
    @Getter @Setter private String cargo;

    @Column(name = "papel_id")
    @Getter @Setter private int papelId;

    public ProjetoUsuario() {
    }

    public ProjetoUsuario(long usuarioId, long projetoId, String cargo, int papelId) {
        this.usuarioId = usuarioId;
        this.projetoId = projetoId;
        this.cargo = cargo;
        this.papelId = papelId;
    }
}
