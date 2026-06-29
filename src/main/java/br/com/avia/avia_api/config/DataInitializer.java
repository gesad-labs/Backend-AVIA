package br.com.avia.avia_api.config;

import br.com.avia.avia_api.entity.Papel;
import br.com.avia.avia_api.enums.PapelProjeto;
import br.com.avia.avia_api.repository.PapelRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private final PapelRepository papelRepository;

    public DataInitializer(PapelRepository papelRepository) {
        this.papelRepository = papelRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        // ProjetoService looks up papeis by PapelProjeto.name() ("GERENTE", "MEMBRO", "VISUALIZADOR")
        for (PapelProjeto papel : PapelProjeto.values()) {
            String nome = papel.name();
            if (papelRepository.findByNome(nome).isEmpty()) {
                papelRepository.save(new Papel(nome));
            }
        }
    }
}
