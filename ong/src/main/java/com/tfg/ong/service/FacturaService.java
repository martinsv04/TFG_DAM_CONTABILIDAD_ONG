package com.tfg.ong.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.tfg.ong.model.DetalleFactura;
import com.tfg.ong.model.Factura;
import com.tfg.ong.model.Gasto;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.repository.FacturaRepository;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.IngresoRepository;

import jakarta.transaction.Transactional;

@Service
public class FacturaService {

    /** Tipos de IVA que se pueden aplicar (en %). El 0 sirve para operaciones exentas o no sujetas. */
    public static final Set<Integer> TIPOS_IVA = Set.of(0, 4, 10, 21);

    private final FacturaRepository facturaRepository;

    private final IngresoRepository ingresoRepository;

    private final GastoRepository gastoRepository;

    public FacturaService(FacturaRepository facturaRepository, IngresoRepository ingresoRepository, GastoRepository gastoRepository) {
        this.facturaRepository = facturaRepository;
        this.ingresoRepository = ingresoRepository;
        this.gastoRepository = gastoRepository;
    }

      public List<Factura> getAllFacturas() {
        return facturaRepository.findAll();
    }

    public Factura getFacturaById(Long id) {
        Optional<Factura> factura = facturaRepository.findById(id);
        return factura.orElse(null);
    }

    public Factura createFactura(Factura factura) {
        if (factura.getDetalles() != null) {
            factura.getDetalles().forEach(detalle -> detalle.setFactura(factura));
        }
        return facturaRepository.save(factura);
    }

    public Factura updateFactura(Long id, Factura factura) {
        factura.setId(id);
        if (factura.getDetalles() != null) {
            factura.getDetalles().forEach(detalle -> detalle.setFactura(factura));
        }
        return facturaRepository.save(factura);
    }


    /**
     * Siguiente número de factura de una serie: «F-AAAA-NNNN» para los ingresos y «G-AAAA-NNNN» para los gastos,
     * correlativo dentro de cada ONG y año (las facturas deben numerarse sin saltos dentro de cada serie).
     */
    String siguienteNumero(Long idOng, String letra, int anio) {
        String prefijo = letra + "-" + anio + "-";
        int maximo = 0;
        for (String numero : facturaRepository.findNumerosByOngIdAndPrefijo(idOng, prefijo)) {
            String resto = numero.substring(prefijo.length());
            if (resto.matches("\\d{1,9}")) {
                maximo = Math.max(maximo, Integer.parseInt(resto));
            }
        }
        return prefijo + String.format("%04d", maximo + 1);
    }

    /** Genera la factura sin IVA (0 %). */
    public Factura generarDesdeMovimiento(Long idOng, String tipo, Long idMovimiento) {
        return generarDesdeMovimiento(idOng, tipo, idMovimiento, null);
    }

    /**
     * Genera la factura de un movimiento (ingreso o gasto) ya registrado.
     * El número, la fecha y el importe los fija el servidor a partir del propio movimiento.
     * El importe del movimiento se entiende con el IVA incluido: la base imponible se calcula quitándolo.
     *
     * @param tipo "INGRESO" o "GASTO"
     * @param iva  tipo de IVA en % (0, 4, 10 o 21); si es null se usa 0
     */
    @Transactional
    public Factura generarDesdeMovimiento(Long idOng, String tipo, Long idMovimiento, BigDecimal iva) {
        BigDecimal tipoIva = iva == null ? BigDecimal.ZERO : iva;
        if (tipoIva.stripTrailingZeros().scale() > 0 || !TIPOS_IVA.contains(tipoIva.intValue())) {
            throw new IllegalArgumentException("El IVA debe ser 0, 4, 10 o 21 %");
        }
        if (tipo == null || idMovimiento == null) {
            throw new IllegalArgumentException("Hay que indicar el tipo de movimiento y cuál es");
        }

        Factura factura = new Factura();
        Ong ong;
        BigDecimal monto;
        String descripcion;
        String letra;

        switch (tipo.toUpperCase()) {
            case "INGRESO" -> {
                Ingreso ingreso = ingresoRepository.findById(idMovimiento)
                        .orElseThrow(() -> new IllegalArgumentException("El ingreso no existe"));
                if (ingreso.getOng() == null || !idOng.equals(ingreso.getOng().getId())) {
                    throw new IllegalArgumentException("El ingreso no pertenece a esta ONG");
                }
                if (facturaRepository.existsByIngresoId(idMovimiento)) {
                    throw new IllegalArgumentException("Este ingreso ya tiene una factura");
                }
                factura.setIngreso(ingreso);
                factura.setUsuario(ingreso.getUsuario());
                ong = ingreso.getOng();
                monto = ingreso.getMonto();
                descripcion = ingreso.getDescripcion();
                letra = "F";
            }
            case "GASTO" -> {
                Gasto gasto = gastoRepository.findById(idMovimiento)
                        .orElseThrow(() -> new IllegalArgumentException("El gasto no existe"));
                if (gasto.getOng() == null || !idOng.equals(gasto.getOng().getId())) {
                    throw new IllegalArgumentException("El gasto no pertenece a esta ONG");
                }
                if (facturaRepository.existsByGastoId(idMovimiento)) {
                    throw new IllegalArgumentException("Este gasto ya tiene una factura");
                }
                factura.setGasto(gasto);
                ong = gasto.getOng();
                monto = gasto.getMonto();
                descripcion = gasto.getDescripcion();
                letra = "G";
            }
            default -> throw new IllegalArgumentException("El tipo debe ser INGRESO o GASTO");
        }

        if (monto == null) {
            throw new IllegalArgumentException("El movimiento no tiene importe");
        }

        LocalDate hoy = LocalDate.now();
        BigDecimal total = monto.setScale(2, RoundingMode.HALF_UP);
        BigDecimal divisor = BigDecimal.ONE.add(tipoIva.movePointLeft(2));
        BigDecimal base = total.divide(divisor, 2, RoundingMode.HALF_UP);

        factura.setNumero(siguienteNumero(idOng, letra, hoy.getYear()));
        factura.setFecha(hoy);
        factura.setOng(ong);
        factura.setTotal(total);

        DetalleFactura detalle = new DetalleFactura();
        detalle.setFactura(factura);
        detalle.setDescripcion(descripcion == null || descripcion.isBlank() ? "Sin descripción" : descripcion);
        detalle.setCantidad(1);
        detalle.setPrecio(base);
        detalle.setIva(tipoIva.setScale(2, RoundingMode.HALF_UP));
        factura.setDetalles(new ArrayList<>(List.of(detalle)));

        return facturaRepository.save(factura);
    }

    public void deleteFactura(Long id) {
        facturaRepository.deleteById(id);
    }

}
