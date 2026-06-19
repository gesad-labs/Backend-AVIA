package br.com.avia.avia_api.controller;

import br.com.avia.avia_api.dto.projeto.*;
import br.com.avia.avia_api.dto.sprint.*;
import br.com.avia.avia_api.service.ProjetoService;
import br.com.avia.avia_api.service.SprintService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/projetos")
public class ProjetoController {

    private final ProjetoService projetoService;
    private final SprintService sprintService;

    public ProjetoController(ProjetoService projetoService, SprintService sprintService) {
        this.projetoService = projetoService;
        this.sprintService = sprintService;

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
    public ResponseEntity<List<MembroResponseDTO>> verMembros(@PathVariable Long id,
                                                            @AuthenticationPrincipal Jwt jwt){
        String emailUsuario = jwt.getSubject();
        List<MembroResponseDTO> membros = projetoService.listarMembrosDoProjeto(id, emailUsuario);
        return ResponseEntity.ok(membros);
    }

    @PostMapping("/{id}/membros")
    public ResponseEntity<Map<String, String>> adicionarNovoMembro(@PathVariable Long id,
                                                                   @RequestBody @Valid AdicionarMembroProjetoRequestDTO dto,
                                                                   @AuthenticationPrincipal Jwt jwt){
        String emailGerente = jwt.getSubject();
        projetoService.adicionarMembroAoProjeto(id, dto,
                emailGerente);
        Map<String, String> resposta = Map.of("mensagem", "Membro adicionado com sucesso");

        return new ResponseEntity<>(resposta, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}/membros")
    public ResponseEntity<Map<String, String>> removerMembro(@PathVariable Long id,
                                                               @RequestBody @Valid RemoverMembroRequestDTO dto,
                                                               @AuthenticationPrincipal Jwt jwt){

        String emailGerente = jwt.getSubject();
        String nomeMembro = projetoService.removerMembroDoProjeto(id, dto, emailGerente);
        Map<String, String> resposta = Map.of("mensagem", nomeMembro + " foi removido(a)");

        return new ResponseEntity<>(resposta, HttpStatus.CREATED);
    }

    @PostMapping("/{projeto_id}/sprints")
    public ResponseEntity<SprintResponseDTO> criarSprint(
            @PathVariable("projeto_id") Long projetoId,
            @RequestBody @Valid CriarSprintRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String emailUsuario = jwt.getSubject();

        SprintResponseDTO sprint = sprintService.criarSprint(
                projetoId,
                dto,
                emailUsuario
        );

        return new ResponseEntity<>(sprint, HttpStatus.CREATED);
    }

    @GetMapping("/{projeto_id}/sprints")
    public ResponseEntity<List<SprintResponseDTO>> listarSprints(
            @PathVariable("projeto_id") Long projetoId,
            @AuthenticationPrincipal Jwt jwt
    ) {

        String emailUsuario = jwt.getSubject();

        List<SprintResponseDTO> sprints = sprintService
                .listarSprintsPorProjeto(projetoId, emailUsuario);

        return ResponseEntity.ok(sprints);
    }

}