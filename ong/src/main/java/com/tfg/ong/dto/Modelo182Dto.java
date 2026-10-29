package com.tfg.ong.dto;

import java.math.BigDecimal;
import java.util.List;

/** Vista previa del Modelo 182 de una ONG y un año: lo que irá en el fichero y lo que aún falta por completar. */
public record Modelo182Dto(
        int anio,
        DeclaranteDTO declarante,
        List<RegistroDTO> registros,
        BigDecimal importeTotal,
        List<String> problemas,
        boolean listo) {

    public record DeclaranteDTO(String nif, String nombre, String telefono, String contacto, String justificante) {
    }

    public record RegistroDTO(
            Long usuarioId,
            String nombre,
            String nif,
            String codigoPostal,
            String provincia,
            String naturaleza,
            String clave,
            BigDecimal porcentaje,
            BigDecimal importe,
            int numDonaciones,
            boolean recurrente,
            List<String> problemas) {
    }
}
