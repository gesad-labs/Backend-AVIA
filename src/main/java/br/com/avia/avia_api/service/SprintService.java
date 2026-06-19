package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.sprint.*;
import br.com.avia.avia_api.entity.Projeto;
import br.com.avia.avia_api.entity.Sprint;
import br.com.avia.avia_api.entity.Usuario;
import br.com.avia.avia_api.repository.ProjetoRepository;
import br.com.avia.avia_api.repository.SprintRepository;
import br.com.avia.avia_api.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;

    public SprintService(SprintRepository sprintRepository, ProjetoRepository projetoRepository, UsuarioRepository usuarioRepository) {
        this.sprintRepository = sprintRepository;
        this.projetoRepository = projetoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public SprintResponseDTO criarSprint(Long projetoId, CriarSprintRequestDTO dto, String emailUsuario) {

        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);

        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        boolean sprintJaExiste = sprintRepository
                .existsByProjetoIdAndNome(projetoId, dto.nome());

        if (sprintJaExiste) {
            throw new RuntimeException("Já existe uma sprint com esse nome neste projeto");
        }

        Sprint sprint = new Sprint(
                projeto.getId(),
                dto.nome(),
                "Em andamento"
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
}