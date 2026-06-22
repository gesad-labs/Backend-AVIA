package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.notificacao.NotificacaoResponseDTO;
import br.com.avia.avia_api.entity.Notificacao;
import br.com.avia.avia_api.entity.NotificacaoProjetoUsuario;
import br.com.avia.avia_api.entity.ProjetoUsuario;
import br.com.avia.avia_api.enums.TipoNotificacao;
import br.com.avia.avia_api.repository.NotificacaoProjetoUsuarioRepository;
import br.com.avia.avia_api.repository.NotificacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final NotificacaoProjetoUsuarioRepository notificacaoProjetoUsuarioRepository;

    public NotificacaoService(
            NotificacaoRepository notificacaoRepository,
            NotificacaoProjetoUsuarioRepository notificacaoProjetoUsuarioRepository
    ) {
        this.notificacaoRepository = notificacaoRepository;
        this.notificacaoProjetoUsuarioRepository = notificacaoProjetoUsuarioRepository;
    }

    public void notificarUsuarioAdicionadoAoProjeto(ProjetoUsuario projetoUsuario) {
        Notificacao notificacao = new Notificacao();
        notificacao.setTipo(TipoNotificacao.ADICIONADO_PROJETO);

        notificacaoRepository.save(notificacao);

        NotificacaoProjetoUsuario notificacaoProjetoUsuario = new NotificacaoProjetoUsuario();
        notificacaoProjetoUsuario.setProjetoUsuario(projetoUsuario);
        notificacaoProjetoUsuario.setNotificacao(notificacao);
        notificacaoProjetoUsuario.setLida(false);

        notificacaoProjetoUsuarioRepository.save(notificacaoProjetoUsuario);
    }

    @Transactional(readOnly = true)
    public List<NotificacaoResponseDTO> listarNotificacoesDoProjeto(Long projetoId, String emailUsuario) {
        return notificacaoProjetoUsuarioRepository
                .findByProjetoUsuarioProjetoIdAndProjetoUsuarioUsuarioEmailOrderByNotificacaoCriadoEmDesc(
                        projetoId,
                        emailUsuario
                )
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Long contarNaoLidasDoProjeto(Long projetoId, String emailUsuario) {
        return notificacaoProjetoUsuarioRepository
                .countByProjetoUsuarioProjetoIdAndProjetoUsuarioUsuarioEmailAndLidaFalse(
                        projetoId,
                        emailUsuario
                );
    }

    @Transactional
    public void marcarComoLidaNoProjeto(Long projetoId, Long notificacaoProjetoUsuarioId, String emailUsuario) {
        NotificacaoProjetoUsuario notificacaoProjetoUsuario =
                notificacaoProjetoUsuarioRepository
                        .findByIdAndProjetoUsuarioProjetoIdAndProjetoUsuarioUsuarioEmail(
                                notificacaoProjetoUsuarioId,
                                projetoId,
                                emailUsuario
                        )
                        .orElseThrow(() -> new RuntimeException("Notificação não encontrada neste projeto."));

        notificacaoProjetoUsuario.setLida(true);

        notificacaoProjetoUsuarioRepository.save(notificacaoProjetoUsuario);
    }

    private NotificacaoResponseDTO toResponseDTO(NotificacaoProjetoUsuario notificacaoProjetoUsuario) {
        Notificacao notificacao = notificacaoProjetoUsuario.getNotificacao();

        String nomeProjeto = notificacaoProjetoUsuario
                .getProjetoUsuario()
                .getProjeto()
                .getNome();

        String mensagem = gerarMensagem(notificacaoProjetoUsuario);

        return new NotificacaoResponseDTO(
                notificacaoProjetoUsuario.getId(),
                notificacao.getTipo(),
                mensagem,
                nomeProjeto,
                notificacaoProjetoUsuario.getLida(),
                notificacao.getCriadoEm()
        );
    }

    private String gerarMensagem(NotificacaoProjetoUsuario notificacaoProjetoUsuario) {
        Notificacao notificacao = notificacaoProjetoUsuario.getNotificacao();

        return switch (notificacao.getTipo()) {
            case ADICIONADO_PROJETO -> "Você foi adicionado ao projeto "
                    + notificacaoProjetoUsuario.getProjetoUsuario().getProjeto().getNome() + ".";

            case RESPONSAVEL_TAREFA -> "Você foi adicionado como responsável por uma tarefa.";

            case MENCIONADO_COMENTARIO -> "Você foi mencionado em um comentário.";
        };
    }

}