package br.com.avia.avia_api.controller;

import br.com.avia.avia_api.dto.usuario.AtualizarUsuarioRequestDTO;
import br.com.avia.avia_api.dto.usuario.UsuarioResponse;
import br.com.avia.avia_api.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/eu")
    public UsuarioResponse buscarMeuPerfil(Authentication authentication) {
        String email = authentication.getName();

        return usuarioService.buscarUsuarioAutenticado(email);
    }

    @PatchMapping("/eu")
    public UsuarioResponse atualizarMeuPerfil(
            Authentication authentication,
            @RequestBody AtualizarUsuarioRequestDTO request) {
        String email = authentication.getName();

        return usuarioService.atualizarUsuarioAutenticado(email, request);
    }
}