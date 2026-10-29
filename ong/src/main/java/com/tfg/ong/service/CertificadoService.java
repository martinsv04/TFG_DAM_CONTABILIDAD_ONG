package com.tfg.ong.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.tfg.ong.dto.CertificadoDTO;
import com.tfg.ong.dto.CertificadoDTO.DonanteDTO;
import com.tfg.ong.dto.CertificadoDTO.EntidadDTO;
import com.tfg.ong.dto.CertificadoDTO.LineaDTO;
import com.tfg.ong.dto.DonanteResumenDTO;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.OngRepository;

/**
 * Prepara los datos de los certificados de donación (Ley 49/2002, art. 24): identificación fiscal del donante y de la
 * entidad, fecha, importe y destino. Los certificados se numeran de forma determinista, así que no hace falta guardarlos.
 */
@Service
public class CertificadoService {

    private final IngresoRepository ingresoRepository;
    private final OngRepository ongRepository;

    public CertificadoService(IngresoRepository ingresoRepository, OngRepository ongRepository) {
        this.ingresoRepository = ingresoRepository;
        this.ongRepository = ongRepository;
    }

    /** Un ingreso cuenta como donación si su tipo es "DONACIÓN" (con o sin tilde, en cualquier combinación de mayúsculas). */
    static boolean esDonacion(Ingreso ingreso) {
        if (ingreso.getTipo() == null) {
            return false;
        }
        String tipo = Normalizer.normalize(ingreso.getTipo().trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return tipo.equalsIgnoreCase("DONACION");
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private Ong ongConDatosFiscales(Long idOng) {
        Ong ong = ongRepository.findById(idOng)
                .orElseThrow(() -> new IllegalArgumentException("La ONG no existe"));
        if (vacio(ong.getNif()) || vacio(ong.getDireccion()) || vacio(ong.getRepresentante())) {
            throw new IllegalArgumentException(
                    "Completa los datos de la ONG (NIF, dirección y representante legal) en «Editar ONG» antes de emitir certificados");
        }
        return ong;
    }

    private static void exigirDatosDonante(Usuario donante) {
        List<String> faltan = new ArrayList<>();
        if (vacio(donante.getNombre())) {
            faltan.add("nombre");
        }
        if (vacio(donante.getNifCif())) {
            faltan.add("NIF");
        }
        if (vacio(donante.getDireccion())) {
            faltan.add("dirección");
        }
        if (!faltan.isEmpty()) {
            throw new IllegalArgumentException("Faltan datos fiscales del donante: " + String.join(", ", faltan));
        }
    }

    private static EntidadDTO entidad(Ong ong) {
        return new EntidadDTO(ong.getNombre(), ong.getNif(), ong.getDireccion(), ong.getRepresentante(), ong.getLogo(), ong.getFirma());
    }

    private static DonanteDTO donante(Usuario u) {
        return new DonanteDTO(u.getNombre(), u.getNifCif(), u.getDireccion());
    }

    private static LineaDTO linea(Ingreso i) {
        return new LineaDTO(i.getFecha(), i.getMonto().setScale(2, RoundingMode.HALF_UP), i.getDescripcion());
    }

    /** Donaciones identificadas (con donante) de una ONG en un año. */
    private List<Ingreso> donacionesDelAnio(Long idOng, int anio) {
        return ingresoRepository.findByOngIdAndFechaBetween(idOng, LocalDate.of(anio, 1, 1), LocalDate.of(anio, 12, 31))
                .stream()
                .filter(CertificadoService::esDonacion)
                .filter(i -> i.getUsuario() != null && i.getMonto() != null)
                .sorted(Comparator.comparing(Ingreso::getFecha))
                .toList();
    }

    /** Certificado de una única donación. */
    public CertificadoDTO certificadoDonacion(Long idOng, Long idIngreso) {
        Ingreso ingreso = ingresoRepository.findById(idIngreso)
                .orElseThrow(() -> new IllegalArgumentException("La donación no existe"));
        if (ingreso.getOng() == null || !idOng.equals(ingreso.getOng().getId())) {
            throw new IllegalArgumentException("La donación no pertenece a esta ONG");
        }
        if (!esDonacion(ingreso)) {
            throw new IllegalArgumentException("Este ingreso no es una donación");
        }
        if (ingreso.getUsuario() == null) {
            throw new IllegalArgumentException("Es una donación anónima: no se puede emitir certificado");
        }
        if (ingreso.getMonto() == null || ingreso.getFecha() == null) {
            throw new IllegalArgumentException("La donación no tiene importe o fecha");
        }

        Ong ong = ongConDatosFiscales(idOng);
        Usuario donante = ingreso.getUsuario();
        exigirDatosDonante(donante);

        int anio = ingreso.getFecha().getYear();
        LineaDTO linea = linea(ingreso);
        return new CertificadoDTO("CD-" + anio + "-" + idOng + "-" + idIngreso, "DONACION", anio, LocalDate.now(),
                entidad(ong), donante(donante), linea.importe(), List.of(linea));
    }

    /** Certificado anual de un donante: suma de todas sus donaciones del año. */
    public CertificadoDTO certificadoAnual(Long idOng, Long idUsuario, int anio) {
        Ong ong = ongConDatosFiscales(idOng);

        List<Ingreso> donaciones = donacionesDelAnio(idOng, anio).stream()
                .filter(i -> idUsuario.equals(i.getUsuario().getId()))
                .toList();
        if (donaciones.isEmpty()) {
            throw new IllegalArgumentException("No hay donaciones registradas a nombre de esta persona en " + anio);
        }

        Usuario donante = donaciones.get(0).getUsuario();
        exigirDatosDonante(donante);

        List<LineaDTO> lineas = donaciones.stream().map(CertificadoService::linea).toList();
        BigDecimal total = lineas.stream().map(LineaDTO::importe).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CertificadoDTO("CDA-" + anio + "-" + idOng + "-" + idUsuario, "ANUAL", anio, LocalDate.now(),
                entidad(ong), donante(donante), total, lineas);
    }

    /** Donantes de una ONG con lo donado en el año, para elegir a quién emitir el certificado. */
    public List<DonanteResumenDTO> resumenDonantes(Long idOng, int anio) {
        Map<Long, List<Ingreso>> porDonante = new LinkedHashMap<>();
        for (Ingreso i : donacionesDelAnio(idOng, anio)) {
            porDonante.computeIfAbsent(i.getUsuario().getId(), k -> new ArrayList<>()).add(i);
        }

        List<DonanteResumenDTO> resultado = new ArrayList<>();
        for (List<Ingreso> lista : porDonante.values()) {
            Usuario u = lista.get(0).getUsuario();
            BigDecimal total = lista.stream().map(i -> i.getMonto().setScale(2, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            boolean completo = !vacio(u.getNombre()) && !vacio(u.getNifCif()) && !vacio(u.getDireccion());
            resultado.add(new DonanteResumenDTO(u.getId(), u.getNombre(), u.getEmail(), u.getNifCif(), u.getDireccion(), u.getCodigoPostal(),
                    total, lista.size(), completo));
        }
        resultado.sort(Comparator.comparing(DonanteResumenDTO::totalAnio).reversed());
        return resultado;
    }
}
