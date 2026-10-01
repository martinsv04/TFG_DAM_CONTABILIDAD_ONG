package com.tfg.ong.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;

@Entity
@Table(name = "facturas")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @ManyToOne
    @JoinColumn(name = "id_ong", nullable = false)
    private Ong ong;

    // Del usuario solo se envían id y nombre (sin email, teléfono, NIF/CIF, etc.)
    @JsonIgnoreProperties({"email", "telefono", "nifCif", "creadoEn", "ong", "rol"})
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = true)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private BigDecimal total;

    // Movimiento (ingreso o gasto) del que procede la factura; solo uno de los dos está informado
    @JsonIgnoreProperties({"ong"})
    @ManyToOne
    @JoinColumn(name = "id_ingreso", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Ingreso ingreso;

    @JsonIgnoreProperties({"ong"})
    @ManyToOne
    @JoinColumn(name = "id_gasto", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Gasto gasto;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "factura-detalles")
    private List<DetalleFactura> detalles;

    public Factura() {
    // Constructor por defecto necesario para JPA
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public Ong getOng() { return ong; }
    public void setOng(Ong ong) { this.ong = ong; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public Ingreso getIngreso() { return ingreso; }
    public void setIngreso(Ingreso ingreso) { this.ingreso = ingreso; }

    public Gasto getGasto() { return gasto; }
    public void setGasto(Gasto gasto) { this.gasto = gasto; }

    public List<DetalleFactura> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleFactura> detalles) { this.detalles = detalles; }

    @Override
    public String toString() {
        return "Factura [id=" + id + ", numero=" + numero + ", ong=" + ong + ", usuario=" + usuario + ", fecha=" + fecha
                + ", total=" + total + "]";
    }
}
