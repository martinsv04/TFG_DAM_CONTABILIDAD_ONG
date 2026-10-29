package com.tfg.ong.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.tfg.ong.dto.SubvencionDTO;
import com.tfg.ong.model.Gasto;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Proyecto;
import com.tfg.ong.model.Subvencion;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.repository.ProyectoRepository;
import com.tfg.ong.repository.SubvencionRepository;

/** Subvenciones de una ONG: estado, plazos y ejecución (cobrado y gastado frente a lo concedido). */
@Service
public class SubvencionService {

    static final Set<String> ESTADOS = Set.of(Subvencion.SOLICITADA, Subvencion.CONCEDIDA, Subvencion.DENEGADA,
            Subvencion.JUSTIFICADA);

    private final SubvencionRepository subvencionRepository;
    private final ProyectoRepository proyectoRepository;
    private final GastoRepository gastoRepository;
    private final IngresoRepository ingresoRepository;
    private final OngRepository ongRepository;

    public SubvencionService(SubvencionRepository subvencionRepository, ProyectoRepository proyectoRepository,
                             GastoRepository gastoRepository, IngresoRepository ingresoRepository,
                             OngRepository ongRepository) {
        this.subvencionRepository = subvencionRepository;
        this.proyectoRepository = proyectoRepository;
        this.gastoRepository = gastoRepository;
        this.ingresoRepository = ingresoRepository;
        this.ongRepository = ongRepository;
    }

    /** Datos editables de una subvención. */
    public record Datos(String nombre, String financiador, String expediente, String estado,
                        BigDecimal importeSolicitado, BigDecimal importeConcedido, LocalDate fechaSolicitud,
                        LocalDate fechaConcesion, LocalDate fechaJustificacion, Long proyectoId) {
    }

    private static boolean esConcedida(String estado) {
        return Subvencion.CONCEDIDA.equals(estado) || Subvencion.JUSTIFICADA.equals(estado);
    }

    private static String limpio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static void validar(Datos d) {
        if (d.nombre() == null || d.nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la subvención es obligatorio");
        }
        if (d.nombre().trim().length() > 150) {
            throw new IllegalArgumentException("El nombre es demasiado largo (máximo 150 caracteres)");
        }
        if (d.estado() == null || !ESTADOS.contains(d.estado())) {
            throw new IllegalArgumentException("Estado no válido");
        }
        if ((d.importeSolicitado() != null && d.importeSolicitado().signum() < 0)
                || (d.importeConcedido() != null && d.importeConcedido().signum() < 0)) {
            throw new IllegalArgumentException("Los importes no pueden ser negativos");
        }
        if (esConcedida(d.estado()) && (d.importeConcedido() == null || d.importeConcedido().signum() <= 0)) {
            throw new IllegalArgumentException("Indica el importe concedido de una subvención concedida o justificada");
        }
    }

    private void aplicar(Long idOng, Subvencion s, Datos d) {
        s.setNombre(d.nombre().trim());
        s.setFinanciador(limpio(d.financiador()));
        s.setExpediente(limpio(d.expediente()));
        s.setEstado(d.estado());
        s.setImporteSolicitado(d.importeSolicitado());
        // Una subvención solicitada o denegada no tiene importe concedido
        s.setImporteConcedido(esConcedida(d.estado()) ? d.importeConcedido() : null);
        s.setFechaSolicitud(d.fechaSolicitud());
        s.setFechaConcesion(d.fechaConcesion());
        s.setFechaJustificacion(d.fechaJustificacion());
        if (d.proyectoId() == null) {
            s.setProyecto(null);
        } else {
            Proyecto p = proyectoRepository.findById(d.proyectoId())
                    .filter(x -> x.getOng() != null && x.getOng().getId().equals(idOng))
                    .orElseThrow(() -> new IllegalArgumentException("El proyecto no existe en esta ONG"));
            s.setProyecto(p);
        }
    }

    /** La subvención debe existir y ser de la ONG indicada. */
    public Subvencion obtener(Long idOng, Long idSubvencion) {
        return subvencionRepository.findById(idSubvencion)
                .filter(s -> s.getOng() != null && s.getOng().getId().equals(idOng))
                .orElseThrow(() -> new IllegalArgumentException("La subvención no existe"));
    }

    public Subvencion crear(Long idOng, Datos datos) {
        validar(datos);
        Ong ong = ongRepository.findById(idOng).orElseThrow(() -> new IllegalArgumentException("La ONG no existe"));
        Subvencion s = new Subvencion();
        s.setOng(ong);
        aplicar(idOng, s, datos);
        return subvencionRepository.save(s);
    }

    public Subvencion actualizar(Long idOng, Long idSubvencion, Datos datos) {
        validar(datos);
        Subvencion s = obtener(idOng, idSubvencion);
        aplicar(idOng, s, datos);
        return subvencionRepository.save(s);
    }

    public void eliminar(Long idOng, Long idSubvencion) {
        subvencionRepository.delete(obtener(idOng, idSubvencion));
    }

    /** Subvenciones de la ONG, las más recientes primero. */
    public List<SubvencionDTO> listar(Long idOng) {
        List<Gasto> gastos = gastoRepository.findByOngId(idOng);
        List<Ingreso> ingresos = ingresoRepository.findByOngId(idOng);
        LocalDate hoy = LocalDate.now();

        return subvencionRepository.findByOngId(idOng).stream()
                .map(s -> dto(s, gastos, ingresos, hoy))
                .sorted(Comparator.comparing(SubvencionDTO::id).reversed())
                .toList();
    }

    static SubvencionDTO dto(Subvencion s, List<Gasto> gastos, List<Ingreso> ingresos, LocalDate hoy) {
        BigDecimal cobrado = ProyectoService.suma(ingresos.stream()
                .filter(i -> i.getSubvencion() != null && s.getId().equals(i.getSubvencion().getId()))
                .map(i -> i.getMonto() == null ? BigDecimal.ZERO : i.getMonto()).toList());
        Proyecto proyecto = s.getProyecto();
        BigDecimal gastado = proyecto == null ? BigDecimal.ZERO.setScale(2) : ProyectoService.suma(gastos.stream()
                .filter(g -> g.getProyecto() != null && proyecto.getId().equals(g.getProyecto().getId()))
                .map(g -> g.getMonto() == null ? BigDecimal.ZERO : g.getMonto()).toList());

        boolean pendienteDeJustificar = Subvencion.CONCEDIDA.equals(s.getEstado()) && s.getFechaJustificacion() != null;
        Long dias = pendienteDeJustificar ? ChronoUnit.DAYS.between(hoy, s.getFechaJustificacion()) : null;

        return new SubvencionDTO(s.getId(), s.getNombre(), s.getFinanciador(), s.getExpediente(), s.getEstado(),
                s.getImporteSolicitado(), s.getImporteConcedido(), cobrado,
                proyecto == null ? null : proyecto.getId(), proyecto == null ? null : proyecto.getNombre(), gastado,
                ProyectoService.porcentaje(gastado, s.getImporteConcedido()),
                s.getFechaSolicitud(), s.getFechaConcesion(), s.getFechaJustificacion(), dias,
                dias != null && dias < 0);
    }
}
