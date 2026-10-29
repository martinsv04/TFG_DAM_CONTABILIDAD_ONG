package com.tfg.ong.controller;

import com.tfg.ong.security.AuthUser;
import com.tfg.ong.security.OngAccessService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.repository.UsuarioRepository;
import com.tfg.ong.service.CodigoPostal;
import com.tfg.ong.service.Nif;
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

    /** El administrador completa los datos fiscales de un miembro (por ejemplo, un donante) para sus certificados. */
    @PatchMapping("/{id}/datos-fiscales")
    public ResponseEntity<?> actualizarDatosFiscales(@PathVariable Long id, @RequestBody Map<String, String> payload,
                                                     @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarUsuario(caller, id);

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String nif = Nif.normalizar(payload.get("nifCif"));
        if (nif != null && !Nif.esValido(nif)) {
            return ResponseEntity.badRequest().body(Map.of("message", "El NIF/NIE/CIF no tiene un formato válido"));
        }

        String cp = CodigoPostal.normalizar(payload.get("codigoPostal"));
        if (cp != null && !CodigoPostal.esValido(cp)) {
            return ResponseEntity.badRequest().body(Map.of("message", "El código postal debe tener 5 cifras y una provincia válida"));
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setCodigoPostal(cp);
        String nombre = payload.get("nombre");
        if (nombre != null && !nombre.isBlank()) {
            usuario.setNombre(nombre.trim());
        }
        usuario.setNifCif(nif);
        String direccion = payload.get("direccion");
        usuario.setDireccion(direccion == null || direccion.isBlank() ? null : direccion.trim());

        return ResponseEntity.ok(usuarioRepository.save(usuario));
    }

    /** El administrador fija una contraseña nueva a un miembro de su ONG (por ejemplo, si la ha olvidado). */
    @PatchMapping("/{id}/password")
    public ResponseEntity<Object> restablecerPassword(@PathVariable Long id, @RequestBody Map<String, String> payload,
                                                      @AuthenticationPrincipal AuthUser caller) {
        ongAccess.verificarUsuario(caller, id);
        try {
            usuarioService.cambiarPassword(id, payload.get("password"));
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
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
