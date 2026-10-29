package com.tfg.ong.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Una línea del registro de actividad: quién hizo qué y cuándo.
 * No tiene claves foráneas a propósito: el historial se conserva aunque se borre después el usuario o el elemento.
 */
@Entity
@Table(name = "auditoria", indexes = {@Index(name = "idx_auditoria_ong_fecha", columnList = "id_ong, fecha")})
public class RegistroAuditoria {

    public static final String CREAR = "CREAR";
    public static final String EDITAR = "EDITAR";
    public static final String ELIMINAR = "ELIMINAR";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "id_ong")
    private Long ongId;

    private Long usuarioId;

    @Column(length = 150)
    private String usuarioNombre;

    @Column(length = 20)
    private String usuarioRol;

    /** INGRESO, GASTO, FACTURA, PROYECTO o SUBVENCION. */
    @Column(nullable = false, length = 20)
    private String entidad;

    private Long entidadId;

    /** CREAR, EDITAR o ELIMINAR. */
    @Column(nullable = false, length = 10)
    private String accion;

    @Column(length = 600)
    private String descripcion;

    public RegistroAuditoria() {
    // Constructor por defecto necesario para JPA
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public LocalDateTime getFecha() {return fecha;}
    public void setFecha(LocalDateTime fecha) {this.fecha = fecha;}

    public Long getOngId() {return ongId;}
    public void setOngId(Long ongId) {this.ongId = ongId;}

    public Long getUsuarioId() {return usuarioId;}
    public void setUsuarioId(Long usuarioId) {this.usuarioId = usuarioId;}

    public String getUsuarioNombre() {return usuarioNombre;}
    public void setUsuarioNombre(String usuarioNombre) {this.usuarioNombre = usuarioNombre;}

    public String getUsuarioRol() {return usuarioRol;}
    public void setUsuarioRol(String usuarioRol) {this.usuarioRol = usuarioRol;}

    public String getEntidad() {return entidad;}
    public void setEntidad(String entidad) {this.entidad = entidad;}

    public Long getEntidadId() {return entidadId;}
    public void setEntidadId(Long entidadId) {this.entidadId = entidadId;}

    public String getAccion() {return accion;}
    public void setAccion(String accion) {this.accion = accion;}

    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
}
