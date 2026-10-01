package com.tfg.ong.service;

import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan Pérez");
        usuario.setEmail("juan@example.com");
        usuario.setRol(Rol.ADMIN);
    }

    @Test
    public void testGetAllUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario));

        List<Usuario> usuarios = usuarioService.getAllUsuarios();

        assertEquals(1, usuarios.size());
        assertEquals("Juan Pérez", usuarios.get(0).getNombre());
    }

    @Test
    public void testGetUsuarioById() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Usuario result = usuarioService.getUsuarioById(1L);

        assertNotNull(result);
        assertEquals("Juan Pérez", result.getNombre());
    }

    @Test
    public void testCreateUsuario() {
        usuario.setPassword("secreta");
        when(passwordEncoder.encode("secreta")).thenReturn("hash-bcrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario result = usuarioService.createUsuario(usuario);

        assertNotNull(result);
        assertEquals("juan@example.com", result.getEmail());
        // Nunca se guarda la contraseña en texto plano
        assertEquals("hash-bcrypt", result.getPassword());
    }

    @Test
    public void testCreateUsuarioSinContrasenaLanzaExcepcion() {
        usuario.setPassword("  ");

        assertThrows(IllegalArgumentException.class, () -> usuarioService.createUsuario(usuario));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    public void testUpdateUsuario() {
        usuario.setNombre("Juan Actualizado");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario result = usuarioService.updateUsuario(1L, usuario);

        assertEquals("Juan Actualizado", result.getNombre());
        assertEquals(1L, result.getId());
    }

    @Test
    public void testUpdateUsuarioSinContrasenaConservaElHashAnterior() {
        Usuario existente = new Usuario();
        existente.setId(1L);
        existente.setPassword("hash-anterior");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario cambios = new Usuario();
        cambios.setNombre("Nuevo nombre");

        Usuario result = usuarioService.updateUsuario(1L, cambios);

        assertEquals("hash-anterior", result.getPassword());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    public void testUpdateUsuarioConContrasenaNuevaLaCifra() {
        when(passwordEncoder.encode("nueva")).thenReturn("hash-nuevo");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario cambios = new Usuario();
        cambios.setPassword("nueva");

        usuarioService.updateUsuario(1L, cambios);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("hash-nuevo", captor.getValue().getPassword());
        assertEquals(1L, captor.getValue().getId());
    }

    @Test
    public void testDeleteUsuario() {
        doNothing().when(usuarioRepository).deleteById(1L);

        usuarioService.deleteUsuario(1L);

        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    @Test
    void testUpdateAndSaveRol() {
        // Arrange
        Long userId = 1L;
        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setId(userId);
        usuarioExistente.setRol(Rol.VOLUNTARIO);

        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.save(Mockito.any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Usuario actualizado = usuarioService.actualizarRol(userId, Rol.CONTABLE);

        // Assert
        assertEquals(Rol.CONTABLE, actualizado.getRol());
        verify(usuarioRepository).findById(userId);
        verify(usuarioRepository).save(usuarioExistente);
    }

    @Test
    void actualizarRol_deberiaLanzarExcepcionSiNoExiste() {
        Long userId = 2L;
        when(usuarioRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            usuarioService.actualizarRol(userId, Rol.ADMIN);
        });

        assertEquals("Usuario no encontrado", ex.getMessage());
        verify(usuarioRepository).findById(userId);
        verify(usuarioRepository, never()).save(any());
    }
}

