package com.tfg.ong.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.ong.security.AuthUser;
import com.tfg.ong.security.OngAccessService;
import com.tfg.ong.service.SubvencionService;

/** Subvenciones de una ONG. ADMIN y CONTABLE las consultan y crean; solo ADMIN edita o borra (reglas en SecurityConfig). */
@RestController
@RequestMapping("/api/subvenciones")
public class SubvencionController {

    public record SubvencionRequest(String nombre, String financiador, String expediente, String estado,
                                    BigDecimal importeSolicitado, BigDecimal importeConcedido,
                                    LocalDate fechaSolicitud, LocalDate fechaConcesion, LocalDate fechaJustificacion,
                                    Long proyectoId) {
        SubvencionService.Datos aDatos() {
            return new SubvencionService.Datos(nombre, financiador, expediente, estado, importeSolicitado,
                    importeConcedido, fechaSolicitud, fechaConcesion, fechaJustificacion, proyectoId);
        }
    }

    private final OngAccessService ongAccess;
    private final SubvencionService subvencionService;

    public SubvencionController(OngAccessService ongAccess, SubvencionService subvencionService) {
        this.ongAccess = ongAccess;
        this.subvencionService = subvencionService;
    }

    private static ResponseEntity<Object> error(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    @GetMapping("/ong/{idOng}")
    public ResponseEntity<Object> listar(@PathVariable Long idOng, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);
        return ResponseEntity.ok(subvencionService.listar(idOng));
    }

    @PostMapping("/ong/{idOng}")
    public ResponseEntity<Object> crear(@PathVariable Long idOng, @RequestBody SubvencionRequest body,
                                        @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);
        try {
            return ResponseEntity.ok(subvencionService.crear(idOng, body.aDatos()));
        } catch (IllegalArgumentException e) {
            return error(e);
        }
    }

    @PutMapping("/ong/{idOng}/{idSubvencion}")
    public ResponseEntity<Object> actualizar(@PathVariable Long idOng, @PathVariable Long idSubvencion,
                                             @RequestBody SubvencionRequest body, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);
        try {
            return ResponseEntity.ok(subvencionService.actualizar(idOng, idSubvencion, body.aDatos()));
        } catch (IllegalArgumentException e) {
            return error(e);
        }
    }

    @DeleteMapping("/ong/{idOng}/{idSubvencion}")
    public ResponseEntity<Object> eliminar(@PathVariable Long idOng, @PathVariable Long idSubvencion,
                                           @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);
        try {
            subvencionService.eliminar(idOng, idSubvencion);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return error(e);
        }
    }
}
