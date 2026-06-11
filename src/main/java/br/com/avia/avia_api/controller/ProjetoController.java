package br.com.avia.avia_api.controller;

import br.com.avia.avia_api.dto.projeto.*;
import br.com.avia.avia_api.dto.usuario.UsuarioResponse;
import br.com.avia.avia_api.service.ProjetoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projetos")
public class ProjetoController {

    private final ProjetoService projetoService;

    public ProjetoController(ProjetoService projetoService) {
        this.projetoService = projetoService;
    }

    @PostMapping
    public ResponseEntity<ProjetoResponseDTO> criar(@RequestBody @Valid CriarProjetoRequestDTO dto,
                                                    @AuthenticationPrincipal Jwt jwt) {
        String emailUsuario = jwt.getSubject();
        ProjetoResponseDTO novoProjeto = projetoService.criarProjeto(dto, emailUsuario);
        return new ResponseEntity<>(novoProjeto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjetoResponseDTO> buscarPorId(@PathVariable Long id,
                                                          @AuthenticationPrincipal Jwt jwt) {
        String emailUsuario = jwt.getSubject();
        ProjetoResponseDTO projeto = projetoService.buscarPorId(id, emailUsuario);
        return ResponseEntity.ok(projeto);
    }

    @GetMapping
    public ResponseEntity<List<ProjetoResponseDTO>> listarMeusProjetos(@AuthenticationPrincipal Jwt jwt) {
        String emailUsuario = jwt.getSubject();
        List<ProjetoResponseDTO> projetos = projetoService.listarProjetosDoUsuario(emailUsuario);
        return ResponseEntity.ok(projetos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id,
                                        @AuthenticationPrincipal Jwt jwt) {
        String emailUsuario = jwt.getSubject();
        projetoService.deletarProjeto(id, emailUsuario);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjetoResponseDTO> editar(@PathVariable Long id,
                                                     @RequestBody @Valid AtualizarProjetoRequestDTO dto,
                                                     @AuthenticationPrincipal Jwt jwt) {
        String emailUsuario = jwt.getSubject();
        ProjetoResponseDTO projetoAtualizado = projetoService.editarInformacoesDoProjeto(id, dto, emailUsuario);
        return ResponseEntity.ok(projetoAtualizado);
    }

    @GetMapping("/{id}/membros")
    public ResponseEntity<List<UsuarioResponse>> verMembros(@PathVariable Long id,
                                                            @AuthenticationPrincipal Jwt jwt){
        String emailUsuario = jwt.getSubject();
        List<UsuarioResponse> membros = projetoService.listarMembrosDoProjeto(id, emailUsuario);
        return ResponseEntity.ok(membros);
    }

    @PostMapping("/{id}/membros")
    public ResponseEntity<List<UsuarioResponse>> adicionarNovoMembro(@PathVariable Long id,
                                    @RequestBody @Valid AdicionarMembroProjetoRequestDTO dto,
                                    @AuthenticationPrincipal Jwt jwt){
        String emailGerente = jwt.getSubject();
        List<UsuarioResponse> membros = projetoService.adicionarMembroAoProjeto(id, dto,
                emailGerente);
        return ResponseEntity.ok(membros);
    }

    @DeleteMapping("/{id}/membros")
    public ResponseEntity<List<UsuarioResponse>> removerMembro(@PathVariable Long id,
                                                               @RequestBody @Valid RemoverMembroRequestDTO dto,
                                                               @AuthenticationPrincipal Jwt jwt){

        String emailGerente = jwt.getSubject();
        List<UsuarioResponse> membros = projetoService.removerMembroDoProjeto(id, dto, emailGerente);
        return ResponseEntity.ok(membros);
    }
}