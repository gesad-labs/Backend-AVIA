package br.com.avia.avia_api.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "permissoes")
public class Permissao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Getter private int id;

    @Column(name = "descricao")
    @Getter private String descricao;

    public Permissao() {
    }

    public Permissao(String descricao) {
        this.descricao = descricao;
    }

}
