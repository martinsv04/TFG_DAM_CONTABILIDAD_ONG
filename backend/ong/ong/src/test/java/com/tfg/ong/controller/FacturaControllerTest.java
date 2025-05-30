package com.tfg.ong.controller;

import com.tfg.ong.model.Factura;
import com.tfg.ong.repository.FacturaRepository;
import com.tfg.ong.service.FacturaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacturaControllerTest {

    @Mock
    private FacturaService facturaService;

    @Mock
    private FacturaRepository facturaRepository;

    @InjectMocks
    private FacturaController facturaController;

    @Test
    void TestGetAllFacturas() {
        Factura f1 = new Factura(); f1.setId(1L);
        Factura f2 = new Factura(); f2.setId(2L);
        when(facturaService.getAllFacturas()).thenReturn(Arrays.asList(f1, f2));

        List<Factura> resultado = facturaController.getAllFacturas();

        assertEquals(2, resultado.size());
        verify(facturaService, times(1)).getAllFacturas();
    }

    @Test
    void TestGetFacturaByIdExistente() {
        Factura f = new Factura(); f.setId(1L);
        when(facturaService.getFacturaById(1L)).thenReturn(f);

        Factura resultado = facturaController.getFacturaById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void TestGetFacturaByIdNoExistente() {
        when(facturaService.getFacturaById(99L)).thenReturn(null);

        Factura resultado = facturaController.getFacturaById(99L);

        assertNull(resultado);
    }

    @Test
    void TestCrearFactura() {
        Factura entrada = new Factura();
        entrada.setNumero("F-001");
        entrada.setTotal(new BigDecimal("50.00"));
        entrada.setFecha(LocalDate.of(2024, 5, 1));

        Factura creada = new Factura();
        creada.setId(1L);
        creada.setNumero("F-001");

        when(facturaService.createFactura(entrada)).thenReturn(creada);

        Factura resultado = facturaController.createFactura(entrada);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("F-001", resultado.getNumero());
    }

    @Test
    void TestUpdateFactura() {
        Long id = 1L;
        Factura actualizada = new Factura();
        actualizada.setId(id);
        actualizada.setNumero("F-001-UPDATED");

        when(facturaService.updateFactura(eq(id), any(Factura.class))).thenReturn(actualizada);

        Factura entrada = new Factura();
        entrada.setNumero("F-001");

        Factura resultado = facturaController.updateFactura(id, entrada);

        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals("F-001-UPDATED", resultado.getNumero());
    }

    @Test
    void TestDeleteFactura() {
        Long id = 1L;
        doNothing().when(facturaService).deleteFactura(id);

        facturaController.deleteFactura(id);

        verify(facturaService, times(1)).deleteFactura(id);
    }

    @Test
    void TestGetFacturasByOng() {
        Long ongId = 1L;
        Factura f1 = new Factura(); f1.setId(1L);
        Factura f2 = new Factura(); f2.setId(2L);
        List<Factura> lista = Arrays.asList(f1, f2);

        when(facturaRepository.findByOngId(ongId)).thenReturn(lista);

        List<Factura> resultado = facturaController.getFacturasByOng(ongId);

        assertEquals(2, resultado.size());
        assertEquals(lista, resultado);
        verify(facturaRepository, times(1)).findByOngId(ongId);
    }
}
