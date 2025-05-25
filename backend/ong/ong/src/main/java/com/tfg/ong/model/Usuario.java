package com.tfg.ong.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

   
    private String telefono;
    private String nifCif;

    @Column(name = "creado_en")
    private LocalDate creadoEn;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_ong")
    private Ong ong;

    @SuppressWarnings("unused")
    public Usuario() {
    // Constructor por defecto necesario para JPA
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

    public Rol getRol() {return rol;}
    public void setRol(Rol rol) {this.rol = rol;}

    public String getTelefono() {return telefono;}
    public void setTelefono(String telefono) {this.telefono = telefono;}

    public String getNifCif() {return nifCif;}
    public void setNifCif(String nifCif) {this.nifCif = nifCif;}

    public LocalDate getCreadoEn() {return creadoEn;}
    public void setCreadoEn(LocalDate creadoEn) {this.creadoEn = creadoEn;}

    public Ong getOng() {return ong;}
    public void setOng(Ong ong) {this.ong = ong;}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        Usuario other = (Usuario) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
