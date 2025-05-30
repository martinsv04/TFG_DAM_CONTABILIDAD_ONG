
package com.tfg.ong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.service.IngresoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IngresoController.class)
public class IngresoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngresoService ingresoService;

    @MockitoBean
    private IngresoRepository ingresoRepository;

    @MockitoBean
    private OngRepository ongRepository;

    private ObjectMapper objectMapper;
    private Ingreso mockIngreso;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockIngreso = new Ingreso();
        mockIngreso.setId(1L);
        mockIngreso.setMonto(new BigDecimal("100.50"));
        mockIngreso.setFecha(LocalDate.of(2024, 5, 1));
        mockIngreso.setTipo("Donación");
        mockIngreso.setDescripcion("Ingreso de prueba");
        mockIngreso.setOng(new Ong());
    }

    @Test
    public void testGetAllIngresos() throws Exception {
        when(ingresoService.getAllIngresos()).thenReturn(List.of(mockIngreso));

        mockMvc.perform(get("/api/ingresos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descripcion").value("Ingreso de prueba"));
    }

    @Test
    public void testGetIngresoById() throws Exception {
        when(ingresoService.getIngresoById(1L)).thenReturn(mockIngreso);

        mockMvc.perform(get("/api/ingresos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Donación"));
    }

    @Test
    public void testUpdateIngreso() throws Exception {
        when(ingresoService.updateIngresos(eq(1L), any(Ingreso.class))).thenReturn(mockIngreso);

        String json = objectMapper.writeValueAsString(mockIngreso);

        mockMvc.perform(put("/api/ingresos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monto").value(100.50));
    }

    @Test
    public void testDeleteIngreso() throws Exception {
        doNothing().when(ingresoService).deleteIngreso(1L);

        mockMvc.perform(delete("/api/ingresos/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetIngresosByOng() throws Exception {
        when(ingresoRepository.findByOngId(1L)).thenReturn(List.of(mockIngreso));

        mockMvc.perform(get("/api/ingresos/ong/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descripcion").value("Ingreso de prueba"));
    }

    @Test
    public void testCrearIngresoConFactura() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("descripcion", "Ingreso de prueba");
        payload.put("monto", 100.5);
        payload.put("tipo", "Donación");
        payload.put("fecha", "2024-05-01");
        payload.put("id_ong", 1);
        payload.put("id_usuario", 2);

        Ong ong = new Ong();
        when(ongRepository.findById(1L)).thenReturn(Optional.of(ong));

        Ingreso ingresoGuardado = new Ingreso();
        ingresoGuardado.setId(10L);
        when(ingresoService.createIngresoConFactura(any(Ingreso.class), eq(2L))).thenReturn(ingresoGuardado);

        String json = objectMapper.writeValueAsString(payload);

        mockMvc.perform(post("/api/ingresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ingreso registrado correctamente"))
                .andExpect(jsonPath("$.facturaId").value(10));
    }

    @Test
    public void testCrearIngresoConFactura_Error() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("descripcion", "Ingreso inválido");
        payload.put("monto", "no_es_un_numero");
        payload.put("tipo", "Donación");
        payload.put("fecha", "2024-05-01");
        payload.put("id_ong", 1);
        payload.put("id_usuario", 2);

        String json = objectMapper.writeValueAsString(payload);

        mockMvc.perform(post("/api/ingresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCrearIngresoConFactura_SinIdUsuario() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("descripcion", "Ingreso sin usuario");
        payload.put("monto", 50.0);
        payload.put("tipo", "Donación");
        payload.put("fecha", "2024-05-01");
        payload.put("id_ong", 1);

        Ong ong = new Ong();
        when(ongRepository.findById(1L)).thenReturn(Optional.of(ong));

        Ingreso ingresoGuardado = new Ingreso();
        ingresoGuardado.setId(11L);
        when(ingresoService.createIngresoConFactura(any(Ingreso.class), isNull())).thenReturn(ingresoGuardado);

        String json = objectMapper.writeValueAsString(payload);

        mockMvc.perform(post("/api/ingresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ingreso registrado correctamente"))
                .andExpect(jsonPath("$.facturaId").value(11));
    }


}
