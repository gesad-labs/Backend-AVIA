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
@Table(name = "projetos")
public class Projeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Getter  private int id;

    @Column(name = "nome")
    @Getter  @Setter  private String nome;

    @Column(name = "tipo_estimativa")
    @Getter  @Setter  private String tipo_estimativa;

    public Projeto() {
    }

    public Projeto(String nome, String tipo_estimativa) {
        this.nome = nome;
        this.tipo_estimativa = tipo_estimativa;
    }

}
