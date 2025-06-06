package com.tfg.ong.service;

import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

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
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario result = usuarioService.createUsuario(usuario);

        assertNotNull(result);
        assertEquals("juan@example.com", result.getEmail());
    }

    @Test
    public void testUpdateUsuario() {
        usuario.setNombre("Juan Actualizado");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario result = usuarioService.updateUsuario(1L, usuario);

        assertEquals("Juan Actualizado", result.getNombre());
        assertEquals(1L, result.getId());
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

