package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.tarefa.SessaoPokerResponseDTO;
import br.com.avia.avia_api.dto.tarefa.TarefaResponseDTO;
import br.com.avia.avia_api.dto.tarefa.VotoResponseDTO;
import br.com.avia.avia_api.entity.Tarefa;
import br.com.avia.avia_api.entity.Usuario;
import br.com.avia.avia_api.entity.Voto;
import br.com.avia.avia_api.enums.PapelProjeto;
import br.com.avia.avia_api.repository.ProjetoUsuarioRepository;
import br.com.avia.avia_api.repository.SprintRepository;
import br.com.avia.avia_api.repository.TarefaRepository;
import br.com.avia.avia_api.repository.UsuarioRepository;
import br.com.avia.avia_api.repository.VotoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VotoService {

    private final VotoRepository votoRepository;
    private final TarefaRepository tarefaRepository;
    private final SprintRepository sprintRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProjetoUsuarioRepository projetoUsuarioRepository;
    private final TarefaService tarefaService;

    public VotoService(VotoRepository votoRepository,
                       TarefaRepository tarefaRepository,
                       SprintRepository sprintRepository,
                       UsuarioRepository usuarioRepository,
                       ProjetoUsuarioRepository projetoUsuarioRepository,
                       TarefaService tarefaService) {
        this.votoRepository = votoRepository;
        this.tarefaRepository = tarefaRepository;
        this.sprintRepository = sprintRepository;
        this.usuarioRepository = usuarioRepository;
        this.projetoUsuarioRepository = projetoUsuarioRepository;
        this.tarefaService = tarefaService;
    }

    public SessaoPokerResponseDTO getSessao(Long projetoId, Long sprintId, Long tarefaId, String emailUsuario) {
        Usuario usuario = buscarUsuarioPorEmail(emailUsuario);
        validarMembroProjeto(projetoId, usuario.getId());
        Tarefa tarefa = buscarTarefa(projetoId, sprintId, tarefaId);

        List<Voto> votos = votoRepository.findByTarefaId(tarefaId);
        List<VotoResponseDTO> votosDTO = votos.stream()
                .map(v -> new VotoResponseDTO(v.getId(), v.getUsuario().getId(), v.getUsuario().getNome(), v.getValor()))
                .collect(Collectors.toList());

        Integer media = votos.isEmpty() ? null
                : (int) Math.round(votos.stream().mapToInt(Voto::getValor).average().orElse(0));

        return new SessaoPokerResponseDTO(
                tarefaId,
                votosDTO,
                media,
                tarefa.isEstimativaConsolidada(),
                tarefa.getEstimativa()
        );
    }

    @Transactional
    public VotoResponseDTO votar(Long projetoId, Long sprintId, Long tarefaId, int valor, String emailUsuario) {
        Usuario usuario = buscarUsuarioPorEmail(emailUsuario);
        validarMembroProjeto(projetoId, usuario.getId());
        Tarefa tarefa = buscarTarefa(projetoId, sprintId, tarefaId);

        if (tarefa.isEstimativaConsolidada()) {
            throw new RuntimeException("Estimativa já consolidada. Reinicie a rodada para votar novamente.");
        }

        Optional<Voto> existente = votoRepository.findByTarefaIdAndUsuarioId(tarefaId, usuario.getId());
        Voto voto;
        if (existente.isPresent()) {
            voto = existente.get();
            voto.setValor(valor);
        } else {
            voto = new Voto(tarefaId, usuario, valor);
        }
        voto = votoRepository.save(voto);
        return new VotoResponseDTO(voto.getId(), usuario.getId(), usuario.getNome(), voto.getValor());
    }

    @Transactional
    public TarefaResponseDTO consolidar(Long projetoId, Long sprintId, Long tarefaId, String emailUsuario) {
        Usuario usuario = buscarUsuarioPorEmail(emailUsuario);
        validarPermissaoGerente(projetoId, usuario.getId());
        Tarefa tarefa = buscarTarefa(projetoId, sprintId, tarefaId);

        List<Voto> votos = votoRepository.findByTarefaId(tarefaId);
        if (votos.isEmpty()) {
            throw new RuntimeException("Nenhum voto registrado para consolidar.");
        }

        int media = (int) Math.round(votos.stream().mapToInt(Voto::getValor).average().orElse(0));
        tarefa.setEstimativa(media);
        tarefa.setEstimativaConsolidada(true);
        tarefaRepository.save(tarefa);

        return tarefaService.converterParaDTO(tarefa);
    }

    @Transactional
    public TarefaResponseDTO resetar(Long projetoId, Long sprintId, Long tarefaId, String emailUsuario) {
        Usuario usuario = buscarUsuarioPorEmail(emailUsuario);
        validarPermissaoGerente(projetoId, usuario.getId());
        Tarefa tarefa = buscarTarefa(projetoId, sprintId, tarefaId);

        tarefa.setEstimativaConsolidada(false);
        tarefa.setEstimativa(0);
        tarefaRepository.save(tarefa);
        votoRepository.deleteByTarefaId(tarefaId);

        return tarefaService.converterParaDTO(tarefa);
    }

    private Tarefa buscarTarefa(Long projetoId, Long sprintId, Long tarefaId) {
        var sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint não encontrada."));
        if (sprint.getProjetoId() != projetoId) {
            throw new RuntimeException("Sprint não pertence a esse projeto.");
        }
        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada."));
        if (tarefa.getSprintId() != sprintId) {
            throw new RuntimeException("Tarefa não pertence a essa sprint.");
        }
        return tarefa;
    }

    private void validarMembroProjeto(Long projetoId, Long usuarioId) {
        boolean membro = projetoUsuarioRepository.existsByProjetoIdAndUsuarioId(projetoId, usuarioId);
        if (!membro) {
            throw new RuntimeException("Você não é membro deste projeto.");
        }
    }

    private void validarPermissaoGerente(Long projetoId, Long usuarioId) {
        var pu = projetoUsuarioRepository.findByProjetoIdAndUsuarioId(projetoId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Você não é membro deste projeto."));
        if (!pu.getPapel().getNome().equals(PapelProjeto.GERENTE.name())) {
            throw new RuntimeException("Apenas o gerente pode realizar esta ação.");
        }
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }
}
