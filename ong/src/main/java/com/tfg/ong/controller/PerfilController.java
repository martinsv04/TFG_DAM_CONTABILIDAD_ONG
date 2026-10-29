package com.tfg.ong.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.UsuarioRepository;
import com.tfg.ong.security.AuthUser;
import com.tfg.ong.service.CodigoPostal;
import com.tfg.ong.service.Nif;
import com.tfg.ong.service.PoliticaPassword;

/** "Mi perfil": cualquier usuario autenticado ve y edita sus propios datos personales y fiscales. */
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    public record Perfil(Long id, String nombre, String email, Rol rol, String telefono, String nifCif, String direccion, String codigoPostal) {
        static Perfil de(Usuario u) {
            return new Perfil(u.getId(), u.getNombre(), u.getEmail(), u.getRol(), u.getTelefono(), u.getNifCif(), u.getDireccion(), u.getCodigoPostal());
        }
    }

    public record CambiosPerfil(String nombre, String telefono, String nifCif, String direccion, String codigoPostal) {
    }

    public record CambioPassword(String actual, String nueva) {
    }

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    public PerfilController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Cada usuario cambia su propia contraseña, confirmando la actual. */
    @PutMapping("/password")
    public ResponseEntity<Object> cambiarPassword(@RequestBody CambioPassword cambio, @AuthenticationPrincipal AuthUser caller) {
        Usuario usuario = usuarioRepository.findById(caller.id()).orElse(null);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        if (cambio.actual() == null || !passwordEncoder.matches(cambio.actual(), usuario.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "La contraseña actual no es correcta"));
        }
        try {
            PoliticaPassword.validar(cambio.nueva());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
        if (cambio.nueva().equals(cambio.actual())) {
            return ResponseEntity.badRequest().body(Map.of("message", "La contraseña nueva debe ser distinta de la actual"));
        }
        usuario.setPassword(passwordEncoder.encode(cambio.nueva()));
        usuarioRepository.save(usuario);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Perfil> ver(@AuthenticationPrincipal AuthUser caller) {
        return usuarioRepository.findById(caller.id())
                .map(u -> ResponseEntity.ok(Perfil.de(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<Object> guardar(@RequestBody CambiosPerfil cambios, @AuthenticationPrincipal AuthUser caller) {
        Usuario usuario = usuarioRepository.findById(caller.id()).orElse(null);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        if (cambios.nombre() == null || cambios.nombre().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "El nombre es obligatorio"));
        }
        String nif = Nif.normalizar(cambios.nifCif());
        if (nif != null && !Nif.esValido(nif)) {
            return ResponseEntity.badRequest().body(Map.of("message", "El NIF/NIE/CIF no tiene un formato válido"));
        }

        String cp = CodigoPostal.normalizar(cambios.codigoPostal());
        if (cp != null && !CodigoPostal.esValido(cp)) {
            return ResponseEntity.badRequest().body(Map.of("message", "El código postal debe tener 5 cifras y una provincia válida"));
        }

        usuario.setNombre(cambios.nombre().trim());
        usuario.setTelefono(cambios.telefono() == null || cambios.telefono().isBlank() ? null : cambios.telefono().trim());
        usuario.setNifCif(nif);
        usuario.setCodigoPostal(cp);
        usuario.setDireccion(cambios.direccion() == null || cambios.direccion().isBlank() ? null : cambios.direccion().trim());

        return ResponseEntity.ok(Perfil.de(usuarioRepository.save(usuario)));
    }
}
