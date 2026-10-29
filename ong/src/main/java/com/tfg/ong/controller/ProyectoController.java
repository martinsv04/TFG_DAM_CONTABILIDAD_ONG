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
import com.tfg.ong.service.ProyectoService;

/** Proyectos de una ONG. ADMIN y CONTABLE los consultan y crean; solo ADMIN edita o borra (reglas en SecurityConfig). */
@RestController
@RequestMapping("/api/proyectos")
public class ProyectoController {

    public record ProyectoRequest(String nombre, String descripcion, String estado, LocalDate fechaInicio,
                                  LocalDate fechaFin, BigDecimal presupuesto) {
        ProyectoService.Datos aDatos() {
            return new ProyectoService.Datos(nombre, descripcion, estado, fechaInicio, fechaFin, presupuesto);
        }
    }

    private final OngAccessService ongAccess;
    private final ProyectoService proyectoService;

    public ProyectoController(OngAccessService ongAccess, ProyectoService proyectoService) {
        this.ongAccess = ongAccess;
        this.proyectoService = proyectoService;
    }

    private static ResponseEntity<Object> error(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    @GetMapping("/ong/{idOng}")
    public ResponseEntity<Object> listar(@PathVariable Long idOng, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);
        return ResponseEntity.ok(proyectoService.listar(idOng));
    }

    @GetMapping("/ong/{idOng}/{idProyecto}")
    public ResponseEntity<Object> detalle(@PathVariable Long idOng, @PathVariable Long idProyecto,
                                          @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);
        try {
            return ResponseEntity.ok(proyectoService.detalle(idOng, idProyecto));
        } catch (IllegalArgumentException e) {
            return error(e);
        }
    }

    @PostMapping("/ong/{idOng}")
    public ResponseEntity<Object> crear(@PathVariable Long idOng, @RequestBody ProyectoRequest body,
                                        @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);
        try {
            return ResponseEntity.ok(proyectoService.crear(idOng, body.aDatos()));
        } catch (IllegalArgumentException e) {
            return error(e);
        }
    }

    @PutMapping("/ong/{idOng}/{idProyecto}")
    public ResponseEntity<Object> actualizar(@PathVariable Long idOng, @PathVariable Long idProyecto,
                                             @RequestBody ProyectoRequest body, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);
        try {
            return ResponseEntity.ok(proyectoService.actualizar(idOng, idProyecto, body.aDatos()));
        } catch (IllegalArgumentException e) {
            return error(e);
        }
    }

    @DeleteMapping("/ong/{idOng}/{idProyecto}")
    public ResponseEntity<Object> eliminar(@PathVariable Long idOng, @PathVariable Long idProyecto,
                                           @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);
        try {
            proyectoService.eliminar(idOng, idProyecto);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return error(e);
        }
    }
}
