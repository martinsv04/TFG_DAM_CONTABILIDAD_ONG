package com.tfg.ong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.service.OngService;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OngController.class)
public class OngControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OngService ongService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private OngRepository ongRepository;

    private Ong ong;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        ong = new Ong();
        ong.setId(1L);
        ong.setNombre("ONG de Prueba");
        ong.setFechaCreacion(LocalDate.now());

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testGetAllOngs() throws Exception {
        when(ongService.getAllOngs()).thenReturn(List.of(ong));

        mockMvc.perform(get("/api/ongs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("ONG de Prueba"));
    }

    @Test
    public void testGetOngById() throws Exception {
        when(ongService.getOngById(1L)).thenReturn(ong);

        mockMvc.perform(get("/api/ongs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("ONG de Prueba"));
    }

    @Test
    public void testCreateOng() throws Exception {
        when(ongService.createOng(any(Ong.class))).thenReturn(ong);

        mockMvc.perform(post("/api/ongs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ong)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("ONG de Prueba"));
    }

    @Test
    public void testUpdateOng() throws Exception {
        when(ongRepository.findById(1L)).thenReturn(Optional.of(ong));
        when(ongRepository.save(any(Ong.class))).thenReturn(ong);

        mockMvc.perform(put("/api/ongs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ong)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("ONG de Prueba"));
    }

    @Test
    public void testDeleteOng() throws Exception {
        doNothing().when(ongService).deleteOng(1L);

        mockMvc.perform(delete("/api/ongs/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetOngsByAdminId() throws Exception {
        when(ongRepository.findByAdminId(1L)).thenReturn(List.of(ong));

        mockMvc.perform(get("/api/ongs/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("ONG de Prueba"));
    }

    @Test
    public void testGetOngsForUser_Admin() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setRol(Rol.ADMIN);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(ongRepository.findByAdminId(1L)).thenReturn(List.of(ong));

        mockMvc.perform(get("/api/ongs/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("ONG de Prueba"));
    }

    @Test
    public void testGetOngsForUser_OtroRolConOng() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setRol(Rol.DONANTE);
        usuario.setOng(ong);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/api/ongs/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("ONG de Prueba"));
    }


    @Test
    public void testGetOngsForUser_NotFound() throws Exception {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/ongs/usuario/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateOng_OngExists() throws Exception {
        Ong existingOng = new Ong();
        existingOng.setId(1L);
        existingOng.setNombre("Antigua");
        existingOng.setDescripcion("Desc antigua");
        existingOng.setDireccion("Dir");
        existingOng.setTelefono("123");
        existingOng.setEmail("ong@old.com");

        Ong updatedOng = new Ong();
        updatedOng.setNombre("Nueva");
        updatedOng.setDescripcion("Desc nueva");
        updatedOng.setDireccion("Dir nueva");
        updatedOng.setTelefono("456");
        updatedOng.setEmail("ong@new.com");

        when(ongRepository.findById(1L)).thenReturn(Optional.of(existingOng));
        when(ongRepository.save(any(Ong.class))).thenReturn(existingOng);

        mockMvc.perform(put("/api/ongs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updatedOng)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nueva"))
                .andExpect(jsonPath("$.email").value("ong@new.com"));
    }

    @Test
    public void testUpdateOng_OngNotFound() throws Exception {
        Ong updatedOng = new Ong();
        updatedOng.setNombre("Nueva");

        when(ongRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/ongs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedOng)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetOngsForUser_OtroRolSinOng() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setRol(Rol.VOLUNTARIO);
        usuario.setOng(null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/api/ongs/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

}
