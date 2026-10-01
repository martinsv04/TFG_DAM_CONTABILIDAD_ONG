package com.tfg.ong.service;

import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario getUsuarioById(Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return usuario.orElse(null);
    }

    public Usuario createUsuario(Usuario usuario) {
        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    public Usuario updateUsuario(Long id, Usuario usuario) {
        usuario.setId(id);

        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            // Si no se envía contraseña nueva, se conserva la que ya tenía (hash)
            usuarioRepository.findById(id)
                    .ifPresent(existente -> usuario.setPassword(existente.getPassword()));
        } else {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }

        return usuarioRepository.save(usuario);
    }

    public Usuario actualizarRol(Long id, Rol nuevoRol) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setRol(nuevoRol);
        return usuarioRepository.save(usuario);
    }


    public void deleteUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

}
