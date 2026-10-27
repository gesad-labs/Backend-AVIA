package br.com.avia.avia_api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Informa o ambiente (APP_ENV: dev, homolog, prod) e a versão (arquivo VERSION)
 * da aplicação em execução. Público, usado pelo selo do front e pelo deploy.
 */
@RestController
public class VersionController {

    private final String env;
    private final String version;

    public VersionController(@Value("${app.env:dev}") String env,
                             @Value("${app.version:0.0.0}") String version) {
        this.env = env;
        this.version = version;
    }

    @GetMapping("/version")
    public Map<String, String> version() {
        return Map.of("env", env, "version", version, "label", env + "-" + version);
    }
}
