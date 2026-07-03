package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.tarefa.AnexoResponseDTO;
import br.com.avia.avia_api.dto.tarefa.CriarAnexoRequestDTO;
import br.com.avia.avia_api.entity.Anexo;
import br.com.avia.avia_api.entity.Tarefa;
import br.com.avia.avia_api.exception.tarefa.TarefaNaoEncontradaException;
import br.com.avia.avia_api.exception.tarefa.TarefaNaoPertenceAoProjetoException;
import br.com.avia.avia_api.repository.AnexoRepository;
import br.com.avia.avia_api.repository.TarefaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import br.com.avia.avia_api.exception.anexo.AnexoNaoEncontradoException;
import br.com.avia.avia_api.exception.anexo.AnexoNaoPertenceATarefaException;

import java.util.Objects;

@Service
public class AnexoService {

    private final AnexoRepository anexoRepository;
    private final TarefaRepository tarefaRepository;

    public AnexoService(
            AnexoRepository anexoRepository,
            TarefaRepository tarefaRepository
    ) {
        this.anexoRepository = anexoRepository;
        this.tarefaRepository = tarefaRepository;
    }

    public List<AnexoResponseDTO> listarAnexos(Long projetoId, Long tarefaId) {
        buscarTarefaDoProjeto(projetoId, tarefaId);
        return anexoRepository.findByTarefaId(tarefaId)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional
    public AnexoResponseDTO adicionarAnexo(Long projetoId, Long tarefaId, CriarAnexoRequestDTO dto) {
        buscarTarefaDoProjeto(projetoId, tarefaId);

        Anexo anexo = new Anexo(
                tarefaId,
                dto.nome(),
                dto.tipoArquivo(),
                dto.url()
        );

        Anexo salvo = anexoRepository.save(anexo);

        return converterParaDTO(salvo);
    }

    @Transactional
    public void removerAnexo(Long projetoId, Long tarefaId, Integer anexoId) {
        buscarTarefaDoProjeto(projetoId, tarefaId);

        Anexo anexo = anexoRepository.findById(anexoId)
                .orElseThrow(AnexoNaoEncontradoException::new);

        if (!Objects.equals(anexo.getTarefaId(), tarefaId)) {
            throw new AnexoNaoPertenceATarefaException();
        }

        anexoRepository.delete(anexo);
    }

    private Tarefa buscarTarefaDoProjeto(Long projetoId, Long tarefaId) {
        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(TarefaNaoEncontradaException::new);

        if (!Objects.equals(tarefa.getProjetoId(), projetoId)) {
            throw new TarefaNaoPertenceAoProjetoException();
        }

        return tarefa;
    }

    private AnexoResponseDTO converterParaDTO(Anexo anexo) {
        return new AnexoResponseDTO(
                anexo.getId(),
                anexo.getTarefaId(),
                anexo.getNome(),
                anexo.getTipoArquivo(),
                anexo.getUrl()
        );
    }
}