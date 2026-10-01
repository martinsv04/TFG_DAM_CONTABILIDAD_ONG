package com.tfg.ong.controller;

import com.tfg.ong.security.OngAccessService;
import com.tfg.ong.dto.MiembroDTO;
import org.springframework.security.access.AccessDeniedException;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.repository.UsuarioRepository;
import com.tfg.ong.security.AuthUser;
import com.tfg.ong.service.OngService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ongs")
public class OngController {

    private final OngAccessService ongAccess;

    private final OngService ongService;

    private final UsuarioRepository usuarioRepository;

    private final OngRepository ongRepository;

    public OngController(OngService ongService, UsuarioRepository usuarioRepository, OngRepository ongRepository, OngAccessService ongAccess) {
        this.ongAccess = ongAccess;
        this.ongService = ongService;
        this.usuarioRepository = usuarioRepository;
        this.ongRepository = ongRepository;
    }
    @GetMapping
    public List<Ong> getAllOngs(@AuthenticationPrincipal AuthUser caller) {

        return ongAccess.filtrar(caller, ongService.getAllOngs(), ong -> ong);

    }

    @GetMapping("/{id}")
    public Ong getOngById(@PathVariable Long id, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, id);


        return ongService.getOngById(id);

    }

    @PostMapping
    public Ong createOng(@RequestBody Ong ong, @AuthenticationPrincipal AuthUser caller) {
        // La ONG queda siempre asociada al administrador que la crea
        if (caller != null) {
            usuarioRepository.findById(caller.id()).ifPresent(ong::setAdmin);
        }


        ong.setFechaCreacion(LocalDate.now());
        return ongService.createOng(ong);

    }

    @PutMapping("/{id}")
    public ResponseEntity<Ong> updateOng(@PathVariable Long id, @RequestBody Ong updatedOng, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, id);


    Optional<Ong> ongOpt = ongRepository.findById(id);

        if (ongOpt.isPresent()) {
            Ong ong = ongOpt.get();
            ong.setNombre(updatedOng.getNombre());
            ong.setDescripcion(updatedOng.getDescripcion());
            ong.setDireccion(updatedOng.getDireccion());
            ong.setTelefono(updatedOng.getTelefono());
            ong.setEmail(updatedOng.getEmail());

            ongRepository.save(ong);

            return ResponseEntity.ok(ong);
        } else {
            return ResponseEntity.notFound().build();
        }

    }


    @DeleteMapping("/{id}")
    public void deleteOng(@PathVariable Long id, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, id);


        ongService.deleteOng(id);

    }

    @GetMapping("/admin/{adminId}")
    public List<Ong> getOngsByAdminId(@PathVariable Long adminId, @AuthenticationPrincipal AuthUser caller) {
        if (caller == null || !caller.id().equals(adminId)) {
            throw new AccessDeniedException("Solo puedes consultar tus propias ONGs");
        }


    return ongRepository.findByAdminId(adminId);

    }

    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<Ong>> getOngsForUser(@PathVariable Long userId, @AuthenticationPrincipal AuthUser caller) {

        // Solo se pueden consultar las ONGs propias
        if (caller == null || !caller.id().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

    Optional<Usuario> usuarioOpt = usuarioRepository.findById(userId);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            if (usuario.getRol().equals(Rol.ADMIN)) {
                List<Ong> ongs = ongRepository.findByAdminId(userId);
                return ResponseEntity.ok(ongs);
            } else {
                Ong ong = usuario.getOng();
                if (ong != null) {
                    List<Ong> resultado = new ArrayList<>();
                    resultado.add(ong);
                    return ResponseEntity.ok(resultado);
                } else {
                    return ResponseEntity.ok(Collections.emptyList());
                }
            }
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/{id}/miembros")
    public ResponseEntity<List<MiembroDTO>> getMiembrosDeOng(@PathVariable Long id, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarOng(caller, id);

        Optional<Ong> ongOpt = ongRepository.findById(id);

        if (ongOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Ong ong = ongOpt.get();

        List<Usuario> miembros = usuarioRepository.findByOngId(id);

        Usuario admin = ong.getAdmin();
        if (admin != null && miembros.stream().noneMatch(u -> u.getId().equals(admin.getId()))) {
            miembros.add(0, admin);
        }

        List<MiembroDTO> resultado = miembros.stream()
                .map(usuario -> new MiembroDTO(usuario.getId(), usuario.getNombre(), usuario.getRol()))
                .toList();

        return ResponseEntity.ok(resultado);
    }

}
