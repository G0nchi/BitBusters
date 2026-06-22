package com.example.bitbusters.models;

import com.google.firebase.Timestamp;

public class Usuario {

    private String uid;
    private String nombre;
    private String email;
    private String telefono;
    private String dni;
    private String rol;
    private String fotoUrl;
    private Timestamp fechaRegistro;
    private Boolean activo;

    public Usuario() {
        // Required empty constructor for Firestore serialization.
    }

    public Usuario(String uid, String nombre, String email, String telefono, String dni,
                   String rol, String fotoUrl, Timestamp fechaRegistro, Boolean activo) {
        this.uid = uid;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.dni = dni;
        this.rol = rol;
        this.fotoUrl = fotoUrl;
        this.fechaRegistro = fechaRegistro;
        this.activo = activo;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
