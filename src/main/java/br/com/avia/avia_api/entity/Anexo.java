package br.com.avia.avia_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "anexos")
public class Anexo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Getter private int id;

    @Column(name = "tarefa_id")
    @Getter @Setter private long tarefaId;

    @Column(name = "nome")
    @Getter @Setter private String nome;

    @Column(name = "tipo_arquivo")
    @Getter @Setter private String tipoArquivo;

    @Column(name = "url")
    @Getter @Setter private String url;

    public Anexo() {
    }

    public Anexo(long tarefaId, String nome, String tipoArquivo, String url) {
        this.tarefaId = tarefaId;
        this.nome = nome;
        this.tipoArquivo = tipoArquivo;
        this.url = url;
    }
}
