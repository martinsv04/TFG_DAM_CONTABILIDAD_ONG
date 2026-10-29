package com.tfg.ong.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.tfg.ong.dto.Modelo182Dto;
import com.tfg.ong.dto.Modelo182Dto.DeclaranteDTO;
import com.tfg.ong.dto.Modelo182Dto.RegistroDTO;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.OngRepository;

/**
 * Modelo 182 (declaración informativa de donativos). Genera la vista previa y el fichero de la AEAT
 * (registros de 250 caracteres: un registro tipo 1 del declarante y un tipo 2 por cada donante).
 *
 * Solo se declaran las donaciones hechas a nombre de una persona (las anónimas no tienen NIF), todas con clave A
 * (donativos no incluidos en programas prioritarios de mecenazgo) y en dinero.
 */
@Service
public class Modelo182Service {

    private static final BigDecimal UMBRAL_80 = new BigDecimal("250");

    private final IngresoRepository ingresoRepository;
    private final OngRepository ongRepository;

    public Modelo182Service(IngresoRepository ingresoRepository, OngRepository ongRepository) {
        this.ingresoRepository = ingresoRepository;
        this.ongRepository = ongRepository;
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }

    /** Persona física (DNI o NIE) o jurídica (CIF). */
    static String naturaleza(String nif) {
        char primera = nif.charAt(0);
        return Character.isDigit(primera) || primera == 'X' || primera == 'Y' || primera == 'Z' ? "F" : "J";
    }

    /** Porcentaje de deducción que se consigna, según la normativa vigente desde 2024. */
    static BigDecimal porcentaje(String naturaleza, BigDecimal importe, boolean recurrente) {
        if ("F".equals(naturaleza)) {
            if (importe.compareTo(UMBRAL_80) <= 0) {
                return new BigDecimal("80");
            }
            return new BigDecimal(recurrente ? "45" : "40");
        }
        return new BigDecimal(recurrente ? "50" : "40");
    }

