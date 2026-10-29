package com.tfg.ong.model;

import org.hibernate.Length;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

    // Datos fiscales de la entidad (aparecen en los certificados de donación)
    private String nif;

    private String representante;

    // Imágenes para los certificados (data URL en base64). No se envían con los datos de la ONG: tienen sus propios endpoints.
    @JsonIgnore
    @Column(length = Length.LONG32)
    private String logo;

    @JsonIgnore
    @Column(length = Length.LONG32)
    private String firma;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    // true = la página pública de transparencia de esta ONG está publicada
    @Column(name = "transparencia_publica")
    private Boolean transparenciaPublica;


    // Del administrador solo se envían id y nombre (sin email, teléfono, NIF/CIF, etc.)
    @JsonIgnoreProperties({"email", "telefono", "nifCif", "direccion", "codigoPostal", "creadoEn", "ong", "rol"})
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

    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }

    public String getRepresentante() { return representante; }
    public void setRepresentante(String representante) { this.representante = representante; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getFirma() { return firma; }
    public void setFirma(String firma) { this.firma = firma; }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Boolean getTransparenciaPublica() { return transparenciaPublica; }
    public void setTransparenciaPublica(Boolean transparenciaPublica) { this.transparenciaPublica = transparenciaPublica; }

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
