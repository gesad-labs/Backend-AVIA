package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.sprint.*;
import br.com.avia.avia_api.entity.*;
import br.com.avia.avia_api.enums.StatusSprint;
import br.com.avia.avia_api.exception.permissao.PermissaoNegadaException;
import br.com.avia.avia_api.exception.sprint.SprintJaExisteException;
import br.com.avia.avia_api.exception.sprint.SprintNaoEncontradaException;
import br.com.avia.avia_api.repository.ProjetoRepository;
import br.com.avia.avia_api.repository.ProjetoUsuarioRepository;
import br.com.avia.avia_api.repository.SprintRepository;
import br.com.avia.avia_api.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SprintService {

    private final ProjetoUsuarioRepository projetoUsuarioRepository;
    private final SprintRepository sprintRepository;
    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;

    public SprintService(ProjetoUsuarioRepository projetoUsuarioRepository, SprintRepository sprintRepository, ProjetoRepository projetoRepository, UsuarioRepository usuarioRepository) {
        this.projetoUsuarioRepository = projetoUsuarioRepository;
        this.sprintRepository = sprintRepository;
        this.projetoRepository = projetoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public SprintResponseDTO criarSprint(Long projetoId, CriarSprintRequestDTO dto, String emailUsuario) {

        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);

        verificarPermissao(projetoId, usuarioLogado.getId(), "GERENCIARSPRINTS");

        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

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
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

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

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(SprintNaoEncontradaException::new);

        if (dto.nome() != null) sprint.setNome(dto.nome());

        if (dto.status() != null) sprint.setStatus(dto.status());

        Sprint sprintAtualizada = sprintRepository.save(sprint);

        return converterParaDTO(sprintAtualizada);
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
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    private void verificarPermissao(Long projetoId, Long usuarioId, String permissaoLabel){
        Papel membroPapel = projetoUsuarioRepository.findPapelByProjetoIdAndUsuarioId(projetoId, usuarioId);
        Set<Permissao> membroPermissoes = membroPapel.getPermissoes();

        boolean possuiPermissao = membroPermissoes.stream()
                .anyMatch(p -> p.getDescricao().equals(permissaoLabel));

        if(!possuiPermissao)
            throw new PermissaoNegadaException();
    }
}