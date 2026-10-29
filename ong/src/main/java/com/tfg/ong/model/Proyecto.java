package com.tfg.ong.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Proyecto de una ONG: tiene presupuesto y agrupa los gastos e ingresos que se le asignan. */
@Entity
@Table(name = "proyectos")
public class Proyecto {

    public static final String PLANIFICADO = "PLANIFICADO";
    public static final String EN_CURSO = "EN_CURSO";
    public static final String FINALIZADO = "FINALIZADO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_ong", nullable = false)
    private Ong ong;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false, length = 20)
    private String estado = EN_CURSO;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private BigDecimal presupuesto;

    public Proyecto() {
    // Constructor por defecto necesario para JPA
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public Ong getOng() {return ong;}
    public void setOng(Ong ong) {this.ong = ong;}

    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}

    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}

    public String getEstado() {return estado;}
    public void setEstado(String estado) {this.estado = estado;}

    public LocalDate getFechaInicio() {return fechaInicio;}
    public void setFechaInicio(LocalDate fechaInicio) {this.fechaInicio = fechaInicio;}

    public LocalDate getFechaFin() {return fechaFin;}
    public void setFechaFin(LocalDate fechaFin) {this.fechaFin = fechaFin;}

    public BigDecimal getPresupuesto() {return presupuesto;}
    public void setPresupuesto(BigDecimal presupuesto) {this.presupuesto = presupuesto;}
}
