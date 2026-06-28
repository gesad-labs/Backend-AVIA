package br.com.avia.avia_api.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissoes")
public class Permissao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Getter private int id;

    @Column(name = "descricao")
    @Getter private String descricao;

    @ManyToMany(mappedBy = "permissoes")
    private Set<Papel> papeis = new HashSet<>();

    public Permissao() {
    }

    public Permissao(String descricao) {
        this.descricao = descricao;
    }

}
