package com.tfg.ong.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tfg.ong.model.MiembroGobierno;
import com.tfg.ong.model.Ong;
import com.tfg.ong.repository.MiembroGobiernoRepository;
import com.tfg.ong.repository.OngRepository;

/** Altas, cambios y bajas de las personas del órgano de gobierno y del equipo de dirección. */
@Service
public class GobiernoService {

    private final MiembroGobiernoRepository repository;
    private final OngRepository ongRepository;

    public GobiernoService(MiembroGobiernoRepository repository, OngRepository ongRepository) {
        this.repository = repository;
        this.ongRepository = ongRepository;
    }

    public List<MiembroGobierno> listar(Long idOng) {
        return repository.findByOngIdOrderByOrganoAscOrdenAscIdAsc(idOng);
    }

    public MiembroGobierno crear(Long idOng, MiembroGobierno datos) {
        Ong ong = ongRepository.findById(idOng).orElseThrow(() -> new IllegalArgumentException("La ONG no existe"));
        MiembroGobierno nuevo = new MiembroGobierno();
        nuevo.setOng(ong);
        aplicar(nuevo, datos);
        return repository.save(nuevo);
    }

    public MiembroGobierno actualizar(Long id, MiembroGobierno datos) {
        MiembroGobierno actual = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("La persona no existe"));
        aplicar(actual, datos);
        return repository.save(actual);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    /** Id de la ONG a la que pertenece una persona (null si no existe). */
    public Long ongDe(Long id) {
        return repository.findById(id).map(m -> m.getOng().getId()).orElse(null);
    }

    private static void aplicar(MiembroGobierno destino, MiembroGobierno datos) {
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (datos.getCargo() == null || datos.getCargo().isBlank()) {
            throw new IllegalArgumentException("El cargo es obligatorio");
        }
        String organo = datos.getOrgano() == null ? MiembroGobierno.GOBIERNO : datos.getOrgano().trim().toUpperCase();
        if (!MiembroGobierno.GOBIERNO.equals(organo) && !MiembroGobierno.DIRECCION.equals(organo)) {
            throw new IllegalArgumentException("El órgano debe ser de gobierno o de dirección");
        }
        if (datos.getNombre().trim().length() > 120 || datos.getCargo().trim().length() > 80) {
            throw new IllegalArgumentException("El nombre o el cargo son demasiado largos");
        }
        if (datos.getDescripcion() != null && datos.getDescripcion().length() > 500) {
            throw new IllegalArgumentException("La descripción no puede superar 500 caracteres");
        }
        destino.setNombre(datos.getNombre().trim());
        destino.setCargo(datos.getCargo().trim());
        destino.setOrgano(organo);
        destino.setFechaNombramiento(datos.getFechaNombramiento());
        destino.setDescripcion(datos.getDescripcion() == null || datos.getDescripcion().isBlank() ? null : datos.getDescripcion().trim());
        destino.setOrden(datos.getOrden());
    }
}
