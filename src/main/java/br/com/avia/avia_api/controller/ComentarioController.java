package br.com.avia.avia_api.controller;

import br.com.avia.avia_api.dto.comentario.ComentarioResponseDTO;
import br.com.avia.avia_api.dto.comentario.CriarComentarioRequestDTO;
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
@RequestMapping("/projetos/{projetoId}/tarefas/{tarefaId}/comentarios")
@Tag(name = "Comentários", description = "Comentários de tarefas")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping
    public ResponseEntity<List<ComentarioResponseDTO>> listar(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(comentarioService.listarComentarios(projetoId, tarefaId));
    }

    @PostMapping
    public ResponseEntity<ComentarioResponseDTO> adicionar(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @RequestBody @Valid CriarComentarioRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String email = jwt.getSubject();
        ComentarioResponseDTO resp = comentarioService.adicionarComentario(projetoId, tarefaId, dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
}
