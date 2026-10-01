package com.tfg.ong.security;

import com.tfg.ong.WithMockAuthUser;
import com.tfg.ong.config.SecurityConfig;
import com.tfg.ong.controller.GastoController;
import com.tfg.ong.controller.UsuarioController;
import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.GastoRepository;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.repository.UsuarioRepository;
import com.tfg.ong.service.GastoService;
import com.tfg.ong.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba las reglas reales de SecurityConfig (quién puede llamar a qué ruta) y el filtro JWT.
 * Los controladores y servicios están simulados: aquí solo interesa el 200, 401 o 403.
 */
@WebMvcTest(controllers = {UsuarioController.class, GastoController.class})
@Import(SecurityConfig.class)
class SecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private OngRepository ongRepository;

    @MockitoBean
    private GastoService gastoService;

    @MockitoBean
    private GastoRepository gastoRepository;

    @MockitoBean
    private OngAccessService ongAccess;

    @BeforeEach
    void dejarPasarListasSinFiltrar() {
        when(ongAccess.filtrar(any(), anyList(), any())).thenAnswer(inv -> inv.getArgument(1));
    }

    @Test
    void sinTokenSeRechazaConUnauthorized() throws Exception {
        mockMvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/gastos")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockAuthUser(rol = Rol.ADMIN)
    void adminPuedeListarUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios")).andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser(rol = Rol.CONTABLE)
    void contableNoPuedeGestionarUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockAuthUser(rol = Rol.CONTABLE)
    void contablePuedeVerGastosPeroNoBorrarlos() throws Exception {
        mockMvc.perform(get("/api/gastos")).andExpect(status().isOk());
        mockMvc.perform(delete("/api/gastos/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockAuthUser(rol = Rol.ADMIN)
    void adminPuedeBorrarGastos() throws Exception {
        mockMvc.perform(delete("/api/gastos/1")).andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser(rol = Rol.DONANTE)
    void donanteNoPuedeVerLosGastos() throws Exception {
        mockMvc.perform(get("/api/gastos")).andExpect(status().isForbidden());
    }

    @Test
    void conUnTokenValidoSeAplicaElRolDelUsuarioDeLaBaseDeDatos() throws Exception {
        Usuario contable = new Usuario();
        contable.setId(5L);
        contable.setEmail("contable@example.com");
        contable.setRol(Rol.CONTABLE);

        when(jwtService.extractUserId("token-valido")).thenReturn(Optional.of(5L));
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(contable));

        mockMvc.perform(get("/api/gastos").header("Authorization", "Bearer token-valido"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer token-valido"))
                .andExpect(status().isForbidden());
    }

    @Test
    void conUnTokenInvalidoSeRechazaConUnauthorized() throws Exception {
        when(jwtService.extractUserId("token-malo")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/gastos").header("Authorization", "Bearer token-malo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void siElUsuarioDelTokenYaNoExisteSeRechazaConUnauthorized() throws Exception {
        when(jwtService.extractUserId("token-de-usuario-borrado")).thenReturn(Optional.of(99L));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/gastos").header("Authorization", "Bearer token-de-usuario-borrado"))
                .andExpect(status().isUnauthorized());
    }
}
