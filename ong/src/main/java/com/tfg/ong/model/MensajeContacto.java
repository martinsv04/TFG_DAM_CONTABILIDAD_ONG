package com.tfg.ong.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Mensaje enviado desde el formulario de contacto de la web (público). */
@Entity
@Table(name = "mensajes_contacto")
public class MensajeContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 150)
    private String organizacion;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(nullable = false, length = 2000)
    private String mensaje;

    public MensajeContacto() {
    // Constructor por defecto necesario para JPA
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public LocalDateTime getFecha() {return fecha;}
    public void setFecha(LocalDateTime fecha) {this.fecha = fecha;}

    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}

    public String getOrganizacion() {return organizacion;}
    public void setOrganizacion(String organizacion) {this.organizacion = organizacion;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public String getMensaje() {return mensaje;}
    public void setMensaje(String mensaje) {this.mensaje = mensaje;}
}
