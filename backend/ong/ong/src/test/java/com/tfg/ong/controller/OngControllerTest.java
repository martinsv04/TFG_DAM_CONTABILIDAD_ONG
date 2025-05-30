package com.tfg.ong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.service.OngService;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.repository.UsuarioRepository;

import com.tfg.ong.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
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
    private UsuarioService usuarioService;

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

    @Test
    void getMiembrosDeOng_deberiaRetornarMiembrosConAdmin() throws Exception {
        Long ongId = 1L;

        Usuario admin = new Usuario();
        admin.setId(100L);
        admin.setNombre("Admin");

        Usuario miembro1 = new Usuario();
        miembro1.setId(101L);
        miembro1.setNombre("Miembro");

        Ong ong = new Ong();
        ong.setId(ongId);
        ong.setAdmin(admin);

        when(ongRepository.findById(ongId)).thenReturn(Optional.of(ong));
        when(usuarioRepository.findByOngId(ongId)).thenReturn(new ArrayList<>(List.of(miembro1)));

        mockMvc.perform(get("/api/ongs/{id}/miembros", ongId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(admin.getId()))
                .andExpect(jsonPath("$[1].id").value(miembro1.getId()));
    }


    @Test
    void TestGetMembersThrowsNotFound() throws Exception {
        Long ongId = 2L;
        when(ongRepository.findById(ongId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/ongs/{id}/miembros", ongId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMiembrosDeOng_noDebeAgregarAdminSiYaEstaEnLaLista() throws Exception {
        Long ongId = 1L;

        Usuario admin = new Usuario();
        admin.setId(100L);
        admin.setNombre("Admin");

        List<Usuario> miembros = new ArrayList<>();
        miembros.add(admin);

        Ong ong = new Ong();
        ong.setId(ongId);
        ong.setAdmin(admin);

        when(ongRepository.findById(ongId)).thenReturn(Optional.of(ong));
        when(usuarioRepository.findByOngId(ongId)).thenReturn(miembros);

        mockMvc.perform(get("/api/ongs/{id}/miembros", ongId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(admin.getId()));
    }

    @Test
    void getMiembrosDeOng_adminEsNull() throws Exception {
        Long ongId = 1L;

        Usuario miembro1 = new Usuario();
        miembro1.setId(101L);
        miembro1.setNombre("Miembro");

        Ong ong = new Ong();
        ong.setId(ongId);
        ong.setAdmin(null);

        when(ongRepository.findById(ongId)).thenReturn(Optional.of(ong));
        when(usuarioRepository.findByOngId(ongId)).thenReturn(List.of(miembro1));

        mockMvc.perform(get("/api/ongs/{id}/miembros", ongId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(miembro1.getId()));
    }


}


