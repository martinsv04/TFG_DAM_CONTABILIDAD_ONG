package com.tfg.ong.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "reportes")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoReporte tipo;  // Balance General, Estado de Resultados, Flujo de Efectivo

    @Column(nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    public Reporte() {}

    // Getters y Setters
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public TipoReporte getTipo() {return tipo;}
    public void setTipo(TipoReporte tipo) {this.tipo = tipo;}

    public LocalDateTime getFechaGeneracion() {return fechaGeneracion;}
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {this.fechaGeneracion = fechaGeneracion;}

    public String getContenido() {return contenido;}
    public void setContenido(String contenido) {this.contenido = contenido;}

    @Override
    public String toString() {
        return "Reporte [id=" + id + ", tipo=" + tipo + ", fechaGeneracion=" + fechaGeneracion + ", contenido="
                + contenido + "]";
    }
}
