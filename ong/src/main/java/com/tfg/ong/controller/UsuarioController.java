package com.tfg.ong.controller;

import com.tfg.ong.security.AuthUser;
import com.tfg.ong.security.OngAccessService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.repository.UsuarioRepository;
import com.tfg.ong.service.OngService;
import com.tfg.ong.service.UsuarioService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final OngAccessService ongAccess;

    
    private final UsuarioService usuarioService;

    private final UsuarioRepository usuarioRepository;

    private final OngRepository ongRepository;

    public UsuarioController(UsuarioService usuarioService, UsuarioRepository usuarioRepository, OngRepository ongRepository, OngAccessService ongAccess) {
        this.ongAccess = ongAccess;
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.ongRepository = ongRepository;
    }

    @GetMapping
    public List<Usuario> getAllUsuarios(@AuthenticationPrincipal AuthUser caller) {

        return ongAccess.filtrar(caller, usuarioService.getAllUsuarios(), Usuario::getOng);

    }

    @GetMapping("/{id}")
    public Usuario getUsuarioById(@PathVariable Long id, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarUsuario(caller, id);


        return usuarioService.getUsuarioById(id);

    }

    @PostMapping
    public Usuario createUsuario(@RequestBody Usuario usuario, @AuthenticationPrincipal AuthUser caller) {
        // El nuevo usuario debe quedar asignado a una ONG propia
        ongAccess.verificarOng(caller, usuario.getOng() != null ? usuario.getOng().getId() : null);

        usuario.setCreadoEn(LocalDate.now());

        if (usuario.getOng() != null && usuario.getOng().getId() != null) {
            Optional<Ong> ongOpt = ongRepository.findById(usuario.getOng().getId());
            if (ongOpt.isPresent()) {
                usuario.setOng(ongOpt.get());
            } else {
                throw new RuntimeException("Ong no encontrada con ID: " + usuario.getOng().getId());
            }
        }

        return usuarioService.createUsuario(usuario);
    }


    @PutMapping("/{id}")
    public Usuario updateUsuario(@PathVariable Long id, @RequestBody Usuario usuario, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarUsuario(caller, id);
        if (usuario.getOng() != null) {
            ongAccess.verificarOng(caller, usuario.getOng().getId());
        }


        return usuarioService.updateUsuario(id, usuario);

    }

    @DeleteMapping("/{id}")
    public void deleteUsuario(@PathVariable Long id, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarUsuario(caller, id);


        usuarioService.deleteUsuario(id);

    }

    @GetMapping("/rol/{rol}")
    public ResponseEntity<List<Usuario>> getUsuariosByRol(@PathVariable("rol") Rol rol, @AuthenticationPrincipal AuthUser caller) {

        try {
            List<Usuario> usuarios = usuarioRepository.findByRol(rol);
            return ResponseEntity.ok(ongAccess.filtrar(caller, usuarios, Usuario::getOng));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

    }

    @PatchMapping("/{id}/rol")
    public ResponseEntity<Usuario> actualizarRol(@PathVariable Long id, @RequestBody Map<String, String> payload, @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarUsuario(caller, id);

        System.out.println("Rol recibido: " + payload.get("rol"));

        try {
            System.out.println("Rol recibido: " + payload.get("rol"));

            Rol nuevoRol = Rol.valueOf(payload.get("rol"));
            Usuario actualizado = usuarioService.actualizarRol(id, nuevoRol);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); // rol no válido
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    
}
