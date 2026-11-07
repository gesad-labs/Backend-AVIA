package br.com.avia.avia_api.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Component
public class SegurancaExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final AuthenticationEntryPoint entryPointPadrao = new BearerTokenAuthenticationEntryPoint();
    private final AccessDeniedHandler accessDeniedPadrao = new BearerTokenAccessDeniedHandler();
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException, ServletException {
        entryPointPadrao.commence(request, response, ex);

        // Token enviado, mas inválido ou expirado
        String mensagem = ex instanceof OAuth2AuthenticationException
                ? "Sessão expirada ou inválida. Faça login novamente."
                : "É preciso fazer login para acessar este recurso.";
        escreverCorpo(response, mensagem);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException, ServletException {
        accessDeniedPadrao.handle(request, response, ex);
        escreverCorpo(response, "Você não tem permissão para executar essa ação.");
    }

    private void escreverCorpo(HttpServletResponse response, String mensagem) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(jsonMapper.writeValueAsString(new ErroResponseDTO(mensagem)));
    }
}
