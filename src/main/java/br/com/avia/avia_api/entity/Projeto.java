package br.com.avia.avia_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    public Projeto(String nome, String tipoEstimativa) {
        this.nome = nome;
        this.tipoEstimativa = tipoEstimativa;
    }

}
