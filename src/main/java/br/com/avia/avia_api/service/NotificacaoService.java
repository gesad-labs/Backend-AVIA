package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.notificacao.NotificacaoResponseDTO;
import br.com.avia.avia_api.entity.Comentario;
import br.com.avia.avia_api.entity.Notificacao;
import br.com.avia.avia_api.entity.NotificacaoProjetoUsuario;
import br.com.avia.avia_api.entity.ProjetoUsuario;
import br.com.avia.avia_api.enums.TipoNotificacao;
import br.com.avia.avia_api.repository.NotificacaoProjetoUsuarioRepository;
import br.com.avia.avia_api.repository.NotificacaoRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final NotificacaoProjetoUsuarioRepository notificacaoProjetoUsuarioRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificacaoService(
            NotificacaoRepository notificacaoRepository,
            NotificacaoProjetoUsuarioRepository notificacaoProjetoUsuarioRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.notificacaoRepository = notificacaoRepository;
        this.notificacaoProjetoUsuarioRepository = notificacaoProjetoUsuarioRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void notificarUsuarioAdicionadoAoProjeto(ProjetoUsuario projetoUsuario) {
        Notificacao notificacao = new Notificacao();
        notificacao.setTipo(TipoNotificacao.ADICIONADO_PROJETO);

        notificacaoRepository.save(notificacao);

        criarNotificacaoProjetoUsuario(projetoUsuario, notificacao);
    }

    public void notificarUsuarioMencionadoEmComentario(ProjetoUsuario projetoUsuario, Comentario comentario) {
        Notificacao notificacao = new Notificacao();
        notificacao.setTipo(TipoNotificacao.MENCIONADO_COMENTARIO);
        notificacao.setComentario(comentario);

        notificacaoRepository.save(notificacao);

        criarNotificacaoProjetoUsuario(projetoUsuario, notificacao);
    }

    private void criarNotificacaoProjetoUsuario(ProjetoUsuario projetoUsuario, Notificacao notificacao) {
        NotificacaoProjetoUsuario notificacaoProjetoUsuario = new NotificacaoProjetoUsuario();
        notificacaoProjetoUsuario.setProjetoUsuario(projetoUsuario);
        notificacaoProjetoUsuario.setNotificacao(notificacao);
        notificacaoProjetoUsuario.setLida(false);

        NotificacaoProjetoUsuario salva = notificacaoProjetoUsuarioRepository.save(notificacaoProjetoUsuario);
        enviarNotificacaoEmTempoReal(projetoUsuario, toResponseDTO(salva));
    }

    private void enviarNotificacaoEmTempoReal(ProjetoUsuario projetoUsuario, NotificacaoResponseDTO notificacao) {
        String emailUsuario = projetoUsuario.getUsuario().getEmail();
        String destino = "/queue/projetos/" + projetoUsuario.getProjeto().getId() + "/notificacoes";

        Runnable envio = () -> messagingTemplate.convertAndSendToUser(emailUsuario, destino, notificacao);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    envio.run();
                }
            });
            return;
        }

        envio.run();
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
