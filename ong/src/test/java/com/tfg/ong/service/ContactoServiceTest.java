package com.tfg.ong.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tfg.ong.model.MensajeContacto;
import com.tfg.ong.repository.MensajeContactoRepository;

@ExtendWith(MockitoExtension.class)
class ContactoServiceTest {

    @Mock
    private MensajeContactoRepository repository;

    @InjectMocks
    private ContactoService service;

    @Test
    void guardaElMensajeLimpiandoEspacios() {
        when(repository.save(any(MensajeContacto.class))).thenAnswer(i -> i.getArgument(0));

        MensajeContacto mc = service.registrar("  Ana ", "  ", " ana@ong.org ", " Hola ");

        assertEquals("Ana", mc.getNombre());
        assertNull(mc.getOrganizacion());
        assertEquals("ana@ong.org", mc.getEmail());
        assertEquals("Hola", mc.getMensaje());
        verify(repository).save(mc);
    }

    @Test
    void rechazaDatosNoValidos() {
        assertThrows(IllegalArgumentException.class, () -> service.registrar("", "Org", "a@b.org", "Hola"));
        assertThrows(IllegalArgumentException.class, () -> service.registrar("Ana", "Org", "sin-arroba", "Hola"));
        assertThrows(IllegalArgumentException.class, () -> service.registrar("Ana", "Org", "a@b.org", "   "));
        assertThrows(IllegalArgumentException.class, () -> service.registrar("Ana", "Org", "a@b.org", "x".repeat(2001)));
        verify(repository, never()).save(any());
    }
}
