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
@Table(name = "sprints")
public class Sprint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Getter  private int id;

    @Column(name = "projeto_id")
    @Getter @Setter private int projeto_id;

    @Column(name = "nome")
    @Getter @Setter private String nome;

    @Column(name = "status")
    @Getter @Setter private String status;

    public Sprint() {
    }

    public Sprint(int projeto_id, String nome, String status) {
        this.projeto_id = projeto_id;
        this.nome = nome;
        this.status = status;
    }
}
