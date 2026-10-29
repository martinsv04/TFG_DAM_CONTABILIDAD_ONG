package com.tfg.ong.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tfg.ong.exception.PlanException;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Suscripcion;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.FacturaRepository;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.repository.ProyectoRepository;
import com.tfg.ong.repository.SubvencionRepository;
import com.tfg.ong.repository.SuscripcionRepository;
import com.tfg.ong.repository.UsuarioRepository;

/**
 * Planes de ONGestión. Plan Pro: sin límites. Plan Básico (gratis): una ONG activa, con límites de miembros,
 * proyectos, subvenciones, movimientos y facturas al año, y sin certificados, Modelo 182 ni registro de actividad.
 * Nunca se borran datos: lo que supera los límites sigue visible, pero no se puede crear más.
 */
@Service
public class PlanService {

    public static final int DIAS_PRUEBA = 14;
    public static final int MAX_ONGS = 1;
    public static final int MAX_MIEMBROS = 3;
    public static final int MAX_PROYECTOS = 2;
    public static final int MAX_SUBVENCIONES = 1;
    public static final int MAX_MOVIMIENTOS_ANIO = 50;
    public static final int MAX_FACTURAS_ANIO = 10;

    public enum Origen { SUSCRIPCION, PRUEBA, GRATIS }

    /** Plan vigente de un administrador. */
    public record Estado(boolean pro, Origen origen, long diasPrueba, LocalDate finPeriodo,
                         boolean cancelaAlFinal, boolean pagoPendiente) {
        public static final Estado SIN_LIMITES = new Estado(true, Origen.SUSCRIPCION, 0, null, false, false);
    }

    /** Uso actual de la ONG activa frente a los límites del plan Básico. */
    public record Uso(int ongs, int miembros, int proyectos, int subvenciones, int movimientos, int facturas) {
    }

    private final SuscripcionRepository suscripcionRepository;
    private final OngRepository ongRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProyectoRepository proyectoRepository;
    private final SubvencionRepository subvencionRepository;
    private final IngresoRepository ingresoRepository;
    private final GastoRepository gastoRepository;
    private final FacturaRepository facturaRepository;

    private Clock reloj = Clock.systemDefaultZone();

    public PlanService(SuscripcionRepository suscripcionRepository, OngRepository ongRepository,
                       UsuarioRepository usuarioRepository, ProyectoRepository proyectoRepository,
                       SubvencionRepository subvencionRepository, IngresoRepository ingresoRepository,
                       GastoRepository gastoRepository, FacturaRepository facturaRepository) {
        this.suscripcionRepository = suscripcionRepository;
        this.ongRepository = ongRepository;
        this.usuarioRepository = usuarioRepository;
        this.proyectoRepository = proyectoRepository;
        this.subvencionRepository = subvencionRepository;
        this.ingresoRepository = ingresoRepository;
        this.gastoRepository = gastoRepository;
        this.facturaRepository = facturaRepository;
    }

    /** Solo para tests. */
    void usarReloj(Clock reloj) {
        this.reloj = reloj;
    }

    private LocalDate hoy() {
        return LocalDate.now(reloj);
    }

    // ---------------------------------------------------------------- plan vigente

    /** Devuelve la suscripción del administrador; si no existe, la crea con la prueba gratuita del plan Pro. */
    public Suscripcion suscripcionDe(Long adminId) {
        return suscripcionRepository.findByUsuarioId(adminId).orElseGet(() -> {
            Suscripcion s = new Suscripcion();
            s.setUsuarioId(adminId);
            s.setPruebaHasta(hoy().plusDays(DIAS_PRUEBA));
            return suscripcionRepository.save(s);
        });
    }

    public static Estado calcular(Suscripcion s, LocalDate hoy) {
        String estado = s.getEstado();
        if ("active".equals(estado) || "trialing".equals(estado)) {
            return new Estado(true, Origen.SUSCRIPCION, 0, s.getPeriodoFin(), s.isCancelarAlFinal(), false);
        }
        // Pago fallido: Stripe sigue reintentando, así que se mantiene el Pro mientras tanto
        if ("past_due".equals(estado)) {
            return new Estado(true, Origen.SUSCRIPCION, 0, s.getPeriodoFin(), s.isCancelarAlFinal(), true);
        }
        long dias = ChronoUnit.DAYS.between(hoy, s.getPruebaHasta());
        if (dias >= 0) {
            return new Estado(true, Origen.PRUEBA, dias, null, false, false);
        }
        return new Estado(false, Origen.GRATIS, 0, null, false, false);
    }

    public Estado estadoDeAdmin(Long adminId) {
        return calcular(suscripcionDe(adminId), hoy());
    }

    private Long adminDeOng(Long ongId) {
        if (ongId == null) {
            return null;
        }
        return ongRepository.findById(ongId).map(Ong::getAdmin).map(Usuario::getId).orElse(null);
    }

    /** Plan que rige para una ONG: el de su administrador. Una ONG sin administrador no tiene límites. */
    public Estado estadoDeOng(Long ongId) {
        Long admin = adminDeOng(ongId);
        return admin == null ? Estado.SIN_LIMITES : estadoDeAdmin(admin);
    }

