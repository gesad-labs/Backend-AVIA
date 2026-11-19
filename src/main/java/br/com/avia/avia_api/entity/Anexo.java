package br.com.avia.avia_api.entity;

import br.com.avia.avia_api.enums.TipoAnexo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "anexos")
public class Anexo {
    //o anexo deve ser excluido com a tarefa?

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Getter private Long id;

    @Column(name = "tarefa_id", nullable = false)
    @Getter private Long tarefaId;

    @Column(name = "nome", nullable = false)
    @Getter private String nomeArquivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @Getter private Usuario enviadoPor;

    @Column(name = "tamanho_bytes", nullable = false)
    @Getter private Long tamanhoBytes;

    @Column(name = "data_criacao", nullable = false)
    @Getter private LocalDateTime criadoEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_arquivo", nullable = false)
    @Getter private TipoAnexo tipoAnexo;

    @Column(name = "chave_armazenamento", nullable = false, unique = true)
    @Getter private String chaveArmazenamento;

    public Anexo() {}
    public Anexo(Long tarefaId, Long tamanhoBytes, TipoAnexo tipoArquivo, String chaveArmazenamento,
                 LocalDateTime criadoEm, Usuario enviadoPor, String nomeArquivo) {
        this.tarefaId = tarefaId;
        this.tamanhoBytes = tamanhoBytes;
        this.tipoAnexo = tipoArquivo;
        this.chaveArmazenamento = chaveArmazenamento;
        this.criadoEm = criadoEm;
        this.enviadoPor = enviadoPor;
        this.nomeArquivo = nomeArquivo;
    }
}
