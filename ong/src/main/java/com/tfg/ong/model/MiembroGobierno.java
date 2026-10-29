package com.tfg.ong.model;

import java.time.LocalDate;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

/** Persona del órgano de gobierno (patronato, junta...) o del equipo de dirección de una ONG. */
@Entity
@Table(name = "miembros_gobierno")
public class MiembroGobierno {

    public static final String GOBIERNO = "GOBIERNO";
    public static final String DIRECCION = "DIRECCION";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_ong", nullable = false)
    private Ong ong;

    @Column(nullable = false, length = 120)
    private String nombre;

    /** Presidente, secretario, vocal, director/a... (texto libre). */
    @Column(nullable = false, length = 80)
    private String cargo;

    /** GOBIERNO (patronato o junta) o DIRECCION (equipo directivo). */
    @Column(nullable = false, length = 12)
    private String organo = GOBIERNO;

    @Column(name = "fecha_nombramiento")
    private LocalDate fechaNombramiento;

    @Column(length = 500)
    private String descripcion;

    private Integer orden;

    public MiembroGobierno() {
    // Constructor por defecto necesario para JPA
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public Ong getOng() {return ong;}
    public void setOng(Ong ong) {this.ong = ong;}

    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}

    public String getCargo() {return cargo;}
    public void setCargo(String cargo) {this.cargo = cargo;}

    public String getOrgano() {return organo;}
    public void setOrgano(String organo) {this.organo = organo;}

    public LocalDate getFechaNombramiento() {return fechaNombramiento;}
    public void setFechaNombramiento(LocalDate fechaNombramiento) {this.fechaNombramiento = fechaNombramiento;}

    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}

    public Integer getOrden() {return orden;}
    public void setOrden(Integer orden) {this.orden = orden;}
}
