package com.tfg.ong.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    
    @Column(nullable = false)
    private LocalDate fecha;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_ong", nullable = false)
    private Ong ong;

    public Gasto() {
    // Constructor por defecto necesario para JPA
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public Categoria getCategoria() {return categoria;}
    public void setCategoria(Categoria categoria) {this.categoria = categoria;}

    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}

    public BigDecimal getMonto() {return monto;}
    public void setMonto(BigDecimal monto) {this.monto = monto;}

    public LocalDate getFecha() {return fecha;}
    public void setFecha(LocalDate fecha) {this.fecha = fecha;}

    public Ong getOng() {return ong;}
    public void setOng(Ong ong) {this.ong = ong;}

    @Override
    public String toString() {
        return "Gasto [id=" + id + ", categoria=" + categoria + ", descripcion=" + descripcion + ", monto=" + monto
                + ", fecha=" + fecha + ", ong=" + ong + "]";
    }
}
