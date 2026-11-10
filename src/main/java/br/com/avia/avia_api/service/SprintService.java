package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.sprint.*;
import br.com.avia.avia_api.entity.*;
import br.com.avia.avia_api.enums.StatusSprint;
import br.com.avia.avia_api.enums.StatusTarefa;
import br.com.avia.avia_api.exception.ConflitoException;
import br.com.avia.avia_api.exception.RecursoNaoEncontradoException;
import br.com.avia.avia_api.exception.SessaoInvalidaException;
import br.com.avia.avia_api.exception.permissao.PermissaoNegadaException;
import br.com.avia.avia_api.exception.sprint.SprintJaExisteException;
import br.com.avia.avia_api.exception.sprint.SprintNaoEncontradaException;
import br.com.avia.avia_api.exception.sprint.SprintNaoPertenceAoProjetoException;
import br.com.avia.avia_api.repository.ProjetoRepository;
import br.com.avia.avia_api.repository.ProjetoUsuarioRepository;
import br.com.avia.avia_api.repository.SprintRepository;
import br.com.avia.avia_api.repository.TarefaRepository;
import br.com.avia.avia_api.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SprintService {

    private final ProjetoUsuarioRepository projetoUsuarioRepository;
    private final SprintRepository sprintRepository;
    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TarefaRepository tarefaRepository;

    public SprintService(ProjetoUsuarioRepository projetoUsuarioRepository, SprintRepository sprintRepository, ProjetoRepository projetoRepository, UsuarioRepository usuarioRepository, TarefaRepository tarefaRepository) {
        this.projetoUsuarioRepository = projetoUsuarioRepository;
        this.sprintRepository = sprintRepository;
        this.projetoRepository = projetoRepository;
        this.usuarioRepository = usuarioRepository;
        this.tarefaRepository = tarefaRepository;
    }

    @Transactional
    public SprintResponseDTO criarSprint(Long projetoId, CriarSprintRequestDTO dto, String emailUsuario) {

        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);

        verificarPermissao(projetoId, usuarioLogado.getId(), "GERENCIARSPRINTS");

        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto não encontrado."));

        boolean sprintJaExiste = sprintRepository
                .existsByProjetoIdAndNome(projetoId, dto.nome());

        if (sprintJaExiste) {
            throw new SprintJaExisteException();
        }

        Sprint sprint = new Sprint(
                projeto.getId(),
                dto.nome(),
                StatusSprint.EM_PLANEJAMENTO
        );

        Sprint sprintSalva = sprintRepository.save(sprint);

        return converterParaDTO(sprintSalva);
    }


    @Transactional
    public List<SprintResponseDTO> listarSprintsPorProjeto(Long projetoId, String emailUsuario) {

        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);

        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto não encontrado."));

        List<Sprint> sprints = sprintRepository.findByProjetoId(projetoId);

        return sprints.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SprintResponseDTO editarSprint(Long projetoId, Long sprintId, EditarSprintRequestDTO dto,
                                          String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);

        verificarPermissao(projetoId, usuarioLogado.getId(), "GERENCIARSPRINTS");

        projetoRepository.buscarComLock(projetoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto não encontrado."));

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(SprintNaoEncontradaException::new);
        if (!Objects.equals(sprint.getProjetoId(), projetoId)) {
            throw new SprintNaoPertenceAoProjetoException();
        }

        if (dto.nome() != null) sprint.setNome(dto.nome());

        if (dto.status() != null && dto.status() != sprint.getStatus()) {
            validarTransicao(sprint, dto.status());
            sprint.setStatus(dto.status());
        }

        Sprint sprintAtualizada = sprintRepository.save(sprint);

        return converterParaDTO(sprintAtualizada);
    }

    private void validarTransicao(Sprint sprint, StatusSprint novoStatus) {
        StatusSprint atual = sprint.getStatus();

        if (atual == StatusSprint.EM_PLANEJAMENTO && novoStatus == StatusSprint.EM_ANDAMENTO) {
            validarInicio(sprint);
        } else if (atual == StatusSprint.EM_ANDAMENTO && novoStatus == StatusSprint.CONCLUIDA) {
            validarEncerramento(sprint);
        } else if (atual == StatusSprint.CONCLUIDA) {
            throw new ConflitoException("Uma sprint concluída não pode ser reaberta.");
        } else if (atual == StatusSprint.EM_ANDAMENTO) {
            throw new ConflitoException("Uma sprint em andamento não pode voltar para o planejamento.");
        } else {
            throw new ConflitoException("A sprint precisa ser iniciada antes de ser encerrada.");
        }
    }

    private void validarInicio(Sprint sprint) {
        if (sprintRepository.existsByProjetoIdAndStatus(sprint.getProjetoId(), StatusSprint.EM_ANDAMENTO)) {
            throw new ConflitoException("Já existe uma sprint em andamento neste projeto. Encerre-a antes de iniciar outra.");
        }
    }

    private void validarEncerramento(Sprint sprint) {
        long pendentes = tarefaRepository.countBySprintIdAndStatusNot(sprint.getId(), StatusTarefa.CONCLUIDA);
        if (pendentes == 1) {
            throw new ConflitoException("A sprint não pode ser encerrada: existe 1 atividade pendente.");
        }
        if (pendentes > 1) {
            throw new ConflitoException("A sprint não pode ser encerrada: existem " + pendentes + " atividades pendentes.");
        }
    }

    private SprintResponseDTO converterParaDTO(Sprint sprint) {
        return new SprintResponseDTO(
                sprint.getId(),
                sprint.getNome(),
                sprint.getProjetoId(),
                sprint.getStatus()
        );
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(SessaoInvalidaException::new);
    }

    private void verificarPermissao(Long projetoId, Long usuarioId, String permissaoLabel){
        ProjetoUsuario membro = projetoUsuarioRepository.findByUsuarioIdAndProjetoId(usuarioId, projetoId)
                .orElseThrow(PermissaoNegadaException::new);
        List<Permissao> membroPermissoes = membro.getPermissoes();

        boolean possuiPermissao = membroPermissoes.stream()
                .anyMatch(p -> p.getDescricao().equals(permissaoLabel));

        if(!possuiPermissao)
            throw new PermissaoNegadaException();
    }
}