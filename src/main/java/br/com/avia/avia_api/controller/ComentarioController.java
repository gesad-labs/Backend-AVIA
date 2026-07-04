package br.com.avia.avia_api.controller;

import br.com.avia.avia_api.dto.comentario.ComentarioResponseDTO;
import br.com.avia.avia_api.dto.comentario.CriarComentarioRequestDTO;
import br.com.avia.avia_api.dto.comentario.EditarComentarioRequestDTO;
import br.com.avia.avia_api.service.ComentarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projetos")
@Tag(name = "Comentário", description = "Gerenciamento de comentários")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @PostMapping("/{projeto_id}/tarefas/{tarefa_id}/comentarios")
    public ResponseEntity<ComentarioResponseDTO> criarComentario(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("tarefa_id") Long tarefaId,
            @RequestBody @Valid CriarComentarioRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String emailUsuario = jwt.getSubject();
        ComentarioResponseDTO comentario = comentarioService.criarComentario(tarefaId, dto, emailUsuario);
        return new ResponseEntity<>(comentario, HttpStatus.CREATED);
    }

    @GetMapping("/{projeto_id}/tarefas/{tarefa_id}/comentarios")
    public ResponseEntity<List<ComentarioResponseDTO>> listarComentarios(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("tarefa_id") Long tarefaId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<ComentarioResponseDTO> comentarios = comentarioService.listarComentarios(tarefaId);
        return ResponseEntity.ok(comentarios);
    }

    @PatchMapping("/{projeto_id}/tarefas/{tarefa_id}/comentarios/{comentario_id}/flag")
    public ResponseEntity<ComentarioResponseDTO> alterarFlag(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("tarefa_id") Long tarefaId,
            @PathVariable("comentario_id") Long comentarioId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ComentarioResponseDTO comentario = comentarioService.alterarFlag(comentarioId);
        return ResponseEntity.ok(comentario);
    }

    @DeleteMapping("/{projeto_id}/tarefas/{tarefa_id}/comentarios/{comentario_id}")
    public ResponseEntity<Void> deletarComentario(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("tarefa_id") Long tarefaId,
            @PathVariable("comentario_id") Long comentarioId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        comentarioService.deletarComentario(tarefaId, comentarioId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{projeto_id}/tarefas/{tarefa_id}/comentarios/{comentario_id}")
    public ResponseEntity<ComentarioResponseDTO> editarComentario(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("tarefa_id") Long tarefaId,
            @PathVariable("comentario_id") Long comentarioId,
            @RequestBody @Valid EditarComentarioRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ComentarioResponseDTO comentario = comentarioService.editarComentario(tarefaId, comentarioId, dto.descricao());
        return ResponseEntity.ok(comentario);
    }
}
