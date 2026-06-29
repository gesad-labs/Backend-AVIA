package br.com.avia.avia_api.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "papeis")
public class Papel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Setter(AccessLevel.NONE)
    private Long id;
    
    @Column(name = "nome")
    private String nome;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "papel_permissao",
            joinColumns = @JoinColumn(name = "papel_id"),
            inverseJoinColumns = @JoinColumn(name = "permissao_id")
    )
    private Set<Permissao> permissoes = new HashSet<>();

    public Papel() {
    }

    public Papel(String nome) {
        this.nome = nome;
    }
}
