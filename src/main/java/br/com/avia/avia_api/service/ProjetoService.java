package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.projeto.*;
import br.com.avia.avia_api.entity.Papel;
import br.com.avia.avia_api.entity.Projeto;
import br.com.avia.avia_api.entity.ProjetoUsuario;
import br.com.avia.avia_api.entity.Usuario;
import br.com.avia.avia_api.enums.CargoProjeto;
import br.com.avia.avia_api.enums.PapelProjeto;
import br.com.avia.avia_api.repository.PapelRepository;
import br.com.avia.avia_api.repository.ProjetoRepository;
import br.com.avia.avia_api.repository.ProjetoUsuarioRepository;
import br.com.avia.avia_api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProjetoUsuarioRepository projetoUsuarioRepository;
    private final PapelRepository papelRepository;

    public ProjetoService(ProjetoRepository projetoRepository,
                          UsuarioRepository usuarioRepository,
                          ProjetoUsuarioRepository projetoUsuarioRepository, PapelRepository papelRepository) {
        this.projetoRepository = projetoRepository;
        this.usuarioRepository = usuarioRepository;
        this.projetoUsuarioRepository = projetoUsuarioRepository;
        this.papelRepository = papelRepository;
    }

    @Transactional
    public ProjetoResponseDTO criarProjeto(CriarProjetoRequestDTO dto, String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);

        Projeto projeto = new Projeto();
        projeto.setNome(dto.nome());
        projeto.setTipoEstimativa(dto.tipoEstimativa());
        Projeto projetoSalvo = projetoRepository.save(projeto);

        String papelGerente = PapelProjeto.GERENTE.name();
        Papel papel = papelRepository.findByNome(papelGerente).
                orElseThrow(() -> new RuntimeException("Papel inexistente."));

        ProjetoUsuario vinculoGerente = new ProjetoUsuario(CargoProjeto.GERENTE, usuarioLogado, projetoSalvo, papel);
        projetoUsuarioRepository.save(vinculoGerente);

        return converterParaDTO(projetoSalvo);
    }

    public ProjetoResponseDTO buscarPorId(Long projetoId, String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);

        List<Projeto> projetosDoUsuario = projetoUsuarioRepository.findProjetosByMembroId(usuarioLogado.getId());

        boolean temAcesso = projetosDoUsuario.stream()
                .anyMatch(p -> p.getId() == projetoId);

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
                .map(membro -> new MembroResponseDTO(
                        membro.getUsuario().getId(),
                        membro.getUsuario().getNome(),
                        membro.getUsuario().getEmail(),
                        membro.getCargo()
                ))
                .collect(Collectors.toList());
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

    public void adicionarMembroAoProjeto(Long id, AdicionarMembroProjetoRequestDTO dto,
                                         String emailGerente){
        if (!dto.cargo().isAtribuivel())
            throw new RuntimeException("Este cargo não pode ser atribuído manualmente.");

        if (!dto.papel().isAtribuivel())
            throw new RuntimeException("Este papel não pode ser atribuído manualmente.");

        Usuario gerente = buscarUsuarioPorEmail(emailGerente);

        validarPermissaoGerente(id, gerente.getId());

        Usuario novoMembro = buscarUsuarioPorEmail(dto.emailUsuario());

        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado."));

        boolean jaEhMembro = projetoUsuarioRepository.existsByProjetoIdAndUsuarioId(id, novoMembro.getId());
        if (jaEhMembro)
            throw new RuntimeException("Este usuário já é membro do projeto.");

        String nomePapel = dto.papel().name();

        Papel papel = papelRepository.findByNome(nomePapel).
                orElseThrow(() -> new RuntimeException("Papel inexistente."));

        ProjetoUsuario pu = new ProjetoUsuario(dto.cargo(), novoMembro, projeto, papel);
        projetoUsuarioRepository.save(pu);
    }

    public String removerMembroDoProjeto(Long id, RemoverMembroRequestDTO dto,
                                                        String emailGerente){

        Usuario gerente = buscarUsuarioPorEmail(emailGerente);

        validarPermissaoGerente(id, gerente.getId());

        Usuario membro = buscarUsuarioPorEmail(dto.emailMembro());

        projetoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado."));

        boolean ehMembro = projetoUsuarioRepository.existsByProjetoIdAndUsuarioId(id, membro.getId());
        if (!ehMembro)
            throw new RuntimeException("Este usuário não é membro do projeto.");

        projetoUsuarioRepository.deleteByUsuarioId(membro.getId());

        return membro.getNome();
    }

    private void validarPermissaoGerente(Long projetoId, Long usuarioId) {

        List<Projeto> projetosDoUsuario = projetoUsuarioRepository.findProjetosByMembroId(usuarioId);
        boolean ehMembro = projetosDoUsuario.stream().anyMatch(p -> p.getId() == projetoId);

        if (!ehMembro) {
            throw new RuntimeException("Acesso negado ou você não é gerente deste projeto.");
        }

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
}
