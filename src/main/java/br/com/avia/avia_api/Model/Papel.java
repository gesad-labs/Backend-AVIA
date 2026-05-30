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
@Table(name = "papeis")
public class Papel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Getter private long id;
    
    @Column(name = "nome")
    @Getter @Setter private String nome;

    public Papel() {
    }

    public Papel(String nome) {
        this.nome = nome;
    }
}
