package br.com.avia.avia_api.service;

import br.com.avia.avia_api.armazenamento.ArmazenamentoLocal;
import br.com.avia.avia_api.dto.tarefa.AnexoResponseDTO;
import br.com.avia.avia_api.dto.tarefa.BaixarAnexoDTO;
import br.com.avia.avia_api.entity.Anexo;
import br.com.avia.avia_api.entity.ProjetoUsuario;
import br.com.avia.avia_api.entity.Tarefa;
import br.com.avia.avia_api.enums.CargoProjeto;
import br.com.avia.avia_api.enums.TipoAnexo;
import br.com.avia.avia_api.exception.auth.UsuarioNaoEncontradoException;
import br.com.avia.avia_api.exception.permissao.PermissaoNegadaException;
import br.com.avia.avia_api.exception.tarefa.TarefaNaoEncontradaException;
import br.com.avia.avia_api.repository.AnexoRepository;
import br.com.avia.avia_api.repository.ProjetoUsuarioRepository;
import br.com.avia.avia_api.repository.TarefaRepository;
import br.com.avia.avia_api.validation.anexo.ArquivoValidator;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.List;
import br.com.avia.avia_api.exception.anexo.AnexoNaoEncontradoException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Service
public class AnexoService {

    private final AnexoRepository anexoRepository;
    private final TarefaRepository tarefaRepository;
    private final ProjetoUsuarioRepository projetoUsuarioRepository;
    private final ArquivoValidator arquivoValidator;
    private final ArmazenamentoLocal armazenamentoLocal;

    public AnexoService(
            AnexoRepository anexoRepository,
            TarefaRepository tarefaRepository,
            ProjetoUsuarioRepository projetoUsuarioRepository,
            ArquivoValidator arquivoValidator,
            ArmazenamentoLocal armazenamentoLocal) {

        this.anexoRepository = anexoRepository;
        this.tarefaRepository = tarefaRepository;
        this.projetoUsuarioRepository = projetoUsuarioRepository;
        this.arquivoValidator = arquivoValidator;
        this.armazenamentoLocal = armazenamentoLocal;
    }

    @Transactional(readOnly = true)
    public List<AnexoResponseDTO> listarAnexos(Long projetoId, Long tarefaId, String emailUsuario) {
        ProjetoUsuario projetoUsuario = buscarMembroProjeto(projetoId, emailUsuario);
        buscarTarefaDoProjeto(projetoId, tarefaId);
        return anexoRepository.findByTarefaIdOrderByCriadoEmAsc(tarefaId)
                .stream().map(anexo -> converterParaDTO(anexo, projetoUsuario)).toList();
    }

    @Transactional
    public AnexoResponseDTO adicionarAnexo(Long projetoId, Long tarefaId, MultipartFile arquivo, String emailUsuario) {
        ProjetoUsuario projetoUsuario = buscarMembroProjeto(projetoId, emailUsuario);
        Tarefa tarefa = buscarTarefaDoProjeto(projetoId, tarefaId);
        // por enquanto ainda nao tem MAX anexos por tarefa
        TipoAnexo tipo = arquivoValidator.validar(arquivo);
        String chave = UUID.randomUUID() + "." + tipo.extensao();

        // faço isso para caso dê erro com o banco, o salvamento no disco seja cancelado.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED){
                    armazenamentoLocal.remover(chave);
                }
            }
        });

        try(InputStream in = arquivo.getInputStream()){
            armazenamentoLocal.salvar(chave, in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        Anexo anexo = anexoRepository.save(new Anexo(tarefa.getId(), arquivo.getSize(), tipo, chave,
                LocalDateTime.now(), projetoUsuario.getUsuario(),
                nomeParaExibicao(arquivo, tipo)));

        return converterParaDTO(anexo, projetoUsuario);
    }

    @Transactional(readOnly = true)
    public BaixarAnexoDTO baixarAnexo(Long projetoId, Long tarefaId, Long anexoid, String emailUsuario){
        buscarMembroProjeto(projetoId, emailUsuario);
        Tarefa tarefa = buscarTarefaDoProjeto(projetoId, tarefaId);
        Anexo anexo = anexoRepository.findByIdAndTarefaId(anexoid, tarefaId)
                .orElseThrow(AnexoNaoEncontradoException::new);
        Resource recurso = armazenamentoLocal.carregar(anexo.getChaveArmazenamento());
        if (!recurso.exists()){
            throw new AnexoNaoEncontradoException();
        }

        return new BaixarAnexoDTO(recurso, anexo.getNomeArquivo(), anexo.getTipoAnexo());
    }

    @Transactional
    public void removerAnexo(Long projetoId, Long tarefaId, Long anexoId, String emailUsuario) {
        ProjetoUsuario projetoUsuario = buscarMembroProjeto(projetoId, emailUsuario);
        buscarTarefaDoProjeto(projetoId, tarefaId);
        Anexo anexo = anexoRepository.findByIdAndTarefaId(anexoId, tarefaId)
                .orElseThrow(AnexoNaoEncontradoException::new);

        if(!podeRemover(anexo, projetoUsuario)){
                throw new PermissaoNegadaException();
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                // se der erro no banco e nao conseguir deletar, nao deleta no disco
                if (status == STATUS_COMMITTED){
                    armazenamentoLocal.remover(anexo.getChaveArmazenamento());
                }
            }
        });
        anexoRepository.delete(anexo);
    }

    private Tarefa buscarTarefaDoProjeto(Long projetoId, Long tarefaId) {
        return tarefaRepository.findByIdAndProjetoId(tarefaId, projetoId)
                .orElseThrow(TarefaNaoEncontradaException::new);
    }

    private ProjetoUsuario buscarMembroProjeto(Long projetoId, String emailMembro){
        return projetoUsuarioRepository.findByProjetoIdAndUsuarioEmail(projetoId, emailMembro)
                .orElseThrow(UsuarioNaoEncontradoException::new);
        // nao passar informações de dentro pra quem é de fora
    }

    // limpa o MultiPartFile.getOriginalFilename para ser apenas o nomeDoArquivo.extencao
    private String nomeParaExibicao(MultipartFile arquivo, TipoAnexo tipo) {
        String nome = StringUtils.getFilename(StringUtils.cleanPath(arquivo.getOriginalFilename()));
        if (nome != null) {
            nome = nome.replaceAll("\\p{Cntrl}", "").strip();
        }
        if (nome == null || nome.isEmpty()) {
            return "arquivo." + tipo.extensao();
        }
        return nome.length() > 255 ? nome.substring(0, 255) : nome;
    }

    private boolean podeRemover(Anexo anexo, ProjetoUsuario solicitante){
        boolean ehGerente = solicitante.getCargo() == CargoProjeto.GERENTE;
        boolean ehAutor = anexo.getEnviadoPor().getId()
                .equals(solicitante.getUsuario().getId());
        return ehGerente || ehAutor;
    }

    private AnexoResponseDTO converterParaDTO(Anexo anexo, ProjetoUsuario projetoUsuario) {
        return new AnexoResponseDTO(
                anexo.getId(),
                anexo.getTarefaId(),
                anexo.getTipoAnexo(),
                anexo.getTamanhoBytes(),
                anexo.getCriadoEm(),
                anexo.getNomeArquivo(),
                // listar quem enviou o anexo, nao quem pediu pra ver o anexo
                anexo.getEnviadoPor().getNome(),
                podeRemover(anexo, projetoUsuario)
        );
    }
}