package br.com.avia.avia_api.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projetos")
@Setter
@Getter
@NoArgsConstructor
public class Projeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(name = "nome")
    private String nome;

    @Column(name = "tipo_estimativa")
    private String tipoEstimativa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gerente_id", nullable = false)
    private Usuario gerente;

    @OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjetoUsuario> membros = new ArrayList<>();

    public Projeto(String nome, String tipoEstimativa, Usuario gerente) {
        this.nome = nome;
        this.tipoEstimativa = tipoEstimativa;
        this.gerente = gerente;
    }

}
