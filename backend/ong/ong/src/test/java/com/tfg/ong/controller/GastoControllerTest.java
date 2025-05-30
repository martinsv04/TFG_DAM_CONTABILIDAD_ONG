
package com.tfg.ong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tfg.ong.model.Categoria;
import com.tfg.ong.model.Gasto;
import com.tfg.ong.model.Ong;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.service.GastoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GastoController.class)
public class GastoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GastoService gastoService;

    @MockitoBean
    private GastoRepository gastoRepository;

    @MockitoBean
    private OngRepository ongRepository;

    private Gasto mockGasto;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        mockGasto = new Gasto();
        mockGasto.setId(1L);
        mockGasto.setDescripcion("Gasto de prueba");
        mockGasto.setMonto(new BigDecimal("100.00"));
        mockGasto.setCategoria(Categoria.PERSONAL);
        mockGasto.setFecha(LocalDate.of(2024, 5, 1));
        mockGasto.setOng(new Ong());

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    public void testGetAllGastos() throws Exception {
        when(gastoService.getAllGastos()).thenReturn(Arrays.asList(mockGasto));

        mockMvc.perform(get("/api/gastos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descripcion").value("Gasto de prueba"));
    }

    @Test
    public void testGetGastoById() throws Exception {
        when(gastoService.getGastoById(1L)).thenReturn(mockGasto);

        mockMvc.perform(get("/api/gastos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Gasto de prueba"));
    }

    @Test
    public void testUpdateGasto() throws Exception {
        when(gastoService.updateGastos(eq(1L), any(Gasto.class))).thenReturn(mockGasto);
        String json = objectMapper.writeValueAsString(mockGasto);

        mockMvc.perform(put("/api/gastos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Gasto de prueba"));
    }

    @Test
    public void testDeleteGasto() throws Exception {
        mockMvc.perform(delete("/api/gastos/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetGastosByOng() throws Exception {
        when(gastoRepository.findByOngId(1L)).thenReturn(Arrays.asList(mockGasto));

        mockMvc.perform(get("/api/gastos/ong/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descripcion").value("Gasto de prueba"));
    }

    @Test
    public void testCrearGastoConFactura() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("descripcion", "Compra material");
        payload.put("monto", 80.0);
        payload.put("categoria", "PERSONAL");
        payload.put("fecha", "2024-05-01");
        payload.put("id_ong", 1);
        payload.put("id_usuario", 2);

        Ong ong = new Ong();
        when(ongRepository.findById(1L)).thenReturn(Optional.of(ong));

        Gasto gastoGuardado = new Gasto();
        gastoGuardado.setId(100L);
        when(gastoService.createGastoConFactura(any(Gasto.class), eq(2L))).thenReturn(gastoGuardado);

        String json = objectMapper.writeValueAsString(payload);

        mockMvc.perform(post("/api/gastos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Gasto registrado correctamente"))
                .andExpect(jsonPath("$.facturaId").value(100));
    }


    @Test
    public void testCrearGastoConFactura_SinIdUsuario() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("descripcion", "Transporte");
        payload.put("monto", 30.0);
        payload.put("categoria", "PERSONAL");
        payload.put("fecha", "2024-05-01");
        payload.put("id_ong", 1);

        Ong ong = new Ong();
        when(ongRepository.findById(1L)).thenReturn(Optional.of(ong));

        Gasto gastoGuardado = new Gasto();
        gastoGuardado.setId(101L);
        when(gastoService.createGastoConFactura(any(Gasto.class), isNull())).thenReturn(gastoGuardado);

        String json = objectMapper.writeValueAsString(payload);

        mockMvc.perform(post("/api/gastos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Gasto registrado correctamente"))
                .andExpect(jsonPath("$.facturaId").value(101));
    }

    @Test
    public void testCrearGastoConFactura_BadRequest() throws Exception {
        String payload = """
                    {
                        "descripcion": "Error test",
                        "monto": "INVALIDO",
                        "categoria": "PERSONAL",
                        "fecha": "2024-05-01",
                        "id_ong": 1
                    }
                """;

        mockMvc.perform(post("/api/gastos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

}
