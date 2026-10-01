package com.tfg.ong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfg.ong.TestSecurityConfig;
import com.tfg.ong.model.LoginRequest;
import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.UsuarioRepository;
import com.tfg.ong.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private JwtService jwtService;

    private ObjectMapper objectMapper;
    private Usuario usuario;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("usuario@example.com");
        // En la base de datos la contraseña se guarda cifrada con BCrypt
        usuario.setPassword(new BCryptPasswordEncoder().encode("password123"));
        usuario.setRol(Rol.ADMIN);
    }

    @Test
    public void testLoginExitoso() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("usuario@example.com");
        loginRequest.setPassword("password123");

        when(usuarioRepository.findByEmail("usuario@example.com"))
                .thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("token-de-prueba");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.token").value("token-de-prueba"));
    }

    @Test
    public void testLoginContrasenaIncorrecta() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("usuario@example.com");
        loginRequest.setPassword("incorrecta");

        when(usuarioRepository.findByEmail("usuario@example.com"))
                .thenReturn(Optional.of(usuario));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLoginUsuarioNoExiste() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("noexiste@example.com");
        loginRequest.setPassword("cualquier");

        when(usuarioRepository.findByEmail("noexiste@example.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLoginSinEmailNiContrasena() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLoginNoAceptaContrasenaEnTextoPlanoGuardada() throws Exception {
        // Si en la base de datos quedara una contraseña sin cifrar, el login debe fallar
        usuario.setPassword("password123");
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("usuario@example.com");
        loginRequest.setPassword("password123");

        when(usuarioRepository.findByEmail("usuario@example.com"))
                .thenReturn(Optional.of(usuario));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
