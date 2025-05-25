package com.tfg.ong.controller;

import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.UsuarioRepository;
import com.tfg.ong.service.UsuarioService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    
    private final UsuarioService usuarioService;

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioService usuarioService, UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public List<Usuario> getAllUsuarios() {

        return usuarioService.getAllUsuarios();

    }

    @GetMapping("/{id}")
    public Usuario getUsuarioById(@PathVariable Long id) {

        return usuarioService.getUsuarioById(id);

    }

    @PostMapping
    public Usuario createUsuario(@RequestBody Usuario usuario) {

        usuario.setCreadoEn(LocalDate.now());

        return usuarioService.createUsuario(usuario);

    }

    @PutMapping("/{id}")
    public Usuario updateUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {

        return usuarioService.updateUsuario(id, usuario);

    }

    @DeleteMapping("/{id}")
    public void deleteUsuario(@PathVariable Long id) {

        usuarioService.deleteUsuario(id);

    }

    @GetMapping("/rol/{rol}")
    public ResponseEntity<List<Usuario>> getUsuariosByRol(@PathVariable("rol") Rol rol) {

        try {
            List<Usuario> usuarios = usuarioRepository.findByRol(rol);
            return ResponseEntity.ok(usuarios);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

    }


    
}
