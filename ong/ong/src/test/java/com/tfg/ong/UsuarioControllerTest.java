package com.tfg.ong;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.tfg.ong.controller.UsuarioController;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.service.UsuarioService;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest extends AbstractControllerTest {

    @MockitoBean
    private UsuarioService usuarioService;

    private Usuario usuario1;
    private List<Usuario> usuarios;

    @BeforeEach
    void initData() {
        initObject();
    }

    @Test
    void testGetAllUsuarios() throws Exception {
        // Simulamos la respuesta del servicio con la lista de usuarios
        when(usuarioService.getAllUsuarios()).thenReturn(usuarios);

        // Realizamos la solicitud GET a la API y verificamos la respuesta
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())  // Verificamos que la respuesta sea 200 OK
                .andExpect(jsonPath("$.length()").value(usuarios.size()))  // Verificamos la longitud de la lista de usuarios
                .andExpect(jsonPath("$[0].nombre").value("Juan"));  // Verificamos que el primer usuario tenga el nombre "Juan"
    }

    // Método para inicializar los objetos
    void initObject() {
        // Crear usuario1
        usuario1 = new Usuario();
        usuario1.setNombre("Juan");
        usuario1.setEmail("juanito@gmail.com");
        usuario1.setTelefono("123456789");
        usuario1.setNifCif("12345678A");

        // Inicializar la lista de usuarios con el usuario1
        usuarios = Arrays.asList(usuario1);
    }
}
