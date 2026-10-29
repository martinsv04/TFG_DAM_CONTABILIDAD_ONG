package com.tfg.ong.controller;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.ong.security.AuthUser;
import com.tfg.ong.security.OngAccessService;
import com.tfg.ong.service.CertificadoService;

/**
 * Certificados de donación. ADMIN y CONTABLE los consultan por ONG; el DONANTE solo puede pedir
 * su propio certificado anual en /mi-anual (reglas de rol en SecurityConfig).
 */
@RestController
@RequestMapping("/api/certificados")
public class CertificadoController {

    private final OngAccessService ongAccess;
    private final CertificadoService certificadoService;

    public CertificadoController(OngAccessService ongAccess, CertificadoService certificadoService) {
        this.ongAccess = ongAccess;
        this.certificadoService = certificadoService;
    }

    private static int anioOActual(Integer anio) {
        return anio != null ? anio : LocalDate.now().getYear();
    }

    private static ResponseEntity<Object> error(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    @GetMapping("/ong/{idOng}/donantes")
    public ResponseEntity<Object> donantes(@PathVariable Long idOng, @RequestParam(required = false) Integer anio,
                                           @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);
        return ResponseEntity.ok(certificadoService.resumenDonantes(idOng, anioOActual(anio)));
    }

    @GetMapping("/ong/{idOng}/donacion/{idIngreso}")
    public ResponseEntity<Object> donacion(@PathVariable Long idOng, @PathVariable Long idIngreso,
                                           @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);
        try {
            return ResponseEntity.ok(certificadoService.certificadoDonacion(idOng, idIngreso));
        } catch (IllegalArgumentException e) {
            return error(e);
        }
    }

    @GetMapping("/ong/{idOng}/anual/{idUsuario}")
    public ResponseEntity<Object> anual(@PathVariable Long idOng, @PathVariable Long idUsuario,
                                        @RequestParam(required = false) Integer anio,
                                        @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);
        try {
            return ResponseEntity.ok(certificadoService.certificadoAnual(idOng, idUsuario, anioOActual(anio)));
        } catch (IllegalArgumentException e) {
            return error(e);
        }
    }

    /** Certificado anual del propio donante, en la ONG a la que pertenece. */
    @GetMapping("/mi-anual")
    public ResponseEntity<Object> miAnual(@RequestParam(required = false) Integer anio,
                                          @AuthenticationPrincipal AuthUser caller) {
        if (caller == null || caller.ongId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "No perteneces a ninguna ONG"));
        }
        try {
            return ResponseEntity.ok(certificadoService.certificadoAnual(caller.ongId(), caller.id(), anioOActual(anio)));
        } catch (IllegalArgumentException e) {
            return error(e);
        }
    }
}
