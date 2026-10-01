
package com.tfg.ong.service;

import com.tfg.ong.model.DetalleFactura;
import com.tfg.ong.model.Factura;
import com.tfg.ong.repository.FacturaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FacturaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @InjectMocks
    private FacturaService facturaService;

    private Factura factura;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        factura = new Factura();
        factura.setId(1L);
        factura.setNumero("F-TEST");
        factura.setFecha(LocalDate.of(2024, 5, 1));
        factura.setTotal(new BigDecimal("250.00"));
    }

    @Test
    public void testGetAllFacturas() {
        when(facturaRepository.findAll()).thenReturn(Arrays.asList(factura));
        List<Factura> result = facturaService.getAllFacturas();
        assertEquals(1, result.size());
        assertEquals("F-TEST", result.get(0).getNumero());
    }

    @Test
    public void testGetFacturaById() {
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));
        Factura result = facturaService.getFacturaById(1L);
        assertNotNull(result);
        assertEquals("F-TEST", result.getNumero());
    }

    @Test
    public void testGetFacturaById_NotFound() {
        when(facturaRepository.findById(1L)).thenReturn(Optional.empty());
        Factura result = facturaService.getFacturaById(1L);
        assertNull(result);
    }

    @Test
    public void testCreateFactura() {
        when(facturaRepository.save(factura)).thenReturn(factura);
        Factura result = facturaService.createFactura(factura);
        assertNotNull(result);
        assertEquals("F-TEST", result.getNumero());
    }

    @Test
    public void testUpdateFactura() {
        factura.setNumero("F-UPDATED");
        when(facturaRepository.save(factura)).thenReturn(factura);
        Factura result = facturaService.updateFactura(1L, factura);
        assertEquals("F-UPDATED", result.getNumero());
        assertEquals(1L, result.getId());
    }

    @Test
    public void testDeleteFactura() {
        facturaService.deleteFactura(1L);
        verify(facturaRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testCreateFactura_WithDetalles() {
        DetalleFactura detalle = new DetalleFactura();
        detalle.setDescripcion("Producto");
        detalle.setCantidad(2);
        detalle.setPrecio(new BigDecimal("50"));
        detalle.setIva(new BigDecimal("10"));

        factura.setDetalles(List.of(detalle));

        when(facturaRepository.save(any(Factura.class))).thenAnswer(invoc -> invoc.getArgument(0));

        Factura result = facturaService.createFactura(factura);

        assertNotNull(result);
        assertEquals(1, result.getDetalles().size());
        assertEquals(result, result.getDetalles().get(0).getFactura());
    }

    @Test
    public void testUpdateFactura_WithDetalles() {
        DetalleFactura detalle = new DetalleFactura();
        detalle.setDescripcion("Producto");
        detalle.setCantidad(1);
        detalle.setPrecio(new BigDecimal("100"));
        detalle.setIva(new BigDecimal("21"));

        factura.setDetalles(List.of(detalle));

        when(facturaRepository.save(any(Factura.class))).thenAnswer(invoc -> invoc.getArgument(0));

        Factura result = facturaService.updateFactura(1L, factura);

        assertEquals(1L, result.getId());
        assertEquals("F-TEST", result.getNumero());
        assertEquals(result, result.getDetalles().get(0).getFactura());
    }


}
