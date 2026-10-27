package br.com.avia.avia_api.armazenamento;


import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

// usei service pois qnd formos usar serviços na nuvem a gente n precise mexer na Service, so cria uma nova classe q implemente a interface
public interface ArmazenamentoArquivos {
    void salvar(String chave, InputStream conteudo) throws IOException;
    Resource carregar(String chave);
    void remover(String chave);
}
