package com.tfg.ong.service;

import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.tfg.ong.model.RegistroAuditoria;

/**
 * Registra en el registro de actividad las altas, cambios y borrados de ingresos, gastos, facturas,
 * proyectos y subvenciones. Se engancha a los servicios, así que no hace falta tocar los controladores.
 */
@Aspect
@Component
public class AuditoriaAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaAspect.class);

    /** Método auditado -> {tipo de elemento, acción}. */
    private static final Map<String, String[]> OPERACIONES = Map.ofEntries(
            Map.entry("IngresoService.createIngreso", new String[]{AuditoriaDescripciones.INGRESO, RegistroAuditoria.CREAR}),
            Map.entry("IngresoService.updateIngresos", new String[]{AuditoriaDescripciones.INGRESO, RegistroAuditoria.EDITAR}),
            Map.entry("IngresoService.deleteIngreso", new String[]{AuditoriaDescripciones.INGRESO, RegistroAuditoria.ELIMINAR}),
            Map.entry("GastoService.createGasto", new String[]{AuditoriaDescripciones.GASTO, RegistroAuditoria.CREAR}),
            Map.entry("GastoService.updateGastos", new String[]{AuditoriaDescripciones.GASTO, RegistroAuditoria.EDITAR}),
            Map.entry("GastoService.deleteGasto", new String[]{AuditoriaDescripciones.GASTO, RegistroAuditoria.ELIMINAR}),
            Map.entry("FacturaService.createFactura", new String[]{AuditoriaDescripciones.FACTURA, RegistroAuditoria.CREAR}),
            Map.entry("FacturaService.generarDesdeMovimiento", new String[]{AuditoriaDescripciones.FACTURA, RegistroAuditoria.CREAR}),
            Map.entry("FacturaService.updateFactura", new String[]{AuditoriaDescripciones.FACTURA, RegistroAuditoria.EDITAR}),
            Map.entry("FacturaService.deleteFactura", new String[]{AuditoriaDescripciones.FACTURA, RegistroAuditoria.ELIMINAR}),
            Map.entry("ProyectoService.crear", new String[]{AuditoriaDescripciones.PROYECTO, RegistroAuditoria.CREAR}),
            Map.entry("ProyectoService.actualizar", new String[]{AuditoriaDescripciones.PROYECTO, RegistroAuditoria.EDITAR}),
            Map.entry("ProyectoService.eliminar", new String[]{AuditoriaDescripciones.PROYECTO, RegistroAuditoria.ELIMINAR}),
            Map.entry("SubvencionService.crear", new String[]{AuditoriaDescripciones.SUBVENCION, RegistroAuditoria.CREAR}),
            Map.entry("SubvencionService.actualizar", new String[]{AuditoriaDescripciones.SUBVENCION, RegistroAuditoria.EDITAR}),
            Map.entry("SubvencionService.eliminar", new String[]{AuditoriaDescripciones.SUBVENCION, RegistroAuditoria.ELIMINAR}));

    private final AuditoriaService auditoriaService;

    public AuditoriaAspect(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @Around("execution(public * com.tfg.ong.service.IngresoService.*(..))"
            + " || execution(public * com.tfg.ong.service.GastoService.*(..))"
            + " || execution(public * com.tfg.ong.service.FacturaService.*(..))"
            + " || execution(public * com.tfg.ong.service.ProyectoService.*(..))"
            + " || execution(public * com.tfg.ong.service.SubvencionService.*(..))")
    public Object auditar(ProceedingJoinPoint pjp) throws Throwable {
        String clave = pjp.getSignature().getDeclaringType().getSimpleName() + "." + pjp.getSignature().getName();
        String[] operacion = OPERACIONES.get(clave);
        if (operacion == null) {
            return pjp.proceed();
        }
        String entidad = operacion[0];
        String accion = operacion[1];

        // En ediciones y borrados se lee el elemento antes de tocarlo, para poder mostrar "antes → ahora"
        Object antes = null;
        if (!RegistroAuditoria.CREAR.equals(accion)) {
            try {
                antes = cargar(pjp.getTarget(), entidad, pjp.getArgs());
            } catch (Exception e) {
                log.debug("No se pudo leer el elemento previo a {}: {}", clave, e.getMessage());
            }
        }

        // El elemento previo puede ser la misma instancia que luego se modifica: se guarda su texto ya
        String previo = antes != null ? AuditoriaDescripciones.describir(antes) : null;

        Object resultado = pjp.proceed();

        try {
            Object despues = RegistroAuditoria.ELIMINAR.equals(accion) ? null : resultado;
            Object referencia = despues != null ? despues : antes;
            String texto = AuditoriaDescripciones.describir(referencia);
            if (RegistroAuditoria.EDITAR.equals(accion) && antes != null && despues != null) {
                if (previo != null && !previo.equals(texto)) {
                    texto = "Antes: " + previo + " → Ahora: " + texto;
                }
            }
            Long ongId = AuditoriaDescripciones.ongId(referencia);
            if (ongId == null) {
                ongId = AuditoriaDescripciones.ongId(antes);
            }
            Long entidadId = AuditoriaDescripciones.id(referencia);
            if (entidadId == null && pjp.getArgs().length > 0 && pjp.getArgs()[pjp.getArgs().length - 1] instanceof Long) {
                entidadId = (Long) pjp.getArgs()[pjp.getArgs().length - 1];
            }
            auditoriaService.registrar(ongId, entidad, entidadId, accion, texto);
        } catch (Exception e) {
            log.warn("No se pudo registrar la actividad de {}: {}", clave, e.getMessage());
        }
        return resultado;
    }

    private static Object cargar(Object servicio, String entidad, Object[] args) {
        if (servicio instanceof IngresoService s) return s.getIngresoById((Long) args[0]);
        if (servicio instanceof GastoService s) return s.getGastoById((Long) args[0]);
        if (servicio instanceof FacturaService s) return s.getFacturaById((Long) args[0]);
        if (servicio instanceof ProyectoService s) return s.obtener((Long) args[0], (Long) args[1]);
        if (servicio instanceof SubvencionService s) return s.obtener((Long) args[0], (Long) args[1]);
        return null;
    }
}
