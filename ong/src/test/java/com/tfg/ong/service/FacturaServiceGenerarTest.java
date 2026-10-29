package com.tfg.ong.service;

import com.tfg.ong.model.Factura;
import com.tfg.ong.model.Gasto;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.repository.FacturaRepository;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.IngresoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacturaServiceGenerarTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private IngresoRepository ingresoRepository;

    @Mock
    private GastoRepository gastoRepository;

    @InjectMocks
    private FacturaService facturaService;

    private Ong ong(Long id) {
        Ong o = new Ong();
        o.setId(id);
        return o;
    }

    private Ingreso ingreso(Long id, Long idOng) {
        Ingreso i = new Ingreso();
        i.setId(id);
        i.setOng(ong(idOng));
        i.setMonto(new BigDecimal("150.5"));
        i.setDescripcion("Donación campaña");
        i.setFecha(LocalDate.of(2026, 9, 1));
        return i;
    }

    private Gasto gasto(Long id, Long idOng) {
        Gasto g = new Gasto();
        g.setId(id);
        g.setOng(ong(idOng));
        g.setMonto(new BigDecimal("40"));
        g.setDescripcion("Material");
        return g;
    }

    @Test
    void generaFacturaDeUnIngreso() {
        Ingreso i = ingreso(7L, 1L);
        when(ingresoRepository.findById(7L)).thenReturn(Optional.of(i));
        when(facturaRepository.existsByIngresoId(7L)).thenReturn(false);
        when(facturaRepository.save(any(Factura.class))).thenAnswer(inv -> inv.getArgument(0));

        Factura f = facturaService.generarDesdeMovimiento(1L, "INGRESO", 7L);

        assertSame(i, f.getIngreso());
        assertNull(f.getGasto());
        assertEquals(new BigDecimal("150.50"), f.getTotal());
        assertEquals("F-" + LocalDate.now().getYear() + "-0001", f.getNumero());
        assertEquals(1, f.getDetalles().size());
        assertEquals("Donación campaña", f.getDetalles().get(0).getDescripcion());
        assertSame(f, f.getDetalles().get(0).getFactura());
    }

    @Test
    void generaFacturaDeUnGasto() {
        Gasto g = gasto(3L, 1L);
        when(gastoRepository.findById(3L)).thenReturn(Optional.of(g));
        when(facturaRepository.existsByGastoId(3L)).thenReturn(false);
        when(facturaRepository.save(any(Factura.class))).thenAnswer(inv -> inv.getArgument(0));

        Factura f = facturaService.generarDesdeMovimiento(1L, "gasto", 3L);

        assertSame(g, f.getGasto());
        assertNull(f.getIngreso());
        assertEquals(new BigDecimal("40.00"), f.getTotal());
        assertEquals("G-" + LocalDate.now().getYear() + "-0001", f.getNumero());
    }

    @Test
    void numeraCorrelativamenteDentroDeLaSerieYElAnio() {
        int anio = LocalDate.now().getYear();
        when(gastoRepository.findById(3L)).thenReturn(Optional.of(gasto(3L, 1L)));
        when(facturaRepository.findNumerosByOngIdAndPrefijo(1L, "G-" + anio + "-"))
                .thenReturn(List.of("G-" + anio + "-0001", "G-" + anio + "-0009", "G-" + anio + "-0002", "G-" + anio + "-ABC"));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(inv -> inv.getArgument(0));

        Factura f = facturaService.generarDesdeMovimiento(1L, "GASTO", 3L);

        assertEquals("G-" + anio + "-0010", f.getNumero());
    }

    @Test
    void quitaElIvaIncluidoDelImporte() {
        when(gastoRepository.findById(3L)).thenReturn(Optional.of(gasto(3L, 1L)));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(inv -> inv.getArgument(0));

        Factura f = facturaService.generarDesdeMovimiento(1L, "GASTO", 3L, new BigDecimal("21"));

        // 40,00 € con IVA incluido: base 33,06 € y cuota 6,94 €
        assertEquals(new BigDecimal("40.00"), f.getTotal());
        assertEquals(new BigDecimal("33.06"), f.getDetalles().get(0).getPrecio());
        assertEquals(0, new BigDecimal("21").compareTo(f.getDetalles().get(0).getIva()));
    }

    @Test
    void sinIvaLaBaseEsElTotal() {
        when(gastoRepository.findById(3L)).thenReturn(Optional.of(gasto(3L, 1L)));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(inv -> inv.getArgument(0));

        Factura f = facturaService.generarDesdeMovimiento(1L, "GASTO", 3L, null);

        assertEquals(new BigDecimal("40.00"), f.getDetalles().get(0).getPrecio());
        assertEquals(0, BigDecimal.ZERO.compareTo(f.getDetalles().get(0).getIva()));
    }

    @Test
    void rechazaUnIvaNoPermitido() {
        assertThrows(IllegalArgumentException.class,
                () -> facturaService.generarDesdeMovimiento(1L, "GASTO", 3L, new BigDecimal("19")));
        assertThrows(IllegalArgumentException.class,
                () -> facturaService.generarDesdeMovimiento(1L, "GASTO", 3L, new BigDecimal("10.5")));
        verify(facturaRepository, never()).save(any());
    }

    @Test
    void rechazaMovimientoDeOtraOng() {
        when(ingresoRepository.findById(7L)).thenReturn(Optional.of(ingreso(7L, 2L)));

        assertThrows(IllegalArgumentException.class, () -> facturaService.generarDesdeMovimiento(1L, "INGRESO", 7L));
        verify(facturaRepository, never()).save(any());
    }

    @Test
    void rechazaMovimientoQueYaTieneFactura() {
        when(gastoRepository.findById(3L)).thenReturn(Optional.of(gasto(3L, 1L)));
        when(facturaRepository.existsByGastoId(3L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> facturaService.generarDesdeMovimiento(1L, "GASTO", 3L));
        verify(facturaRepository, never()).save(any());
    }

    @Test
    void rechazaTipoInvalidoOMovimientoInexistente() {
        when(ingresoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> facturaService.generarDesdeMovimiento(1L, "OTRO", 1L));
        assertThrows(IllegalArgumentException.class, () -> facturaService.generarDesdeMovimiento(1L, null, 1L));
        assertThrows(IllegalArgumentException.class, () -> facturaService.generarDesdeMovimiento(1L, "INGRESO", 99L));
    }
}
