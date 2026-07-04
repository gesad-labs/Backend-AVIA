package br.com.avia.avia_api.controller;

import br.com.avia.avia_api.dto.tarefa.AnexoResponseDTO;
import br.com.avia.avia_api.dto.tarefa.CriarAnexoRequestDTO;
import br.com.avia.avia_api.service.AnexoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projetos/{projetoId}/tarefas/{tarefaId}/anexos")
@Tag(name = "Anexos", description = "Gerenciamento de anexos de tarefas")
public class AnexosController {

    private final AnexoService anexoService;

    public AnexosController(AnexoService anexoService) {
        this.anexoService = anexoService;
    }

    @GetMapping
    public ResponseEntity<List<AnexoResponseDTO>> listarAnexos(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(anexoService.listarAnexos(projetoId, tarefaId));
    }

    @PostMapping
    public ResponseEntity<AnexoResponseDTO> adicionarAnexo(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @RequestBody @Valid CriarAnexoRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AnexoResponseDTO response = anexoService.adicionarAnexo(projetoId, tarefaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{anexoId}")
    public ResponseEntity<Void> removerAnexo(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @PathVariable Integer anexoId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        anexoService.removerAnexo(projetoId, tarefaId, anexoId);
        return ResponseEntity.noContent().build();
    }
}
