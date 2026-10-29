package com.tfg.ong.service;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.tfg.ong.model.MensajeContacto;
import com.tfg.ong.repository.MensajeContactoRepository;

/** Validación y guardado de los mensajes del formulario de contacto. */
@Service
public class ContactoService {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");

    private final MensajeContactoRepository repository;

    public ContactoService(MensajeContactoRepository repository) {
        this.repository = repository;
    }

    private static String limpio(String valor) {
        return valor == null ? "" : valor.trim();
    }

    public MensajeContacto registrar(String nombre, String organizacion, String email, String mensaje) {
        String n = limpio(nombre);
        String o = limpio(organizacion);
        String e = limpio(email);
        String m = limpio(mensaje);

        if (n.isEmpty() || n.length() > 150) {
            throw new IllegalArgumentException("Indica tu nombre (máximo 150 caracteres)");
        }
        if (o.length() > 150) {
            throw new IllegalArgumentException("El nombre de la organización es demasiado largo");
        }
        if (e.length() > 254 || !EMAIL.matcher(e).matches()) {
            throw new IllegalArgumentException("Indica un correo electrónico válido");
        }
        if (m.isEmpty() || m.length() > 2000) {
            throw new IllegalArgumentException("Escribe un mensaje (máximo 2000 caracteres)");
        }

        MensajeContacto mc = new MensajeContacto();
        mc.setFecha(LocalDateTime.now());
        mc.setNombre(n);
        mc.setOrganizacion(o.isEmpty() ? null : o);
        mc.setEmail(e);
        mc.setMensaje(m);
        return repository.save(mc);
    }
}
