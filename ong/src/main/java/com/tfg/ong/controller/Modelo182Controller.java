package com.tfg.ong.controller;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.ong.security.AuthUser;
import com.tfg.ong.security.OngAccessService;
import com.tfg.ong.service.Modelo182Service;

/** Modelo 182: vista previa y fichero para la AEAT. Solo ADMIN y CONTABLE (ver SecurityConfig). */
@RestController
@RequestMapping("/api/modelo182")
public class Modelo182Controller {

    private final OngAccessService ongAccess;
    private final Modelo182Service modelo182Service;

    public Modelo182Controller(OngAccessService ongAccess, Modelo182Service modelo182Service) {
        this.ongAccess = ongAccess;
        this.modelo182Service = modelo182Service;
    }

    private static int anioOActual(Integer anio) {
        return anio != null ? anio : LocalDate.now().getYear() - 1; // el Modelo 182 se presenta en enero para el año anterior
    }

    @GetMapping("/ong/{idOng}")
    public ResponseEntity<Object> previsualizar(@PathVariable Long idOng, @RequestParam(required = false) Integer anio,
                                                @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);
        try {
            return ResponseEntity.ok(modelo182Service.previsualizar(idOng, anioOActual(anio)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/ong/{idOng}/fichero")
    public ResponseEntity<Object> fichero(@PathVariable Long idOng, @RequestParam(required = false) Integer anio,
                                          @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);
        int ejercicio = anioOActual(anio);
        try {
            byte[] contenido = modelo182Service.fichero(idOng, ejercicio);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment().filename("modelo182_" + ejercicio + ".txt").build().toString())
                    .contentType(MediaType.parseMediaType("text/plain; charset=ISO-8859-1"))
                    .body(contenido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
