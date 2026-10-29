package com.tfg.ong.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.tfg.ong.dto.MovimientoDTO;
import com.tfg.ong.dto.ProyectoDTO;
import com.tfg.ong.dto.ProyectoDetalleDTO;
import com.tfg.ong.dto.SubvencionDTO;
import com.tfg.ong.model.Gasto;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Proyecto;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.repository.ProyectoRepository;

/** Proyectos de una ONG y su ejecución (gastos e ingresos asignados frente al presupuesto). */
@Service
public class ProyectoService {

    static final Set<String> ESTADOS = Set.of(Proyecto.PLANIFICADO, Proyecto.EN_CURSO, Proyecto.FINALIZADO);

    private final ProyectoRepository proyectoRepository;
    private final GastoRepository gastoRepository;
    private final IngresoRepository ingresoRepository;
    private final OngRepository ongRepository;
    private final SubvencionService subvencionService;

    public ProyectoService(ProyectoRepository proyectoRepository, GastoRepository gastoRepository,
                           IngresoRepository ingresoRepository, OngRepository ongRepository,
                           SubvencionService subvencionService) {
        this.proyectoRepository = proyectoRepository;
        this.gastoRepository = gastoRepository;
        this.ingresoRepository = ingresoRepository;
        this.ongRepository = ongRepository;
        this.subvencionService = subvencionService;
    }

    /** Datos editables de un proyecto. */
    public record Datos(String nombre, String descripcion, String estado, LocalDate fechaInicio, LocalDate fechaFin,
                        BigDecimal presupuesto) {
    }

    static BigDecimal suma(List<BigDecimal> importes) {
        return importes.stream().reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    /** Porcentaje con un decimal; null si no hay base (presupuesto o importe concedido) con la que compararlo. */
    static BigDecimal porcentaje(BigDecimal parte, BigDecimal base) {
        if (base == null || base.signum() <= 0) {
            return null;
        }
        return parte.multiply(new BigDecimal("100")).divide(base, 1, RoundingMode.HALF_UP);
    }

    private static void validar(Datos d) {
        if (d.nombre() == null || d.nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del proyecto es obligatorio");
        }
        if (d.nombre().trim().length() > 150) {
            throw new IllegalArgumentException("El nombre es demasiado largo (máximo 150 caracteres)");
        }
        if (d.estado() == null || !ESTADOS.contains(d.estado())) {
            throw new IllegalArgumentException("Estado no válido");
        }
        if (d.presupuesto() != null && d.presupuesto().signum() < 0) {
            throw new IllegalArgumentException("El presupuesto no puede ser negativo");
        }
        if (d.fechaInicio() != null && d.fechaFin() != null && d.fechaFin().isBefore(d.fechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio");
        }
    }

    private static void aplicar(Proyecto p, Datos d) {
        p.setNombre(d.nombre().trim());
        p.setDescripcion(d.descripcion() == null || d.descripcion().isBlank() ? null : d.descripcion().trim());
        p.setEstado(d.estado());
        p.setFechaInicio(d.fechaInicio());
        p.setFechaFin(d.fechaFin());
        p.setPresupuesto(d.presupuesto());
    }

    /** El proyecto debe existir y ser de la ONG indicada. */
    public Proyecto obtener(Long idOng, Long idProyecto) {
        return proyectoRepository.findById(idProyecto)
                .filter(p -> p.getOng() != null && p.getOng().getId().equals(idOng))
                .orElseThrow(() -> new IllegalArgumentException("El proyecto no existe"));
    }

    public Proyecto crear(Long idOng, Datos datos) {
        validar(datos);
        Ong ong = ongRepository.findById(idOng).orElseThrow(() -> new IllegalArgumentException("La ONG no existe"));
        Proyecto p = new Proyecto();
        p.setOng(ong);
        aplicar(p, datos);
        return proyectoRepository.save(p);
    }

    public Proyecto actualizar(Long idOng, Long idProyecto, Datos datos) {
        validar(datos);
        Proyecto p = obtener(idOng, idProyecto);
        aplicar(p, datos);
        return proyectoRepository.save(p);
    }

    public void eliminar(Long idOng, Long idProyecto) {
        proyectoRepository.delete(obtener(idOng, idProyecto));
    }

    private static ProyectoDTO resumen(Proyecto p, List<Gasto> gastos, List<Ingreso> ingresos) {
        BigDecimal gastado = suma(gastos.stream().map(g -> g.getMonto() == null ? BigDecimal.ZERO : g.getMonto()).toList());
        BigDecimal ingresado = suma(ingresos.stream().map(i -> i.getMonto() == null ? BigDecimal.ZERO : i.getMonto()).toList());
        boolean supera = p.getPresupuesto() != null && p.getPresupuesto().signum() > 0
                && gastado.compareTo(p.getPresupuesto()) > 0;
        return new ProyectoDTO(p.getId(), p.getNombre(), p.getDescripcion(), p.getEstado(), p.getFechaInicio(),
                p.getFechaFin(), p.getPresupuesto(), gastado, ingresado, ingresado.subtract(gastado),
                porcentaje(gastado, p.getPresupuesto()), supera, gastos.size(), ingresos.size());
    }

    private static List<Gasto> gastosDe(Proyecto p, List<Gasto> todos) {
        return todos.stream().filter(g -> g.getProyecto() != null && p.getId().equals(g.getProyecto().getId())).toList();
    }

    private static List<Ingreso> ingresosDe(Proyecto p, List<Ingreso> todos) {
        return todos.stream().filter(i -> i.getProyecto() != null && p.getId().equals(i.getProyecto().getId())).toList();
    }

    /** Proyectos de la ONG, los más recientes primero. */
    public List<ProyectoDTO> listar(Long idOng) {
        List<Gasto> gastos = gastoRepository.findByOngId(idOng);
        List<Ingreso> ingresos = ingresoRepository.findByOngId(idOng);
        return proyectoRepository.findByOngId(idOng).stream()
                .sorted(Comparator.comparing(Proyecto::getId).reversed())
                .map(p -> resumen(p, gastosDe(p, gastos), ingresosDe(p, ingresos)))
                .toList();
    }

    public ProyectoDetalleDTO detalle(Long idOng, Long idProyecto) {
        Proyecto p = obtener(idOng, idProyecto);
        List<Gasto> gastos = gastosDe(p, gastoRepository.findByOngId(idOng));
        List<Ingreso> ingresos = ingresosDe(p, ingresoRepository.findByOngId(idOng));

        Comparator<MovimientoDTO> masRecientes = Comparator
                .comparing(MovimientoDTO::fecha, Comparator.nullsLast(Comparator.<LocalDate>reverseOrder()));
        List<MovimientoDTO> gastosDto = gastos.stream()
                .map(g -> new MovimientoDTO(g.getId(), g.getFecha(), g.getDescripcion(),
                        g.getCategoria() == null ? null : g.getCategoria().name(), g.getMonto()))
                .sorted(masRecientes).toList();
        List<MovimientoDTO> ingresosDto = ingresos.stream()
                .map(i -> new MovimientoDTO(i.getId(), i.getFecha(), i.getDescripcion(), i.getTipo(), i.getMonto()))
                .sorted(masRecientes).toList();
        List<SubvencionDTO> subvenciones = subvencionService.listar(idOng).stream()
                .filter(s -> idProyecto.equals(s.proyectoId())).toList();

        return new ProyectoDetalleDTO(resumen(p, gastos, ingresos), gastosDto, ingresosDto, subvenciones);
    }
}
