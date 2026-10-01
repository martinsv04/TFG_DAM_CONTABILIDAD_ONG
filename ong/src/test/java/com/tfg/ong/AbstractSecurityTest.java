package com.tfg.ong;

import com.tfg.ong.security.OngAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Base de los tests de controladores (@WebMvcTest).
 * - Importa una seguridad de pruebas que no bloquea peticiones.
 * - Ejecuta cada test como un ADMIN autenticado (ver @WithMockAuthUser).
 * - Sustituye OngAccessService por un mock: las comprobaciones "¿es tu ONG?" no hacen nada
 *   y filtrar() devuelve la lista tal cual, de modo que el test comprueba solo el controlador.
 */
@Import(TestSecurityConfig.class)
@WithMockAuthUser
public abstract class AbstractSecurityTest {

    @MockitoBean
    protected OngAccessService ongAccess;

    @BeforeEach
    protected void dejarPasarListasSinFiltrar() {
        when(ongAccess.filtrar(any(), anyList(), any())).thenAnswer(invocacion -> invocacion.getArgument(1));
    }
}
