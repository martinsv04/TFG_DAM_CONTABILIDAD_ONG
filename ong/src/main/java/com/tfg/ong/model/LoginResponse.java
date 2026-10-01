package com.tfg.ong.model;

public class LoginResponse {
    private Long id;
    private String rol;
    private String token;

    public LoginResponse(Long id, String rol) {
        this(id, rol, null);
    }

    public LoginResponse(Long id, String rol, String token) {
        this.id = id;
        this.rol = rol;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public String getRol() {
        return rol;
    }

    public String getToken() {
        return token;
    }

}
