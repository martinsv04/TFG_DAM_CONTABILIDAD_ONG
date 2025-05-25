package com.tfg.ong.model;

public class LoginResponse {
    private Long id;
    private String rol;

    public LoginResponse(Long id, String rol) {
        this.id = id;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public String getRol() {
        return rol;
    }

}
