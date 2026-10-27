package br.com.avia.avia_api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VersionControllerTest {

    @Test
    void devolveAmbienteVersaoERotulo() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new VersionController("prod", "0.1.0")).build();

        mvc.perform(get("/version"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.env").value("prod"))
                .andExpect(jsonPath("$.version").value("0.1.0"))
                .andExpect(jsonPath("$.label").value("prod-0.1.0"));
    }
}
