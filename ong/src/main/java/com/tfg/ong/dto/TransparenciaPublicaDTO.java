package com.tfg.ong.dto;

import java.time.LocalDate;
import java.util.List;

import com.tfg.ong.model.MiembroGobierno;

/** Contenido de la página pública de transparencia: datos de la entidad, su gobierno y las cifras del año. */
public record TransparenciaPublicaDTO(
        Long ongId,
        String nombre,
        String descripcion,
        String direccion,
        String email,
        String telefono,
        String nif,
        LocalDate fechaCreacion,
        List<MiembroGobierno> gobierno,
        TransparenciaDTO datos) {
}
