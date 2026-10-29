package com.tfg.ong.model;


import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Ingreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_ong")
    private Ong ong;

    // Del usuario solo se envían id y nombre
    @JsonIgnoreProperties({"email", "telefono", "nifCif", "direccion", "codigoPostal", "creadoEn", "ong", "rol"})
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = true)
    private Usuario usuario;

    // Proyecto y subvención a los que se asigna el ingreso (opcionales); solo se envían id y nombre
    @JsonIgnoreProperties({"descripcion", "estado", "fechaInicio", "fechaFin", "presupuesto"})
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @ManyToOne
    @JoinColumn(name = "id_proyecto")
    private Proyecto proyecto;

    @JsonIgnoreProperties({"financiador", "expediente", "estado", "importeSolicitado", "importeConcedido",
            "fechaSolicitud", "fechaConcesion", "fechaJustificacion", "proyecto"})
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @ManyToOne
    @JoinColumn(name = "id_subvencion")
    private Subvencion subvencion;

    private BigDecimal monto;
    private LocalDate fecha;
    private String tipo;
    private String descripcion;

    // Cuenta contable (plan de contabilidad de entidades sin fines lucrativos), opcional; p. ej. "720"
    @jakarta.persistence.Column(length = 10)
    private String cuenta;

    public Ingreso() {
    // Constructor por defecto necesario para JPA
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public Ong getOng() {return ong;}
    public void setOng(Ong ong) {this.ong = ong;}

    public Usuario getUsuario() {return usuario;}
    public void setUsuario(Usuario usuario) {this.usuario = usuario;}

    public Proyecto getProyecto() {return proyecto;}
    public void setProyecto(Proyecto proyecto) {this.proyecto = proyecto;}

    public Subvencion getSubvencion() {return subvencion;}
    public void setSubvencion(Subvencion subvencion) {this.subvencion = subvencion;}

    public BigDecimal getMonto() {return monto;}
    public void setMonto(BigDecimal monto) {this.monto = monto;}

    public LocalDate getFecha() {return fecha;}
    public void setFecha(LocalDate fecha) {this.fecha = fecha;}

    public String getTipo() {return tipo;}
    public void setTipo(String tipo) {this.tipo = tipo;}

    public String getCuenta() {return cuenta;}
    public void setCuenta(String cuenta) {this.cuenta = cuenta;}

    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    
}

