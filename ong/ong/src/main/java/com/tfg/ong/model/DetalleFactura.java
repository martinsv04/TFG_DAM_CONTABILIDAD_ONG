package com.tfg.ong.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "factura_detalle")
public class DetalleFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_factura")
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
}
