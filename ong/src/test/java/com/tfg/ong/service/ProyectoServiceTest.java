package com.tfg.ong.service;

import com.tfg.ong.dto.ProyectoDTO;
import com.tfg.ong.dto.ProyectoDetalleDTO;
import com.tfg.ong.model.Categoria;
import com.tfg.ong.model.Gasto;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Proyecto;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.repository.ProyectoRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProyectoServiceTest {

    @Mock
    private ProyectoRepository proyectoRepository;
    @Mock
    private GastoRepository gastoRepository;
    @Mock
    private IngresoRepository ingresoRepository;
    @Mock
    private OngRepository ongRepository;
    @Mock
    private SubvencionService subvencionService;

    @InjectMocks
    private ProyectoService service;

    private Ong ong;
    private Proyecto comedor;
    private Proyecto huerto;

    @BeforeEach
    void preparar() {
        ong = new Ong();
        ong.setId(1L);
        lenient().when(ongRepository.findById(1L)).thenReturn(Optional.of(ong));
        lenient().when(proyectoRepository.save(any(Proyecto.class))).thenAnswer(i -> i.getArgument(0));

        comedor = proyecto(10L, "Comedor social", "1000");
        huerto = proyecto(11L, "Huerto", null);
        lenient().when(proyectoRepository.findByOngId(1L)).thenReturn(List.of(comedor, huerto));
        lenient().when(proyectoRepository.findById(10L)).thenReturn(Optional.of(comedor));
        lenient().when(subvencionService.listar(1L)).thenReturn(List.of());
    }

    private Proyecto proyecto(Long id, String nombre, String presupuesto) {
        Proyecto p = new Proyecto();
        p.setId(id);
        p.setOng(ong);
        p.setNombre(nombre);
        p.setEstado(Proyecto.EN_CURSO);
        p.setPresupuesto(presupuesto == null ? null : new BigDecimal(presupuesto));
        return p;
    }

    private Gasto gasto(long id, Proyecto p, String monto, String fecha) {
        Gasto g = new Gasto();
        g.setId(id);
        g.setOng(ong);
        g.setProyecto(p);
        g.setCategoria(Categoria.values()[0]);
        g.setMonto(new BigDecimal(monto));
        g.setFecha(LocalDate.parse(fecha));
        g.setDescripcion("Gasto " + id);
        return g;
    }

    private Ingreso ingreso(long id, Proyecto p, String monto) {
        Ingreso i = new Ingreso();
        i.setId(id);
        i.setOng(ong);
        i.setProyecto(p);
        i.setMonto(new BigDecimal(monto));
        i.setFecha(LocalDate.parse("2026-03-01"));
        i.setTipo("SUBVENCIÓN");
        return i;
    }

    private ProyectoService.Datos datos(String nombre, String estado, String presupuesto, String inicio, String fin) {
        return new ProyectoService.Datos(nombre, " desc ", estado, inicio == null ? null : LocalDate.parse(inicio),
                fin == null ? null : LocalDate.parse(fin), presupuesto == null ? null : new BigDecimal(presupuesto));
    }

    @Test
    void listaCalculaEjecucionYSoloContabilizaSusMovimientos() {
        when(gastoRepository.findByOngId(1L)).thenReturn(List.of(gasto(1, comedor, "300", "2026-02-01"), gasto(2, comedor, "250.50", "2026-03-01"),
                gasto(3, null, "999", "2026-03-02"), gasto(4, huerto, "40", "2026-03-03")));
        when(ingresoRepository.findByOngId(1L)).thenReturn(List.of(ingreso(1, comedor, "800"), ingreso(2, null, "5000")));

        List<ProyectoDTO> lista = service.listar(1L);

        assertEquals(2, lista.size());
        ProyectoDTO c = lista.stream().filter(p -> p.id() == 10L).findFirst().orElseThrow();
        assertEquals(new BigDecimal("550.50"), c.gastado());
        assertEquals(new BigDecimal("800.00"), c.ingresado());
        assertEquals(new BigDecimal("249.50"), c.resultado());
        assertEquals(new BigDecimal("55.1"), c.porcentajeEjecutado());
        assertFalse(c.superaPresupuesto());
        assertEquals(2, c.numGastos());

        ProyectoDTO h = lista.stream().filter(p -> p.id() == 11L).findFirst().orElseThrow();
        assertNull(h.porcentajeEjecutado());
        assertFalse(h.superaPresupuesto());
    }

    @Test
    void avisaCuandoSeSuperaElPresupuesto() {
        when(gastoRepository.findByOngId(1L)).thenReturn(List.of(gasto(1, comedor, "1200", "2026-02-01")));
        when(ingresoRepository.findByOngId(1L)).thenReturn(List.of());

        ProyectoDTO c = service.listar(1L).stream().filter(p -> p.id() == 10L).findFirst().orElseThrow();

        assertTrue(c.superaPresupuesto());
        assertEquals(new BigDecimal("120.0"), c.porcentajeEjecutado());
    }

    @Test
    void detalleOrdenaLosMovimientosPorFechaDescendente() {
        when(gastoRepository.findByOngId(1L)).thenReturn(List.of(gasto(1, comedor, "10", "2026-01-01"), gasto(2, comedor, "20", "2026-05-01")));
        when(ingresoRepository.findByOngId(1L)).thenReturn(List.of(ingreso(1, comedor, "30")));

        ProyectoDetalleDTO d = service.detalle(1L, 10L);

        assertEquals(2L, d.gastos().get(0).id());
        assertEquals(1, d.ingresos().size());
        assertEquals("Comedor social", d.proyecto().nombre());
    }

    @Test
    void detalleDeProyectoDeOtraOngFalla() {
        Ong otra = new Ong();
        otra.setId(2L);
        comedor.setOng(otra);

        assertThrows(IllegalArgumentException.class, () -> service.detalle(1L, 10L));
    }

    @Test
    void creaProyectoConLosDatosLimpios() {
        Proyecto p = service.crear(1L, datos("  Taller de empleo ", "PLANIFICADO", "2500", "2026-01-01", "2026-12-31"));

        assertEquals("Taller de empleo", p.getNombre());
        assertEquals("desc", p.getDescripcion());
        assertSame(ong, p.getOng());
        verify(proyectoRepository).save(p);
    }

    @Test
    void validaLosDatosDelProyecto() {
        assertThrows(IllegalArgumentException.class, () -> service.crear(1L, datos(" ", "EN_CURSO", null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> service.crear(1L, datos("X", "OTRO", null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> service.crear(1L, datos("X", "EN_CURSO", "-1", null, null)));
        assertThrows(IllegalArgumentException.class,
                () -> service.crear(1L, datos("X", "EN_CURSO", null, "2026-05-01", "2026-04-01")));
    }

    @Test
    void actualizaYElimina() {
        service.actualizar(1L, 10L, datos("Comedor 2", "FINALIZADO", "1500", null, null));
        assertEquals("Comedor 2", comedor.getNombre());
        assertEquals("FINALIZADO", comedor.getEstado());

        service.eliminar(1L, 10L);
        verify(proyectoRepository).delete(comedor);
    }
}
