package com.maryNotebook.maryNotebook.usuario.dto;

import com.maryNotebook.maryNotebook.usuario.entity.Usuario;

public class RegistroUsuarioDTO {
    private String nombre;
    private String email;
    private String password;
    private Usuario.Rol rol;

    public RegistroUsuarioDTO() {
    }

    public RegistroUsuarioDTO(String nombre, String email, String password) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Usuario.Rol getRol() {
        return rol;
    }

    public void setRol(Usuario.Rol rol) {
        this.rol = rol;
    }
}
