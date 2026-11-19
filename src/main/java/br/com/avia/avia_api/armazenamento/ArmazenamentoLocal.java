package br.com.avia.avia_api.armazenamento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ArmazenamentoLocal implements ArmazenamentoArquivos{

    private static final Logger log = LoggerFactory.getLogger(ArmazenamentoLocal.class);
    private final Path base;

    // se der pau o problema e aq
    private ArmazenamentoLocal(@Value("${avia.anexos.diretorio}")  String diretorio) throws IOException {
        this.base = Path.of(diretorio).toAbsolutePath().normalize();
        Files.createDirectories(base);
    }

    @Override
    public void salvar(String chave, InputStream conteudo) throws IOException {
        Files.copy(conteudo, resolver(chave)); // sem REPLACE_EXISTING: falha se a chave ja existir
    }

    @Override
    public Resource carregar(String chave) {
        return new FileSystemResource(resolver(chave));
    }

    @Override
    public void remover(String chave) {
        try {
            Files.deleteIfExists(resolver(chave));
        } catch (IOException e) {
            // usa log.warn pq o deleteIfExist nao gera erro, apenas warn
            log.warn("Nao foi possivel remover o arquivo {}; ficou orfao", chave, e);
        }
    }

    private Path resolver(String chave) {
        Path destino = base.resolve(chave).normalize();
        if (!destino.startsWith(base) /*impede do usuario fazer um path traversal, como passar um nome "../../"  */) {
            // com as chaves feitas em UUID's isso nao acontece, mas é bom ter 2 camadas caso alguem no futuro mude
            throw new IllegalArgumentException("Chave invalida");
        }
        return destino;
    }
}
