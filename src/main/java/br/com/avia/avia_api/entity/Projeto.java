package br.com.avia.avia_api.entity;

import br.com.avia.avia_api.enums.TipoEstimativaProjeto;
import jakarta.persistence.*;
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
    private Long id;

    @Column(name = "nome")
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_estimativa", nullable = false)
    private TipoEstimativaProjeto tipoEstimativa;

    @Setter(AccessLevel.NONE)
    @Column(name = "proximo_numero_tarefa", nullable = false)
    private Integer proximoNumeroTarefa = 1;

    public int consumirProximoNumeroTarefa(){ return proximoNumeroTarefa++;}
}
