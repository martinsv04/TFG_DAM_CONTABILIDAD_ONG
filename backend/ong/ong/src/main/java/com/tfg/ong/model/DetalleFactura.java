package com.tfg.ong.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "detalles_factura")
@SuppressWarnings("unused")
public class DetalleFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_factura")
    @JsonBackReference
    private Factura factura;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private Integer cantidad = 1;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(nullable = false)
    private BigDecimal iva = BigDecimal.ZERO;

    public DetalleFactura() {}

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public Factura getFactura() {return factura;}
    public void setFactura(Factura factura) {this.factura = factura;}

    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}

    public Integer getCantidad() {return cantidad;}
    public void setCantidad(Integer cantidad) {this.cantidad = cantidad;}

    public BigDecimal getPrecio() {return precio;}
    public void setPrecio(BigDecimal precio) {this.precio = precio;}

    public BigDecimal getIva() {return iva;}
    public void setIva(BigDecimal iva) {this.iva = iva;}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((factura == null) ? 0 : factura.hashCode());
        result = prime * result + ((descripcion == null) ? 0 : descripcion.hashCode());
        result = prime * result + ((cantidad == null) ? 0 : cantidad.hashCode());
        result = prime * result + ((precio == null) ? 0 : precio.hashCode());
        result = prime * result + ((iva == null) ? 0 : iva.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DetalleFactura other = (DetalleFactura) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (factura == null) {
            if (other.factura != null)
                return false;
        } else if (!factura.equals(other.factura))
            return false;
        if (descripcion == null) {
            if (other.descripcion != null)
                return false;
        } else if (!descripcion.equals(other.descripcion))
            return false;
        if (cantidad == null) {
            if (other.cantidad != null)
                return false;
        } else if (!cantidad.equals(other.cantidad))
            return false;
        if (precio == null) {
            if (other.precio != null)
                return false;
        } else if (!precio.equals(other.precio))
            return false;
        if (iva == null) {
            if (other.iva != null)
                return false;
        } else if (!iva.equals(other.iva))
            return false;
        return true;
    }

    
}
