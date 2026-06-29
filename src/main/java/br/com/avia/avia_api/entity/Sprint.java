package br.com.avia.avia_api.entity;

import br.com.avia.avia_api.enums.StatusSprint;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sprints")
@Getter
@Setter
@NoArgsConstructor
public class Sprint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(name = "projeto_id")
    private long projetoId;

    @Column(name = "nome")
    private String nome;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusSprint status;

    public Sprint(long projetoId, String nome, StatusSprint status) {
        this.projetoId = projetoId;
        this.nome = nome;
        this.status = status;
    }
}
