package com.tfg.ong.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name = "gastos")
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categoria categoria; 

    private String descripcion;
    private BigDecimal monto;

    // Cuenta contable (plan de contabilidad de entidades sin fines lucrativos), opcional; p. ej. "623"
    @Column(length = 10)
    private String cuenta;
    
    @Column(nullable = false)
    private LocalDate fecha;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_ong", nullable = false)
    private Ong ong;

    // Proyecto al que se imputa el gasto (opcional); solo se envían id y nombre
    @JsonIgnoreProperties({"descripcion", "estado", "fechaInicio", "fechaFin", "presupuesto"})
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @ManyToOne
    @JoinColumn(name = "id_proyecto")
    private Proyecto proyecto;

    public Gasto() {
    // Constructor por defecto necesario para JPA
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public Categoria getCategoria() {return categoria;}
    public void setCategoria(Categoria categoria) {this.categoria = categoria;}

    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}

    public String getCuenta() {return cuenta;}
    public void setCuenta(String cuenta) {this.cuenta = cuenta;}

    public BigDecimal getMonto() {return monto;}
    public void setMonto(BigDecimal monto) {this.monto = monto;}

    public LocalDate getFecha() {return fecha;}
    public void setFecha(LocalDate fecha) {this.fecha = fecha;}

    public Ong getOng() {return ong;}
    public void setOng(Ong ong) {this.ong = ong;}

    public Proyecto getProyecto() {return proyecto;}
    public void setProyecto(Proyecto proyecto) {this.proyecto = proyecto;}

    @Override
    public String toString() {
        return "Gasto [id=" + id + ", categoria=" + categoria + ", descripcion=" + descripcion + ", monto=" + monto
                + ", fecha=" + fecha + ", ong=" + ong + "]";
    }
}
