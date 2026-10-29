package com.tfg.ong.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.springframework.stereotype.Service;

import com.tfg.ong.dto.TransparenciaDTO;
import com.tfg.ong.dto.TransparenciaDTO.Parte;
import com.tfg.ong.dto.TransparenciaPublicaDTO;
import com.tfg.ong.model.AreaGasto;
import com.tfg.ong.model.Gasto;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.MiembroGobiernoRepository;
import com.tfg.ong.repository.OngRepository;

/**
 * Calcula las cifras del informe de transparencia a partir de los ingresos y gastos ya registrados.
 * Lo que no esté clasificado se deduce (ver origenDe, financiadorDe y areaDe) y se cuenta aparte para
 * poder avisar de cuántos movimientos conviene revisar.
 */
@Service
public class TransparenciaService {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final IngresoRepository ingresoRepository;
    private final GastoRepository gastoRepository;
    private final OngRepository ongRepository;
    private final MiembroGobiernoRepository gobiernoRepository;

    public TransparenciaService(IngresoRepository ingresoRepository, GastoRepository gastoRepository,
                                OngRepository ongRepository, MiembroGobiernoRepository gobiernoRepository) {
        this.ingresoRepository = ingresoRepository;
        this.gastoRepository = gastoRepository;
        this.ongRepository = ongRepository;
        this.gobiernoRepository = gobiernoRepository;
    }

    // ---------- Clasificación de cada movimiento ----------

    /** PUBLICO o PRIVADO: el indicado o, si falta, público cuando viene de una subvención y privado en el resto. */
    public static String origenDe(Ingreso i) {
        String o = i.getOrigen();
        if (Clasificacion.PUBLICO.equals(o) || Clasificacion.PRIVADO.equals(o)) {
            return o;
        }
        if (i.getSubvencion() != null || "SUBVENCIÓN".equalsIgnoreCase(i.getTipo())) {
            return Clasificacion.PUBLICO;
        }
        return Clasificacion.PRIVADO;
    }

    public static boolean origenIndicado(Ingreso i) {
        return Clasificacion.PUBLICO.equals(i.getOrigen()) || Clasificacion.PRIVADO.equals(i.getOrigen());
    }

    /** Quién aporta el ingreso: el financiador indicado, el de su subvención o un nombre genérico según el tipo. */
    public static String financiadorDe(Ingreso i) {
        if (i.getFinanciador() != null && !i.getFinanciador().isBlank()) {
            return i.getFinanciador().trim();
        }
        if (i.getSubvencion() != null && i.getSubvencion().getFinanciador() != null
                && !i.getSubvencion().getFinanciador().isBlank()) {
            return i.getSubvencion().getFinanciador().trim();
        }
        String tipo = i.getTipo() == null ? "" : i.getTipo().trim().toUpperCase();
        return switch (tipo) {
            case "SUBVENCIÓN" -> "Subvenciones sin financiador indicado";
            case "DONACIÓN" -> "Donaciones de particulares";
            case "CUOTAS" -> "Cuotas de socios";
            case "EVENTOS" -> "Eventos";
            case "VENTAS" -> "Ventas y servicios";
            default -> "Otros ingresos";
        };
    }

    /** Destino del gasto: el indicado o, si falta, misión cuando está imputado a un proyecto y administración si no. */
    public static AreaGasto areaDe(Gasto g) {
        if (g.getArea() != null) {
            return g.getArea();
        }
        return g.getProyecto() != null ? AreaGasto.MISION : AreaGasto.ADMINISTRACION;
    }

    static String nombreArea(AreaGasto a) {
        return switch (a) {
            case MISION -> "Misión (actividades)";
            case ADMINISTRACION -> "Administración";
            case CAPTACION -> "Captación de fondos";
        };
    }

    // ---------- Cálculo ----------

    private static BigDecimal importe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    static BigDecimal porcentaje(BigDecimal parte, BigDecimal total) {
        if (total == null || total.signum() <= 0) {
            return BigDecimal.ZERO.setScale(1);
        }
        return parte.multiply(CIEN).divide(total, 1, RoundingMode.HALF_UP);
    }

