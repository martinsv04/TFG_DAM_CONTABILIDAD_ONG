package com.tfg.ong.service;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.tfg.ong.model.Factura;
import com.tfg.ong.model.Gasto;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.FacturaRepository;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.security.AuthUser;

/**
 * Aplica los límites del plan antes de que los servicios creen o modifiquen datos, y bloquea las funciones
 * del plan Pro en el plan Básico. Se engancha a los servicios, así que no hace falta tocar los controladores.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PlanAspect {

    private final PlanService plan;
    private final IngresoRepository ingresoRepository;
    private final GastoRepository gastoRepository;
    private final FacturaRepository facturaRepository;

    public PlanAspect(PlanService plan, IngresoRepository ingresoRepository, GastoRepository gastoRepository,
                      FacturaRepository facturaRepository) {
        this.plan = plan;
        this.ingresoRepository = ingresoRepository;
        this.gastoRepository = gastoRepository;
        this.facturaRepository = facturaRepository;
    }

    @Before("execution(public * com.tfg.ong.service.IngresoService.*(..))"
            + " || execution(public * com.tfg.ong.service.GastoService.*(..))"
            + " || execution(public * com.tfg.ong.service.FacturaService.*(..))"
            + " || execution(public * com.tfg.ong.service.ProyectoService.*(..))"
            + " || execution(public * com.tfg.ong.service.SubvencionService.*(..))"
            + " || execution(public * com.tfg.ong.service.UsuarioService.createUsuario(..))"
            + " || execution(public * com.tfg.ong.service.OngService.createOng(..))"
            + " || execution(public * com.tfg.ong.service.CertificadoService.*(..))"
            + " || execution(public * com.tfg.ong.service.Modelo182Service.*(..))"
            + " || execution(public * com.tfg.ong.service.AuditoriaService.buscar(..))")
    public void controlar(JoinPoint jp) {
        String clave = jp.getSignature().getDeclaringType().getSimpleName() + "." + jp.getSignature().getName();
        Object[] a = jp.getArgs();

        switch (clave) {
            case "IngresoService.createIngreso" -> {
                // Las donaciones de los donantes no se frenan por el plan de la ONG
                if (!esDonante()) {
                    plan.exigirCrearMovimiento(ongId(((Ingreso) a[0]).getOng()));
                }
            }
            case "IngresoService.updateIngresos", "IngresoService.deleteIngreso" ->
                    ingresoRepository.findById((Long) a[0]).ifPresent(i -> plan.exigirEscritura(ongId(i.getOng())));
            case "GastoService.createGasto" -> plan.exigirCrearMovimiento(ongId(((Gasto) a[0]).getOng()));
            case "GastoService.updateGastos", "GastoService.deleteGasto" ->
                    gastoRepository.findById((Long) a[0]).ifPresent(g -> plan.exigirEscritura(ongId(g.getOng())));
            case "FacturaService.generarDesdeMovimiento" -> plan.exigirCrearFactura((Long) a[0]);
            case "FacturaService.createFactura" -> plan.exigirCrearFactura(ongId(((Factura) a[0]).getOng()));
            case "FacturaService.updateFactura", "FacturaService.deleteFactura" ->
                    facturaRepository.findById((Long) a[0]).ifPresent(f -> plan.exigirEscritura(ongId(f.getOng())));
            case "ProyectoService.crear" -> plan.exigirCrearProyecto((Long) a[0]);
            case "SubvencionService.crear" -> plan.exigirCrearSubvencion((Long) a[0]);
            case "ProyectoService.actualizar", "ProyectoService.eliminar",
                 "SubvencionService.actualizar", "SubvencionService.eliminar" -> plan.exigirEscritura((Long) a[0]);
            case "UsuarioService.createUsuario" -> {
                Long ong = ongId(((Usuario) a[0]).getOng());
                if (ong != null) {
                    plan.exigirCrearMiembro(ong);
                }
            }
            case "OngService.createOng" -> {
                Usuario admin = ((Ong) a[0]).getAdmin();
                if (admin != null && admin.getId() != null) {
                    plan.exigirNuevaOng(admin.getId());
                }
            }
            case "Modelo182Service.previsualizar", "Modelo182Service.fichero", "AuditoriaService.buscar" ->
                    plan.exigirPro((Long) a[0]);
            default -> {
                // Certificados: los donantes siempre pueden consultar los suyos
                if (clave.startsWith("CertificadoService.") && !esDonante() && a.length > 0 && a[0] instanceof Long ongId) {
                    plan.exigirPro(ongId);
                }
            }
        }
    }

    private static Long ongId(Ong ong) {
        return ong == null ? null : ong.getId();
    }

    private static boolean esDonante() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof AuthUser u && u.rol() == Rol.DONANTE;
    }
}