    // ---------------------------------------------------------------- ONG activa

    public Long ongActiva(Long adminId) {
        List<Ong> ongs = ongRepository.findByAdminId(adminId);
        if (ongs.isEmpty()) {
            return null;
        }
        Long elegida = suscripcionDe(adminId).getOngActivaId();
        if (elegida != null && ongs.stream().anyMatch(o -> elegida.equals(o.getId()))) {
            return elegida;
        }
        return ongs.stream().map(Ong::getId).min(Long::compare).orElse(null);
    }

    public void cambiarOngActiva(Long adminId, Long ongId) {
        boolean suya = ongRepository.findByAdminId(adminId).stream().anyMatch(o -> o.getId().equals(ongId));
        if (!suya) {
            throw new IllegalArgumentException("Esa ONG no es tuya");
        }
        Suscripcion s = suscripcionDe(adminId);
        s.setOngActivaId(ongId);
        suscripcionRepository.save(s);
    }

    // ---------------------------------------------------------------- uso

    public Uso uso(Long adminId) {
        Long activa = ongActiva(adminId);
        int ongs = ongRepository.findByAdminId(adminId).size();
        if (activa == null) {
            return new Uso(ongs, 0, 0, 0, 0, 0);
        }
        return new Uso(ongs, usuarioRepository.findByOngId(activa).size(),
                proyectoRepository.findByOngId(activa).size(), subvencionRepository.findByOngId(activa).size(),
                movimientosDelAnio(activa), facturasDelAnio(activa));
    }

    private int movimientosDelAnio(Long ongId) {
        LocalDate ini = LocalDate.of(hoy().getYear(), 1, 1);
        LocalDate fin = LocalDate.of(hoy().getYear(), 12, 31);
        return ingresoRepository.findByOngIdAndFechaBetween(ongId, ini, fin).size()
                + gastoRepository.findByOngIdAndFechaBetween(ongId, ini, fin).size();
    }

    private int facturasDelAnio(Long ongId) {
        int anio = hoy().getYear();
        return (int) facturaRepository.findByOngId(ongId).stream()
                .filter(f -> f.getFecha() != null && f.getFecha().getYear() == anio).count();
    }

    // ---------------------------------------------------------------- comprobaciones

    /** Cualquier cambio en una ONG: en el plan Básico solo se puede en la ONG activa. */
    public void exigirEscritura(Long ongId) {
        Long admin = adminDeOng(ongId);
        if (admin == null || estadoDeAdmin(admin).pro()) {
            return;
        }
        if (!ongId.equals(ongActiva(admin))) {
            throw new PlanException("Esta ONG está en solo lectura con el plan Básico. "
                    + "Pasa al plan Pro o elígela como ONG activa en «Plan y facturación».");
        }
    }

    private boolean esBasico(Long ongId) {
        Long admin = adminDeOng(ongId);
        return admin != null && !estadoDeAdmin(admin).pro();
    }

    public void exigirCrearMiembro(Long ongId) {
        exigirEscritura(ongId);
        if (esBasico(ongId) && usuarioRepository.findByOngId(ongId).size() >= MAX_MIEMBROS) {
            throw limite("miembros por ONG", MAX_MIEMBROS);
        }
    }

    public void exigirCrearProyecto(Long ongId) {
        exigirEscritura(ongId);
        if (esBasico(ongId) && proyectoRepository.findByOngId(ongId).size() >= MAX_PROYECTOS) {
            throw limite("proyectos", MAX_PROYECTOS);
        }
    }

    public void exigirCrearSubvencion(Long ongId) {
        exigirEscritura(ongId);
        if (esBasico(ongId) && subvencionRepository.findByOngId(ongId).size() >= MAX_SUBVENCIONES) {
            throw limite("subvenciones", MAX_SUBVENCIONES);
        }
    }

    public void exigirCrearMovimiento(Long ongId) {
        exigirEscritura(ongId);
        if (esBasico(ongId) && movimientosDelAnio(ongId) >= MAX_MOVIMIENTOS_ANIO) {
            throw limite("ingresos y gastos al año", MAX_MOVIMIENTOS_ANIO);
        }
    }

    public void exigirCrearFactura(Long ongId) {
        exigirEscritura(ongId);
        if (esBasico(ongId) && facturasDelAnio(ongId) >= MAX_FACTURAS_ANIO) {
            throw limite("facturas al año", MAX_FACTURAS_ANIO);
        }
    }

    public void exigirNuevaOng(Long adminId) {
        if (!estadoDeAdmin(adminId).pro() && ongRepository.findByAdminId(adminId).size() >= MAX_ONGS) {
            throw limite("ONG", MAX_ONGS);
        }
    }

    /** Certificados, Modelo 182 y registro de actividad. */
    public void exigirPro(Long ongId) {
        if (esBasico(ongId)) {
            throw new PlanException("Esta función es del plan Pro. Suscríbete en «Plan y facturación» para usarla.");
        }
    }

    private static PlanException limite(String que, int maximo) {
        return new PlanException("Has alcanzado el límite del plan Básico (" + maximo + " " + que
                + "). Pasa al plan Pro para quitar los límites.");
    }
}
