package com.tfg.ong.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServicePasswordTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void cambiaLaContrasenaGuardandoElHash() {
        Usuario u = new Usuario();
        u.setId(4L);
        u.setPassword("hash-viejo");
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(u));
        when(passwordEncoder.encode("nueva1234")).thenReturn("hash-nuevo");

        usuarioService.cambiarPassword(4L, "nueva1234");

        assertEquals("hash-nuevo", u.getPassword());
        verify(usuarioRepository).save(u);
    }

    @Test
    void rechazaUnaContrasenaDebilSinTocarNada() {
        assertThrows(IllegalArgumentException.class, () -> usuarioService.cambiarPassword(4L, "corta"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void fallaSiElUsuarioNoExiste() {
        when(usuarioRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> usuarioService.cambiarPassword(9L, "nueva1234"));
    }
}
