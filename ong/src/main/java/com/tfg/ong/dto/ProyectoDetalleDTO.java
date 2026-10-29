package com.tfg.ong.dto;

import java.util.List;

public record ProyectoDetalleDTO(ProyectoDTO proyecto, List<MovimientoDTO> gastos, List<MovimientoDTO> ingresos,
                                 List<SubvencionDTO> subvenciones) {
}