    private static BigDecimal suma(List<Ingreso> lista) {
        return lista.stream().map(Ingreso::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    public Modelo182Dto previsualizar(Long idOng, int anio) {
        Ong ong = ongRepository.findById(idOng).orElseThrow(() -> new IllegalArgumentException("La ONG no existe"));

        // --- Datos de la ONG (declarante) ---
        List<String> problemas = new ArrayList<>();
        String nifOng = Nif.normalizar(ong.getNif());
        if (nifOng == null || !Nif.esValido(nifOng)) {
            problemas.add(nifOng == null
                    ? "Falta el NIF de la ONG (Editar ONG)"
                    : "El NIF de la ONG («" + nifOng + "») no tiene un formato válido: debe ser un CIF con una letra, 7 cifras y un dígito o letra de control, por ejemplo G12345678 (Editar ONG)");
        }
        if (vacio(ong.getRepresentante())) {
            problemas.add("Falta el representante legal de la ONG, que figura como persona de contacto (Editar ONG)");
        }
        String telefono = ong.getTelefono() == null ? "" : ong.getTelefono().replaceAll("\\D", "");
        if (telefono.length() < 9) {
            problemas.add("Falta un teléfono de contacto de 9 cifras en la ONG (Editar ONG)");
        } else {
            telefono = telefono.substring(telefono.length() - 9);
        }

        // --- Donaciones del año y de los dos anteriores (para la recurrencia) ---
        List<Ingreso> donaciones = ingresoRepository
                .findByOngIdAndFechaBetween(idOng, LocalDate.of(anio - 2, 1, 1), LocalDate.of(anio, 12, 31)).stream()
                .filter(CertificadoService::esDonacion)
                .filter(i -> i.getUsuario() != null && i.getMonto() != null && i.getFecha() != null)
                .toList();

        Map<Long, List<Ingreso>> porDonante = new LinkedHashMap<>();
        for (Ingreso i : donaciones) {
            if (i.getFecha().getYear() == anio) {
                porDonante.computeIfAbsent(i.getUsuario().getId(), k -> new ArrayList<>()).add(i);
            }
        }

        List<RegistroDTO> registros = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (List<Ingreso> delAnio : porDonante.values()) {
            Usuario u = delAnio.get(0).getUsuario();
            BigDecimal importe = suma(delAnio);
            BigDecimal anterior1 = suma(donaciones.stream()
                    .filter(i -> i.getUsuario().getId().equals(u.getId()) && i.getFecha().getYear() == anio - 1).toList());
            BigDecimal anterior2 = suma(donaciones.stream()
                    .filter(i -> i.getUsuario().getId().equals(u.getId()) && i.getFecha().getYear() == anio - 2).toList());
            boolean recurrente = anterior1.signum() > 0 && anterior2.signum() > 0
                    && importe.compareTo(anterior1) >= 0 && anterior1.compareTo(anterior2) >= 0;

            List<String> problemasDonante = new ArrayList<>();
            String nif = Nif.normalizar(u.getNifCif());
            if (vacio(u.getNombre())) {
                problemasDonante.add("Falta el nombre");
            }
            if (nif == null) {
                problemasDonante.add("Falta el NIF");
            } else if (!Nif.esValido(nif)) {
                problemasDonante.add("El NIF no tiene un formato válido");
            }
            String cp = CodigoPostal.normalizar(u.getCodigoPostal());
            String provincia = CodigoPostal.provincia(cp);
            if (provincia == null) {
                problemasDonante.add("Falta el código postal (da la provincia)");
            }

            String naturaleza = nif != null && Nif.esValido(nif) ? naturaleza(nif) : null;
            BigDecimal pct = naturaleza != null ? porcentaje(naturaleza, importe, recurrente) : null;

            registros.add(new RegistroDTO(u.getId(), u.getNombre(), nif, cp, provincia, naturaleza, "A", pct, importe,
                    delAnio.size(), recurrente, problemasDonante));
            total = total.add(importe);
        }
        registros.sort(Comparator.comparing(r -> r.nombre() == null ? "" : r.nombre().toUpperCase(Locale.ROOT)));

        if (registros.isEmpty()) {
            problemas.add("No hay donaciones a nombre de una persona en " + anio);
        }
        boolean listo = problemas.isEmpty() && registros.stream().allMatch(r -> r.problemas().isEmpty());

        DeclaranteDTO declarante = new DeclaranteDTO(nifOng, ong.getNombre(), telefono, ong.getRepresentante(), justificante(anio));
        return new Modelo182Dto(anio, declarante, registros, total.setScale(2, RoundingMode.HALF_UP), problemas, listo);
    }

    /** Número identificativo de la declaración: modelo (3) + ejercicio (4) + secuencial (6). */
    static String justificante(int anio) {
        return "182" + anio + "000001";
    }

    /** Fichero del Modelo 182 listo para presentar (ISO-8859-1, un registro de 250 caracteres por línea). */
    public byte[] fichero(Long idOng, int anio) {
        Modelo182Dto datos = previsualizar(idOng, anio);
        if (!datos.listo()) {
            throw new IllegalArgumentException("El Modelo 182 de " + anio + " todavía no se puede generar: hay datos por completar");
        }

        DeclaranteDTO d = datos.declarante();
        String nifDeclarante = numerico(d.nif(), 9, true);
        List<String> lineas = new ArrayList<>();

        // Registro tipo 1: declarante
        StringBuilder r1 = new StringBuilder();
        r1.append('1').append("182").append(numero(anio, 4));
        r1.append(nifDeclarante);
        r1.append(texto(d.nombre(), 40));
        r1.append('T');
        r1.append(numero(Long.parseLong(d.telefono()), 9));
        r1.append(texto(d.contacto(), 40));
        r1.append(numero(Long.parseLong(d.justificante()), 13));
        r1.append(' ').append(' ');                       // complementaria / sustitutiva
        r1.append(numero(0, 13));                         // declaración anterior
        r1.append(numero(datos.registros().size(), 9));
        r1.append(importe(datos.importeTotal(), 13));
        r1.append('1');                                   // naturaleza del declarante: entidad de la Ley 49/2002
        r1.append(" ".repeat(77));
        r1.append(" ".repeat(13));                        // sello electrónico
        lineas.add(r1.toString());

        // Registros tipo 2: un donante cada uno
        for (RegistroDTO r : datos.registros()) {
            StringBuilder r2 = new StringBuilder();
            r2.append('2').append("182").append(numero(anio, 4));
            r2.append(nifDeclarante);
            r2.append(numerico(r.nif(), 9, true));
            r2.append(" ".repeat(9));                     // NIF del representante legal
            r2.append(texto(r.nombre(), 40));
            r2.append(r.provincia());
            r2.append(r.clave());
            r2.append(porcentajeCampo(r.porcentaje()));
            r2.append(importe(r.importe(), 11));
            r2.append(' ');                               // donativo en especie
            r2.append("  ");                             // deducción de comunidad autónoma
            r2.append("00000");                           // % deducción de comunidad autónoma
            r2.append(r.naturaleza());
            r2.append(' ');                               // revocación
            r2.append("0000");                            // ejercicio de la donación revocada
            r2.append(' ');                               // tipo de bien
            r2.append(" ".repeat(20));                    // identificación del bien
            r2.append(r.recurrente() ? '1' : '2');
            r2.append(" ".repeat(9));                     // NIF del titular del patrimonio protegido
            r2.append(" ".repeat(40));
            r2.append(" ".repeat(69));
            lineas.add(r2.toString());
        }

        for (String linea : lineas) {
            if (linea.length() != 250) {
                throw new IllegalStateException("Registro del Modelo 182 con longitud " + linea.length() + " en lugar de 250");
            }
        }
        return (String.join("\r\n", lineas) + "\r\n").getBytes(StandardCharsets.ISO_8859_1);
    }

    // ---- Formato de campos del fichero ----

    /** Texto en mayúsculas sin acentos ni caracteres especiales (se admiten Ñ y Ç), a la izquierda y relleno con espacios. */
    static String texto(String valor, int longitud) {
        StringBuilder sb = new StringBuilder();
        for (char c : (valor == null ? "" : valor).toUpperCase(Locale.ROOT).toCharArray()) {
            if (c == 'Ñ' || c == 'Ç') {
                sb.append(c);
                continue;
            }
            for (char b : Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD).replaceAll("\\p{M}", "").toCharArray()) {
                sb.append((b >= 'A' && b <= 'Z') || (b >= '0' && b <= '9') ? b : ' ');
            }
        }
        String limpio = sb.toString().trim().replaceAll(" {2,}", " ");
        return limpio.length() >= longitud ? limpio.substring(0, longitud) : limpio + " ".repeat(longitud - limpio.length());
    }

    /** Número a la derecha y relleno con ceros. */
    static String numero(long valor, int longitud) {
        String s = String.valueOf(valor);
        return s.length() >= longitud ? s.substring(s.length() - longitud) : "0".repeat(longitud - s.length()) + s;
    }

    /** Campo alfanumérico alineado a la derecha y relleno con ceros (NIF). */
    static String numerico(String valor, int longitud, boolean ceros) {
        String s = valor == null ? "" : valor;
        return s.length() >= longitud ? s.substring(s.length() - longitud) : "0".repeat(longitud - s.length()) + s;
    }

    /** Importe en euros como parte entera (longitudEntera cifras) + 2 decimales, sin separador. */
    static String importe(BigDecimal valor, int longitudEntera) {
        long centimos = valor.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
        return numero(centimos / 100, longitudEntera) + numero(centimos % 100, 2);
    }

    /** Porcentaje: 3 cifras enteras + 2 decimales. */
    static String porcentajeCampo(BigDecimal pct) {
        long centesimas = pct.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
        return numero(centesimas / 100, 3) + numero(centesimas % 100, 2);
    }
}
