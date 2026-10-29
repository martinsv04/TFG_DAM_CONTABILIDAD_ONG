package com.tfg.ong.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tfg.ong.model.Gasto;
import com.tfg.ong.model.Proyecto;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.ProyectoRepository;

@Service
public class GastoService {

    private final GastoRepository gastoRepository;

    private final ProyectoRepository proyectoRepository;

    public GastoService(GastoRepository gastoRepository, ProyectoRepository proyectoRepository) {
        this.gastoRepository = gastoRepository;
        this.proyectoRepository = proyectoRepository;
    }

    public List<Gasto> getAllGastos() {
        return gastoRepository.findAll();
    }

    public Gasto getGastoById(Long id) {
        Optional<Gasto> gasto = gastoRepository.findById(id);
        return gasto.orElse(null);
    }

    public Gasto createGasto(Gasto gasto) {
        gasto.setCuenta(CuentaContable.normalizar(gasto.getCuenta()));
        // El proyecto (opcional) debe ser de la misma ONG que el gasto
        if (gasto.getProyecto() != null) {
            Long idOng = gasto.getOng() != null ? gasto.getOng().getId() : null;
            Proyecto proyecto = proyectoRepository.findById(gasto.getProyecto().getId())
                    .filter(p -> p.getOng() != null && p.getOng().getId().equals(idOng))
                    .orElseThrow(() -> new IllegalArgumentException("El proyecto no existe en esta ONG"));
            gasto.setProyecto(proyecto);
        }
        return gastoRepository.save(gasto);
    }

    /**
     * Edita un gasto existente. Solo cambian los datos editables (descripción, importe, fecha, categoría, cuenta
     * y proyecto); la ONG a la que pertenece no se toca nunca.
     */
    public Gasto updateGastos(Long id, Gasto datos) {
        Gasto actual = gastoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El gasto no existe"));

        if (datos.getDescripcion() == null || datos.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
        if (datos.getMonto() == null || datos.getMonto().signum() <= 0) {
            throw new IllegalArgumentException("El importe debe ser mayor que cero");
        }
        if (datos.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (datos.getCategoria() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }

        Proyecto proyecto = null;
        if (datos.getProyecto() != null && datos.getProyecto().getId() != null) {
            Long idOng = actual.getOng() != null ? actual.getOng().getId() : null;
            proyecto = proyectoRepository.findById(datos.getProyecto().getId())
                    .filter(p -> p.getOng() != null && p.getOng().getId().equals(idOng))
                    .orElseThrow(() -> new IllegalArgumentException("El proyecto no existe en esta ONG"));
        }

        actual.setDescripcion(datos.getDescripcion().trim());
        actual.setMonto(datos.getMonto());
        actual.setFecha(datos.getFecha());
        actual.setCategoria(datos.getCategoria());
        actual.setCuenta(CuentaContable.normalizar(datos.getCuenta()));
        actual.setProyecto(proyecto);
        return gastoRepository.save(actual);
    }

    public void deleteGasto(Long id) {
        gastoRepository.deleteById(id);
    }

}
