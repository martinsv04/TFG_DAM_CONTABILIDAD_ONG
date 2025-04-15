package com.tfg.ong.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "ongs")
public class Ong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "id_admin", nullable = false)
    private Usuario admin; // Usuario con rol ADMIN que gestiona esta ONG

    // Relación con facturas
    @OneToMany(mappedBy = "ong", cascade = CascadeType.ALL)
    private List<Factura> facturas;

    // Relación con ingresos
    @OneToMany(mappedBy = "ong", cascade = CascadeType.ALL)
    private List<Ingreso> ingresos;

    // Relación con gastos
    @OneToMany(mappedBy = "ong", cascade = CascadeType.ALL)
    private List<Gasto> gastos;

    // Relación con reportes
    @OneToMany(mappedBy = "ong", cascade = CascadeType.ALL)
    private List<Reporte> reportes;

    public Ong() {}

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}

    public Usuario getAdmin() {return admin;}
    public void setAdmin(Usuario admin) {this.admin = admin;}

    public List<Factura> getFacturas() {return facturas;}
    public void setFacturas(List<Factura> facturas) {this.facturas = facturas;}

    public List<Ingreso> getIngresos() {return ingresos;}
    public void setIngresos(List<Ingreso> ingresos) {this.ingresos = ingresos;}

    public List<Gasto> getGastos() {return gastos;}
    public void setGastos(List<Gasto> gastos) {this.gastos = gastos;}

    public List<Reporte> getReportes() {return reportes;}
    public void setReportes(List<Reporte> reportes) {this.reportes = reportes;}

    @Override
    public String toString() {
        return "Ong [id=" + id + ", nombre=" + nombre + ", admin=" + admin.getId() + "]";
    }
}
