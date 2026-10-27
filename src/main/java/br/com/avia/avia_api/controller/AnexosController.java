package br.com.avia.avia_api.controller;

import br.com.avia.avia_api.dto.tarefa.AnexoResponseDTO;
import br.com.avia.avia_api.dto.tarefa.BaixarAnexoDTO;
import br.com.avia.avia_api.service.AnexoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
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
        return ResponseEntity.ok(anexoService.listarAnexos(projetoId, tarefaId, jwt.getSubject()));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnexoResponseDTO> adicionarAnexo(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @RequestParam("arquivo") MultipartFile arquivo,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AnexoResponseDTO response = anexoService.adicionarAnexo(projetoId, tarefaId, arquivo, jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{anexoId}")
    public ResponseEntity<Void> removerAnexo(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @PathVariable Long anexoId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        anexoService.removerAnexo(projetoId, tarefaId, anexoId, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{anexoId}/download")
    public ResponseEntity<Resource> baixarAnexo(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @PathVariable Long anexoId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String emailUsuario = jwt.getSubject();
        BaixarAnexoDTO dtoBaixar = anexoService.baixarAnexo(projetoId, tarefaId, anexoId, emailUsuario);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dtoBaixar.tipo().mime()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(dtoBaixar.nome(), StandardCharsets.UTF_8)
                                .build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(dtoBaixar.recurso());
    }
}
