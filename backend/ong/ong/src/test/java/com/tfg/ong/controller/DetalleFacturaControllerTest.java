package com.tfg.ong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tfg.ong.model.DetalleFactura;
import com.tfg.ong.service.DetalleFacturaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DetalleFacturaController.class)
public class DetalleFacturaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DetalleFacturaService detalleFacturaService;

    private DetalleFactura detalleFactura;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        detalleFactura = new DetalleFactura();
        detalleFactura.setId(1L);
        detalleFactura.setDescripcion("Descripción de prueba");
        detalleFactura.setCantidad(2);
        detalleFactura.setPrecio(BigDecimal.valueOf(100));
        detalleFactura.setIva(BigDecimal.ZERO);
    }

    @Test
    public void testGetAllDetalleFacturas() throws Exception {
        when(detalleFacturaService.getAllDetalleFacturas()).thenReturn(Arrays.asList(detalleFactura));

        mockMvc.perform(get("/api/detallefacturas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descripcion").value("Descripción de prueba"));
    }

    @Test
    public void testGetDetalleFacturaById() throws Exception {
        when(detalleFacturaService.getDetalleFacturaById(1L)).thenReturn(detalleFactura);

        mockMvc.perform(get("/api/detallefacturas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Descripción de prueba"));
    }

    @Test
    public void testCreateDetalleFactura() throws Exception {
        when(detalleFacturaService.createDetalleFactura(any(DetalleFactura.class))).thenReturn(detalleFactura);

        String json = objectMapper.writeValueAsString(detalleFactura);

        mockMvc.perform(post("/api/detallefacturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Descripción de prueba"));
    }

    @Test
    public void testUpdateDetalleFactura() throws Exception {
        when(detalleFacturaService.updateDetalleFacturas(eq(1L), any(DetalleFactura.class))).thenReturn(detalleFactura);

        String json = objectMapper.writeValueAsString(detalleFactura);

        mockMvc.perform(put("/api/detallefacturas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Descripción de prueba"));
    }

    @Test
    public void testDeleteDetalleFactura() throws Exception {
        doNothing().when(detalleFacturaService).deleteDetalleFactura(1L);

        mockMvc.perform(delete("/api/detallefacturas/1"))
                .andExpect(status().isOk());
    }
}
