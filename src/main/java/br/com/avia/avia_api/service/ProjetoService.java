package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.projeto.*;
import br.com.avia.avia_api.entity.*;
import br.com.avia.avia_api.enums.CargoProjeto;
import br.com.avia.avia_api.enums.PapelProjeto;
import br.com.avia.avia_api.exception.permissao.PermissaoNegadaException;
import br.com.avia.avia_api.repository.PapelRepository;
import br.com.avia.avia_api.repository.ProjetoRepository;
import br.com.avia.avia_api.repository.ProjetoUsuarioRepository;
import br.com.avia.avia_api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProjetoUsuarioRepository projetoUsuarioRepository;
    private final PapelRepository papelRepository;
    private final NotificacaoService notificacaoService;

    public ProjetoService(ProjetoRepository projetoRepository,
                          UsuarioRepository usuarioRepository,
                          ProjetoUsuarioRepository projetoUsuarioRepository,
                          PapelRepository papelRepository,
                          NotificacaoService notificacaoService) {
        this.projetoRepository = projetoRepository;
        this.usuarioRepository = usuarioRepository;
        this.projetoUsuarioRepository = projetoUsuarioRepository;
        this.papelRepository = papelRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional
    public ProjetoResponseDTO criarProjeto(CriarProjetoRequestDTO dto, String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);

        Projeto projeto = new Projeto();
        projeto.setNome(dto.nome());
        projeto.setTipoEstimativa(dto.tipoEstimativa());
        Projeto projetoSalvo = projetoRepository.save(projeto);

        String papelGerente = PapelProjeto.GERENTE.name();
        Papel papel = papelRepository.findByNome(papelGerente)
                .orElseThrow(() -> new RuntimeException("Papel inexistente."));

        ProjetoUsuario vinculoGerente = new ProjetoUsuario(CargoProjeto.GERENTE, usuarioLogado, projetoSalvo, papel);
        projetoUsuarioRepository.save(vinculoGerente);

        return converterParaDTO(projetoSalvo);
    }

    public ProjetoResponseDTO buscarPorId(Long projetoId, String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);

        List<Projeto> projetosDoUsuario = projetoUsuarioRepository.findProjetosByMembroId(usuarioLogado.getId());

        boolean temAcesso = projetosDoUsuario.stream()
                .anyMatch(p -> p.getId().equals(projetoId));

        if (!temAcesso) {
            throw new RuntimeException("Projeto não encontrado ou você não tem permissão para visualizá-lo.");
        }

        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado."));

        return converterParaDTO(projeto);
    }

    public List<ProjetoResponseDTO> listarProjetosDoUsuario(String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);

        List<Projeto> projetos = projetoUsuarioRepository.findProjetosByMembroId(usuarioLogado.getId());

        return projetos.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public List<MembroResponseDTO> listarMembrosDoProjeto(long idProjeto, String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);

        List<Projeto> projetosDoUsuario = projetoUsuarioRepository.findProjetosByMembroId(usuarioLogado.getId());
        boolean temAcesso = projetosDoUsuario.stream().anyMatch(p -> p.getId() == idProjeto);

        if (!temAcesso) {
            throw new RuntimeException("Você não tem permissão para visualizar os membros deste projeto.");
        }

        List<ProjetoUsuario> membros = projetoUsuarioRepository.findMembrosByProjetoId(idProjeto);

        return membros.stream()
                .map(this::converterMembroParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MembroResponseDTO atualizarMembroDoProjeto(Long projetoId, Long usuarioAlvoId,
                                                       AtualizarMembroRequestDTO dto, String emailSolicitante) {
        Usuario solicitante = buscarUsuarioPorEmail(emailSolicitante);
        verificarPermissao(projetoId, solicitante.getId(), "ADICIONARMEMBROS");

        ProjetoUsuario pu = projetoUsuarioRepository.findByProjetoIdAndUsuarioId(projetoId, usuarioAlvoId)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado no projeto."));

        if (dto.cargo() != null) {
            if (!dto.cargo().isAtribuivel()) {
                throw new RuntimeException("Este cargo não pode ser atribuído manualmente.");
            }
            pu.setCargo(dto.cargo());
        }

        if (dto.papel() != null) {
            if (!dto.papel().isAtribuivel()) {
                throw new RuntimeException("Este papel não pode ser atribuído manualmente.");
            }
            Papel novoPapel = papelRepository.findByNome(dto.papel().name())
                    .orElseThrow(() -> new RuntimeException("Papel inexistente."));
            pu.setPapel(novoPapel);
        }

        projetoUsuarioRepository.save(pu);
        return converterMembroParaDTO(pu);
    }

    @Transactional
    public void deletarProjeto(Long projetoId, String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);

        validarPermissaoGerente(projetoId, usuarioLogado.getId());

        projetoUsuarioRepository.deleteByProjetoId(projetoId);

        projetoRepository.deleteById(projetoId);
    }

    @Transactional
    public ProjetoResponseDTO editarInformacoesDoProjeto(Long projetoId, AtualizarProjetoRequestDTO dto,
                                                         String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);

        validarPermissaoGerente(projetoId, usuarioLogado.getId());

        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado."));

        if (dto.nome() != null && !dto.nome().isBlank()) {
            projeto.setNome(dto.nome());
        }

        Projeto projetoAtualizado = projetoRepository.save(projeto);
        return converterParaDTO(projetoAtualizado);
    }

    @Transactional
    public void adicionarMembroAoProjeto(Long projetoId, AdicionarMembroProjetoRequestDTO dto, String emailMembro) {
        Usuario membro = buscarUsuarioPorEmail(emailMembro);

        verificarPermissao(projetoId, membro.getId(), "ADICIONARMEMBROS");

        if (!dto.cargo().isAtribuivel()) {
            throw new RuntimeException("Este cargo não pode ser atribuído manualmente.");
        }
        if (!dto.papel().isAtribuivel()) {
            throw new RuntimeException("Este papel não pode ser atribuído manualmente.");
        }

        Usuario novoMembro = buscarUsuarioPorEmail(dto.emailUsuario());

        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado."));

        boolean jaEhMembro = projetoUsuarioRepository.existsByProjetoIdAndUsuarioId(projetoId, novoMembro.getId());
        if (jaEhMembro) {
            throw new RuntimeException("Este usuário já é membro do projeto.");
        }

        String nomePapel = dto.papel().name();
        Papel papel = papelRepository.findByNome(nomePapel)
                .orElseThrow(() -> new RuntimeException("Papel inexistente."));

        ProjetoUsuario pu = new ProjetoUsuario(dto.cargo(), novoMembro, projeto, papel);
        projetoUsuarioRepository.save(pu);

        notificacaoService.notificarUsuarioAdicionadoAoProjeto(pu);
    }

    @Transactional
    public String removerMembroDoProjeto(Long projetoId, RemoverMembroRequestDTO dto, String emailMembro) {
        //Membro que solicita a operação de exclusão
        Usuario membroSolicitante = buscarUsuarioPorEmail(emailMembro);

        verificarPermissao(projetoId, membroSolicitante.getId(), "REMOVERMEMBROS");

        //Membro a ser removido
        Usuario membro = buscarUsuarioPorEmail(dto.emailMembro());

        projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado."));

        boolean ehMembro = projetoUsuarioRepository.existsByProjetoIdAndUsuarioId(projetoId, membro.getId());
        if (!ehMembro) {
            throw new RuntimeException("Este usuário não é membro do projeto.");
        }

        return membro.getNome();
    }

    private void validarPermissaoGerente(Long projetoId, long usuarioId) {
        Papel papelMembro = projetoUsuarioRepository.findPapelByProjetoIdAndUsuarioId(projetoId, usuarioId);
        boolean ehGerente = papelMembro.getNome().equalsIgnoreCase("GERENTE");

        if(!ehGerente)
            throw new RuntimeException("Somente o gerente pode fazer isso.");
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado."));
    }

    private ProjetoResponseDTO converterParaDTO(Projeto projeto) {
        return new ProjetoResponseDTO(
                projeto.getId(),
                projeto.getNome(),
                projeto.getTipoEstimativa()
        );
    }

    private MembroResponseDTO converterMembroParaDTO(ProjetoUsuario pu) {
        PapelProjeto papelEnum = null;
        if (pu.getPapel() != null) {
            try { papelEnum = PapelProjeto.deString(pu.getPapel().getNome()); } catch (Exception ignored) {}
        }
        return new MembroResponseDTO(
                pu.getUsuario().getId(),
                pu.getUsuario().getNome(),
                pu.getUsuario().getEmail(),
                pu.getCargo(),
                papelEnum
        );
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