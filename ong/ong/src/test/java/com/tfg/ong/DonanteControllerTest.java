package com.tfg.ong;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.tfg.ong.controller.DonanteController;
import com.tfg.ong.model.Donante;
import com.tfg.ong.service.DonanteService;

@WebMvcTest(DonanteController.class)
class DonanteControllerTest extends AbstractControllerTest {

    

    @MockitoBean
    private DonanteService donanteService;

    private Donante donante1;

    private List<Donante> donantes;

    @BeforeEach
   void initData(){
    initObject();
   }

    @Test
     void testGetAllDonantes() throws Exception {
        when(donanteService.getAllDonantes()).thenReturn(donantes);

        mockMvc.perform(get("/api/donantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(donantes.size()))
                .andExpect(jsonPath("$[0].nombre").value("Juan"))
                .andExpect(jsonPath("$[1].nombre").value("Maria"));
    }

    void initObject() {
         
        donante1 = new Donante();
        donante1.setNombre("Juan");
        donante1.setEmail("juanito@gmail.com");
        donante1.setTelefono("123456789");
        donante1.setNifCif("12345678A");
        
    }
}
