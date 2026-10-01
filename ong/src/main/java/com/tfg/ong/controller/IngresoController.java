package com.tfg.ong.controller;

import com.tfg.ong.security.OngAccessService;
import org.springframework.security.access.AccessDeniedException;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.security.AuthUser;
import com.tfg.ong.service.IngresoService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ingresos")
public class IngresoController {

    private final OngAccessService ongAccess;

    private final IngresoService ingresoService;

    private final IngresoRepository ingresoRepository;

    private final OngRepository ongRepository;

    public IngresoController(IngresoService ingresoService, IngresoRepository ingresoRepository, OngRepository ongRepository, OngAccessService ongAccess) {
        this.ongAccess = ongAccess;
        this.ingresoService = ingresoService;
        this.ingresoRepository = ingresoRepository;
        this.ongRepository = ongRepository;
    }

    @GetMapping
    public List<Ingreso> getAllIngresos(@AuthenticationPrincipal AuthUser caller) {
        return ongAccess.filtrar(caller, ingresoService.getAllIngresos(), Ingreso::getOng);
    }

    @GetMapping("/{id}")
    public Ingreso getIngresoById(@PathVariable Long id, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarIngreso(caller, id);


        return ingresoService.getIngresoById(id);

    }

    @PutMapping("/{id}")
    public Ingreso updateIngresos(@PathVariable Long id, @RequestBody Ingreso ingreso, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarIngreso(caller, id);


        return ingresoService.updateIngresos(id, ingreso);

    }

    @DeleteMapping("/{id}")
    public void deleteIngreso(@PathVariable Long id, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarIngreso(caller, id);


        ingresoService.deleteIngreso(id);

    }

    @GetMapping("/ong/{idOng}")
    public List<Ingreso> getIngresosByOng(@PathVariable Long idOng, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, idOng);

        return ingresoRepository.findByOngId(idOng);

    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crearIngreso(@RequestBody Map<String, Object> payload,
                                                                        @AuthenticationPrincipal AuthUser caller) {

        try {
            String descripcion = (String) payload.get("descripcion");
            Double monto = Double.parseDouble(payload.get("monto").toString());
            String tipo = (String) payload.get("tipo");
            String fechaStr = (String) payload.get("fecha");
            Long idOng = Long.parseLong(payload.get("id_ong").toString());

            // Solo se puede registrar en la ONG propia
            ongAccess.verificarOng(caller, idOng);

            Long idUsuario = null;
            if (payload.get("id_usuario") != null) {
                idUsuario = Long.parseLong(payload.get("id_usuario").toString());
            }

            // Un DONANTE solo puede registrar donaciones, a su nombre o de forma anónima
            if (caller != null && caller.rol() == Rol.DONANTE) {
                tipo = "DONACIÓN";
                if (idUsuario != null) {
                    idUsuario = caller.id();
                }
            }

            Ong ong = ongRepository.findById(idOng)
                    .orElseThrow(() -> new RuntimeException("ONG no encontrada"));

            Ingreso ingreso = new Ingreso();
            ingreso.setDescripcion(descripcion);
            ingreso.setMonto(BigDecimal.valueOf(monto));
            ingreso.setTipo(tipo);
            ingreso.setFecha(LocalDate.parse(fechaStr));
            ingreso.setOng(ong);

            // Quién hizo el ingreso (p. ej. el donante); se copia a la factura al generarla
            if (idUsuario != null) {
                Usuario donante = new Usuario();
                donante.setId(idUsuario);
                ingreso.setUsuario(donante);
            }

            Ingreso ingresoGuardado = ingresoService.createIngreso(ingreso);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ingreso registrado correctamente");
            response.put("id", ingresoGuardado.getId());

            return ResponseEntity.ok(response);

        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }

    }

}
