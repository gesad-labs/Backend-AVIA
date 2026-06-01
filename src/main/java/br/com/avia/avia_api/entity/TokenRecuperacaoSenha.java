package br.com.avia.avia_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens_recuperacao_senha")
@Getter
@Setter
@NoArgsConstructor
public class TokenRecuperacaoSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(nullable = false)
    private boolean usado = false;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public TokenRecuperacaoSenha(String token, LocalDateTime dataExpiracao, Usuario usuario) {
        this.token = token;
        this.dataExpiracao = dataExpiracao;
        this.usuario = usuario;
    }

    public boolean expirado() {
        return LocalDateTime.now().isAfter(dataExpiracao);
    }
}