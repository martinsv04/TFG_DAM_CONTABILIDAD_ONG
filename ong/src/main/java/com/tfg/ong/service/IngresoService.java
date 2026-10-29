package com.tfg.ong.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Proyecto;
import com.tfg.ong.model.Subvencion;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.ProyectoRepository;
import com.tfg.ong.repository.SubvencionRepository;

@Service
public class IngresoService {

    private final IngresoRepository ingresoRepository;

    private final ProyectoRepository proyectoRepository;

    private final SubvencionRepository subvencionRepository;

    public IngresoService(IngresoRepository ingresoRepository, ProyectoRepository proyectoRepository,
                          SubvencionRepository subvencionRepository) {
        this.ingresoRepository = ingresoRepository;
        this.proyectoRepository = proyectoRepository;
        this.subvencionRepository = subvencionRepository;
    }

    public List<Ingreso> getAllIngresos() {
        return ingresoRepository.findAll();
    }

    public Ingreso getIngresoById(Long id) {
        Optional<Ingreso> ingreso = ingresoRepository.findById(id);
        return ingreso.orElse(null);
    }

    public Ingreso createIngreso(Ingreso ingreso) {
        ingreso.setCuenta(CuentaContable.normalizar(ingreso.getCuenta()));
        // El proyecto y la subvención (opcionales) deben ser de la misma ONG que el ingreso
        Long idOng = ingreso.getOng() != null ? ingreso.getOng().getId() : null;
        if (ingreso.getProyecto() != null) {
            Proyecto proyecto = proyectoRepository.findById(ingreso.getProyecto().getId())
                    .filter(p -> p.getOng() != null && p.getOng().getId().equals(idOng))
                    .orElseThrow(() -> new IllegalArgumentException("El proyecto no existe en esta ONG"));
            ingreso.setProyecto(proyecto);
        }
        if (ingreso.getSubvencion() != null) {
            Subvencion subvencion = subvencionRepository.findById(ingreso.getSubvencion().getId())
                    .filter(s -> s.getOng() != null && s.getOng().getId().equals(idOng))
                    .orElseThrow(() -> new IllegalArgumentException("La subvención no existe en esta ONG"));
            ingreso.setSubvencion(subvencion);
        }
        return ingresoRepository.save(ingreso);
    }

    /**
     * Edita un ingreso existente. Solo cambian los datos editables (descripción, importe, fecha, tipo, cuenta,
     * proyecto y subvención); la ONG y la persona que donó no se tocan nunca.
     */
    public Ingreso updateIngresos(Long id, Ingreso datos) {
        Ingreso actual = ingresoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El ingreso no existe"));

        if (datos.getDescripcion() == null || datos.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
        if (datos.getMonto() == null || datos.getMonto().signum() <= 0) {
            throw new IllegalArgumentException("El importe debe ser mayor que cero");
        }
        if (datos.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (datos.getTipo() == null || datos.getTipo().isBlank()) {
            throw new IllegalArgumentException("El tipo de ingreso es obligatorio");
        }

        Long idOng = actual.getOng() != null ? actual.getOng().getId() : null;
        Proyecto proyecto = null;
        if (datos.getProyecto() != null && datos.getProyecto().getId() != null) {
            proyecto = proyectoRepository.findById(datos.getProyecto().getId())
                    .filter(p -> p.getOng() != null && p.getOng().getId().equals(idOng))
                    .orElseThrow(() -> new IllegalArgumentException("El proyecto no existe en esta ONG"));
        }
        Subvencion subvencion = null;
        if (datos.getSubvencion() != null && datos.getSubvencion().getId() != null) {
            subvencion = subvencionRepository.findById(datos.getSubvencion().getId())
                    .filter(sv -> sv.getOng() != null && sv.getOng().getId().equals(idOng))
                    .orElseThrow(() -> new IllegalArgumentException("La subvención no existe en esta ONG"));
        }

        actual.setDescripcion(datos.getDescripcion().trim());
        actual.setMonto(datos.getMonto());
        actual.setFecha(datos.getFecha());
        actual.setTipo(datos.getTipo());
        actual.setCuenta(CuentaContable.normalizar(datos.getCuenta()));
        actual.setProyecto(proyecto);
        actual.setSubvencion(subvencion);
        return ingresoRepository.save(actual);
    }

    public void deleteIngreso(Long id) {
        ingresoRepository.deleteById(id);
    }

}
