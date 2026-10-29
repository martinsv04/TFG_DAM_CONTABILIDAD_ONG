package com.tfg.ong.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Subvención solicitada o concedida a la ONG por un financiador (ayuntamiento, fundación...). */
@Entity
@Table(name = "subvenciones")
public class Subvencion {

    public static final String SOLICITADA = "SOLICITADA";
    public static final String CONCEDIDA = "CONCEDIDA";
    public static final String DENEGADA = "DENEGADA";
    public static final String JUSTIFICADA = "JUSTIFICADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_ong", nullable = false)
    private Ong ong;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 150)
    private String financiador;

    @Column(length = 60)
    private String expediente;

    @Column(nullable = false, length = 20)
    private String estado = SOLICITADA;

    private BigDecimal importeSolicitado;
    private BigDecimal importeConcedido;

    private LocalDate fechaSolicitud;
    private LocalDate fechaConcesion;
    /** Plazo límite para justificar el gasto ante el financiador. */
    private LocalDate fechaJustificacion;

    // Proyecto que financia; si se borra el proyecto, la subvención se conserva sin proyecto
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @ManyToOne
    @JoinColumn(name = "id_proyecto")
    private Proyecto proyecto;

    public Subvencion() {
    // Constructor por defecto necesario para JPA
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public Ong getOng() {return ong;}
    public void setOng(Ong ong) {this.ong = ong;}

    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}

    public String getFinanciador() {return financiador;}
    public void setFinanciador(String financiador) {this.financiador = financiador;}

    public String getExpediente() {return expediente;}
    public void setExpediente(String expediente) {this.expediente = expediente;}

    public String getEstado() {return estado;}
    public void setEstado(String estado) {this.estado = estado;}

    public BigDecimal getImporteSolicitado() {return importeSolicitado;}
    public void setImporteSolicitado(BigDecimal importeSolicitado) {this.importeSolicitado = importeSolicitado;}

    public BigDecimal getImporteConcedido() {return importeConcedido;}
    public void setImporteConcedido(BigDecimal importeConcedido) {this.importeConcedido = importeConcedido;}

    public LocalDate getFechaSolicitud() {return fechaSolicitud;}
    public void setFechaSolicitud(LocalDate fechaSolicitud) {this.fechaSolicitud = fechaSolicitud;}

    public LocalDate getFechaConcesion() {return fechaConcesion;}
    public void setFechaConcesion(LocalDate fechaConcesion) {this.fechaConcesion = fechaConcesion;}

    public LocalDate getFechaJustificacion() {return fechaJustificacion;}
    public void setFechaJustificacion(LocalDate fechaJustificacion) {this.fechaJustificacion = fechaJustificacion;}

    public Proyecto getProyecto() {return proyecto;}
    public void setProyecto(Proyecto proyecto) {this.proyecto = proyecto;}
}
