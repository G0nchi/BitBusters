package com.example.bitbusters.models;

import com.google.firebase.Timestamp;

/**
 * Modelo POJO para la colección "usuarios" en Firestore.
 * El constructor vacío es obligatorio para la deserialización de Firestore.
 */
public class Usuario {

    private String uid;
    private String nombre;
    private String email;
    private String telefono;
    private String dni;
    private String rol;
    private String fotoUrl;
    private Timestamp fechaRegistro;
    private boolean activo;

    /** Constructor vacío requerido por Firestore */
    public Usuario() {}

    public Usuario(String uid, String nombre, String email, String telefono,
                   String dni, String rol, String fotoUrl,
                   Timestamp fechaRegistro, boolean activo) {
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

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getUid() { return uid; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public String getDni() { return dni; }
    public String getRol() { return rol; }
    public String getFotoUrl() { return fotoUrl; }
    public Timestamp getFechaRegistro() { return fechaRegistro; }
    public boolean isActivo() { return activo; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setUid(String uid) { this.uid = uid; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEmail(String email) { this.email = email; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setDni(String dni) { this.dni = dni; }
    public void setRol(String rol) { this.rol = rol; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public void setFechaRegistro(Timestamp fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