    private static List<Parte> partes(Map<String, BigDecimal> importes, Map<String, String> nombres, BigDecimal total) {
        List<Parte> lista = new ArrayList<>();
        importes.forEach((clave, valor) ->
                lista.add(new Parte(clave, nombres.getOrDefault(clave, clave), valor, porcentaje(valor, total))));
        lista.sort(Comparator.comparing(Parte::importe).reversed());
        return lista;
    }

    private static void sumar(Map<String, BigDecimal> mapa, Map<String, String> nombres, String clave, String nombre, BigDecimal valor) {
        mapa.merge(clave, valor, BigDecimal::add);
        nombres.putIfAbsent(clave, nombre);
    }

    public TransparenciaDTO calcular(Long idOng, Integer anioPedido) {
        Ong ong = ongRepository.findById(idOng).orElseThrow(() -> new IllegalArgumentException("La ONG no existe"));
        List<Ingreso> todosIngresos = ingresoRepository.findByOngId(idOng);
        List<Gasto> todosGastos = gastoRepository.findByOngId(idOng);

        // Años con datos (el actual siempre aparece) y año elegido: el pedido o, por defecto, el más reciente con datos
        TreeSet<Integer> conDatos = new TreeSet<>(Comparator.reverseOrder());
        todosIngresos.stream().filter(i -> i.getFecha() != null).forEach(i -> conDatos.add(i.getFecha().getYear()));
        todosGastos.stream().filter(g -> g.getFecha() != null).forEach(g -> conDatos.add(g.getFecha().getYear()));
        TreeSet<Integer> anios = new TreeSet<>(conDatos);
        anios.add(LocalDate.now().getYear());
        int anio = anioPedido != null ? anioPedido : (conDatos.isEmpty() ? LocalDate.now().getYear() : conDatos.first());

        List<Ingreso> ingresos = todosIngresos.stream()
                .filter(i -> i.getFecha() != null && i.getFecha().getYear() == anio).toList();
        List<Gasto> gastos = todosGastos.stream()
                .filter(g -> g.getFecha() != null && g.getFecha().getYear() == anio).toList();

        BigDecimal totalIngresos = ingresos.stream().map(i -> importe(i.getMonto())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalGastos = gastos.stream().map(g -> importe(g.getMonto())).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Ingresos: público/privado, por financiador y por tipo
        Map<String, BigDecimal> origen = new LinkedHashMap<>();
        Map<String, BigDecimal> financiadores = new LinkedHashMap<>();
        Map<String, String> nombresFinanciador = new LinkedHashMap<>();
        Map<String, BigDecimal> tipos = new LinkedHashMap<>();
        Map<String, String> nombresTipo = new LinkedHashMap<>();
        origen.put(Clasificacion.PUBLICO, BigDecimal.ZERO);
        origen.put(Clasificacion.PRIVADO, BigDecimal.ZERO);
        int ingresosSinClasificar = 0;
        for (Ingreso i : ingresos) {
            BigDecimal monto = importe(i.getMonto());
            String o = origenDe(i);
            origen.merge(o, monto, BigDecimal::add);
            if (!origenIndicado(i)) {
                ingresosSinClasificar++;
            }
            String nombre = financiadorDe(i);
            sumar(financiadores, nombresFinanciador, o + "|" + nombre.toLowerCase(), nombre, monto);
            String tipo = i.getTipo() == null || i.getTipo().isBlank() ? "OTROS" : i.getTipo().trim().toUpperCase();
            sumar(tipos, nombresTipo, tipo, tipo.charAt(0) + tipo.substring(1).toLowerCase(), monto);
        }
        Map<String, String> nombresOrigen = Map.of(Clasificacion.PUBLICO, "Financiación pública",
                Clasificacion.PRIVADO, "Financiación privada");
        List<Parte> listaFinanciadores = partes(financiadores, nombresFinanciador, totalIngresos);
        BigDecimal mayorPct = listaFinanciadores.isEmpty() ? BigDecimal.ZERO.setScale(1) : listaFinanciadores.get(0).porcentaje();
        String mayor = listaFinanciadores.isEmpty() ? null : listaFinanciadores.get(0).nombre();

        // Gastos: por área y por categoría
        Map<String, BigDecimal> areas = new LinkedHashMap<>();
        Map<String, String> nombresAreas = new LinkedHashMap<>();
        for (AreaGasto a : AreaGasto.values()) {
            areas.put(a.name(), BigDecimal.ZERO);
            nombresAreas.put(a.name(), nombreArea(a));
        }
        Map<String, BigDecimal> categorias = new LinkedHashMap<>();
        Map<String, String> nombresCategorias = new LinkedHashMap<>();
        int gastosSinClasificar = 0;
        for (Gasto g : gastos) {
            BigDecimal monto = importe(g.getMonto());
            areas.merge(areaDe(g).name(), monto, BigDecimal::add);
            if (g.getArea() == null) {
                gastosSinClasificar++;
            }
            String cat = g.getCategoria() == null ? "OTROS" : g.getCategoria().name();
            sumar(categorias, nombresCategorias, cat, cat.charAt(0) + cat.substring(1).toLowerCase(), monto);
        }
        // Las áreas se muestran siempre en el mismo orden (misión, administración, captación)
        List<Parte> listaAreas = new ArrayList<>();
        areas.forEach((clave, valor) -> listaAreas.add(new Parte(clave, nombresAreas.get(clave), valor, porcentaje(valor, totalGastos))));

        boolean publicada = Boolean.TRUE.equals(ong.getTransparenciaPublica());
        List<Parte> listaOrigen = new ArrayList<>();
        origen.forEach((clave, valor) -> listaOrigen.add(new Parte(clave, nombresOrigen.get(clave), valor, porcentaje(valor, totalIngresos))));

        return new TransparenciaDTO(anio, new ArrayList<>(anios), totalIngresos, totalGastos,
                totalIngresos.subtract(totalGastos), listaOrigen, listaFinanciadores,
                partes(tipos, nombresTipo, totalIngresos), listaAreas, partes(categorias, nombresCategorias, totalGastos),
                mayorPct, mayor, gastosSinClasificar, ingresosSinClasificar, publicada);
    }

    /** Contenido de la página pública; falla si la ONG no existe o no ha publicado su transparencia. */
    public TransparenciaPublicaDTO publica(Long idOng, Integer anio) {
        Ong ong = ongRepository.findById(idOng).orElse(null);
        if (ong == null || !Boolean.TRUE.equals(ong.getTransparenciaPublica())) {
            return null;
        }
        TransparenciaDTO datos = calcular(idOng, anio);
        // En la página pública no se enseña qué falta por clasificar
        TransparenciaDTO limpio = new TransparenciaDTO(datos.anio(), datos.anios(), datos.ingresos(), datos.gastos(),
                datos.resultado(), datos.financiacion(), datos.financiadores(), datos.ingresosPorTipo(), datos.gastosPorArea(),
                datos.gastosPorCategoria(), datos.mayorFinanciadorPorcentaje(), datos.mayorFinanciador(), 0, 0, true);
        return new TransparenciaPublicaDTO(ong.getId(), ong.getNombre(), ong.getDescripcion(), ong.getDireccion(),
                ong.getEmail(), ong.getTelefono(), ong.getNif(), ong.getFechaCreacion(),
                gobiernoRepository.findByOngIdOrderByOrganoAscOrdenAscIdAsc(idOng), limpio);
    }

    public void publicar(Long idOng, boolean publica) {
        Ong ong = ongRepository.findById(idOng).orElseThrow(() -> new IllegalArgumentException("La ONG no existe"));
        ong.setTransparenciaPublica(publica);
        ongRepository.save(ong);
    }
}
