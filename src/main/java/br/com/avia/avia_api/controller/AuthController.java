package br.com.avia.avia_api.controller;

import br.com.avia.avia_api.dto.auth.*;
import br.com.avia.avia_api.dto.usuario.UsuarioResponse;
import br.com.avia.avia_api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@RequestBody @Valid CadastroRequestDTO request) {
        return authService.cadastrar(request);
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody @Valid LoginRequestDTO request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        /*
         * Como estamos usando JWT stateless, o logout no backend
         * não precisa fazer nada neste primeiro momento.
         *
         * O frontend deve apenas apagar o token salvo.
         *
         */
    }

    @PostMapping("/recuperacao-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recuperacaoSenha(@RequestBody @Valid RecuperacaoSenhaRequestDTO request) {
        authService.solicitarRecuperacaoSenha(request);
    }

    @PostMapping("/redefinicao-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void redefinicaoSenha(@RequestBody @Valid RedefinicaoSenhaRequestDTO request) {
        authService.redefinirSenha(request);
    }
}