
package com.tfg.ong.service;

import com.tfg.ong.model.DetalleFactura;
import com.tfg.ong.repository.DetalleFacturaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DetalleFacturaServiceTest {

    private DetalleFacturaRepository detalleFacturaRepository;
    private DetalleFacturaService detalleFacturaService;

    @BeforeEach
    public void setUp() {
        detalleFacturaRepository = mock(DetalleFacturaRepository.class);
        detalleFacturaService = new DetalleFacturaService(detalleFacturaRepository);
    }

    @Test
    public void testGetAllDetalleFacturas() {
        DetalleFactura detalle = new DetalleFactura();
        detalle.setId(1L);
        detalle.setDescripcion("Servicio");
        detalle.setCantidad(1);
        detalle.setPrecio(BigDecimal.valueOf(100));
        detalle.setIva(BigDecimal.ZERO);

        when(detalleFacturaRepository.findAll()).thenReturn(Arrays.asList(detalle));

        List<DetalleFactura> result = detalleFacturaService.getAllDetalleFacturas();
        assertEquals(1, result.size());
        assertEquals("Servicio", result.get(0).getDescripcion());
    }

    @Test
    public void testGetDetalleFacturaById() {
        DetalleFactura detalle = new DetalleFactura();
        detalle.setId(1L);
        when(detalleFacturaRepository.findById(1L)).thenReturn(Optional.of(detalle));

        DetalleFactura result = detalleFacturaService.getDetalleFacturaById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void testCreateDetalleFactura() {
        DetalleFactura detalle = new DetalleFactura();
        when(detalleFacturaRepository.save(detalle)).thenReturn(detalle);

        DetalleFactura result = detalleFacturaService.createDetalleFactura(detalle);
        assertEquals(detalle, result);
    }

    @Test
    public void testUpdateDetalleFactura() {
        DetalleFactura detalle = new DetalleFactura();
        detalle.setDescripcion("Inicial");

        DetalleFactura actualizado = new DetalleFactura();
        actualizado.setId(1L);
        actualizado.setDescripcion("Actualizado");

        when(detalleFacturaRepository.save(actualizado)).thenReturn(actualizado);

        DetalleFactura result = detalleFacturaService.updateDetalleFacturas(1L, actualizado);
        assertEquals(1L, result.getId());
        assertEquals("Actualizado", result.getDescripcion());
    }

    @Test
    public void testDeleteDetalleFactura() {
        detalleFacturaService.deleteDetalleFactura(1L);
        verify(detalleFacturaRepository, times(1)).deleteById(1L);
    }
}
