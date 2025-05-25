package com.tfg.ong.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "ongs")
public class Ong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String direccion;

    private String telefono;

    private String email;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_admin")
    private Usuario admin;

    @JsonIgnore
    @OneToMany(mappedBy = "ong", cascade = CascadeType.ALL)
    private List<Factura> facturas;

    @JsonIgnore
    @OneToMany(mappedBy = "ong", cascade = CascadeType.ALL)
    private List<Ingreso> ingresos;

    @JsonIgnore
    @OneToMany(mappedBy = "ong", cascade = CascadeType.ALL)
    private List<Gasto> gastos;

    @JsonIgnore
    @OneToMany(mappedBy = "ong", cascade = CascadeType.ALL)
    private List<Reporte> reportes;

    public Ong() {
    // Constructor por defecto necesario para JPA
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Usuario getAdmin() { return admin; }
    public void setAdmin(Usuario admin) { this.admin = admin; }

    public List<Factura> getFacturas() { return facturas; }
    public void setFacturas(List<Factura> facturas) { this.facturas = facturas; }

    public List<Ingreso> getIngresos() { return ingresos; }
    public void setIngresos(List<Ingreso> ingresos) { this.ingresos = ingresos; }

    public List<Gasto> getGastos() { return gastos; }
    public void setGastos(List<Gasto> gastos) { this.gastos = gastos; }

    public List<Reporte> getReportes() { return reportes; }
    public void setReportes(List<Reporte> reportes) { this.reportes = reportes; }

    @Override
    public String toString() {
        return "Ong{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", admin=" + (admin != null ? admin.getId() : "null") +
                '}';
    }
}
