package com.tfg.ong.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.tfg.ong.model.RegistroAuditoria;
import com.tfg.ong.repository.AuditoriaRepository;
import com.tfg.ong.repository.UsuarioRepository;
import com.tfg.ong.security.AuthUser;

/** Registro de actividad: guarda quién ha creado, editado o eliminado datos y permite consultarlo. */
@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);
    private static final Set<String> ENTIDADES = Set.of(AuditoriaDescripciones.INGRESO, AuditoriaDescripciones.GASTO,
            AuditoriaDescripciones.FACTURA, AuditoriaDescripciones.PROYECTO, AuditoriaDescripciones.SUBVENCION);
    private static final Set<String> ACCIONES = Set.of(RegistroAuditoria.CREAR, RegistroAuditoria.EDITAR, RegistroAuditoria.ELIMINAR);

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository, UsuarioRepository usuarioRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** Página de resultados con el total para poder paginar en el cliente. */
    public record Pagina(List<RegistroAuditoria> contenido, long total, int pagina, int paginas) {
    }

    /**
     * Guarda una línea del registro con el usuario autenticado. Nunca lanza excepciones: si falla el registro
     * no debe romperse la operación que se estaba auditando.
     */
    public void registrar(Long ongId, String entidad, Long entidadId, String accion, String descripcion) {
        try {
            RegistroAuditoria r = new RegistroAuditoria();
            r.setFecha(LocalDateTime.now());
            r.setOngId(ongId);
            r.setEntidad(entidad);
            r.setEntidadId(entidadId);
            r.setAccion(accion);
            r.setDescripcion(descripcion == null ? null : descripcion.length() > 600 ? descripcion.substring(0, 597) + "..." : descripcion);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof AuthUser usuario) {
                r.setUsuarioId(usuario.id());
                r.setUsuarioRol(usuario.rol() == null ? null : usuario.rol().name());
                r.setUsuarioNombre(usuarioRepository.findById(usuario.id()).map(u -> u.getNombre()).orElse(usuario.email()));
            } else {
                r.setUsuarioNombre("Sistema");
            }
            auditoriaRepository.save(r);
        } catch (Exception e) {
            log.warn("No se pudo guardar el registro de actividad ({} {} {}): {}", accion, entidad, entidadId, e.getMessage());
        }
    }

    private static String valido(String valor, Set<String> permitidos, String nombre) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String v = valor.trim().toUpperCase();
        if (!permitidos.contains(v)) {
            throw new IllegalArgumentException(nombre + " no válido");
        }
        return v;
    }

    public Pagina buscar(Long idOng, String entidad, String accion, LocalDate desde, LocalDate hasta, int pagina, int tamano) {
        int tam = Math.max(1, Math.min(tamano, 100));
        int pag = Math.max(0, pagina);
        Page<RegistroAuditoria> p = auditoriaRepository.buscar(idOng, valido(entidad, ENTIDADES, "El tipo"),
                valido(accion, ACCIONES, "La acción"), desde == null ? null : desde.atStartOfDay(),
                hasta == null ? null : hasta.plusDays(1).atStartOfDay(), PageRequest.of(pag, tam));
        return new Pagina(p.getContent(), p.getTotalElements(), p.getNumber(), p.getTotalPages());
    }
}
