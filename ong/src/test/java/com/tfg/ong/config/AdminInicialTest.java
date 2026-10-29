package com.tfg.ong.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.UsuarioRepository;

class AdminInicialTest {

    private final UsuarioRepository repo = mock(UsuarioRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void creaElAdministradorSiNoHayUsuarios() {
        when(repo.count()).thenReturn(0L);

        new AdminInicial(repo, encoder, " Admin@Ejemplo.org ", "clave12345").run();

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repo).save(captor.capture());
        Usuario u = captor.getValue();
        assertEquals("admin@ejemplo.org", u.getEmail());
        assertEquals(Rol.ADMIN, u.getRol());
        assertTrue(encoder.matches("clave12345", u.getPassword()));
    }

    @Test
    void noHaceNadaSiYaHayUsuariosOFaltanVariables() {
        when(repo.count()).thenReturn(3L);
        new AdminInicial(repo, encoder, "a@b.org", "clave12345").run();
        new AdminInicial(repo, encoder, "", "").run();
        verify(repo, never()).save(any());
    }

    @Test
    void noCreaElAdministradorConUnaContrasenaDebil() {
        when(repo.count()).thenReturn(0L);
        new AdminInicial(repo, encoder, "a@b.org", "corta").run();
        verify(repo, never()).save(any());
    }
}
