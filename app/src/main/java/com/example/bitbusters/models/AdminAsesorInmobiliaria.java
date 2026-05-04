package com.example.bitbusters.models;

public class AdminAsesorInmobiliaria {
    public String id;
    public String nombre;
    public String iniciales;
    public String email;
    public String telefono;
    public String estado; // "Activo", "Inactivo"

    public AdminAsesorInmobiliaria(String id, String nombre, String iniciales, String email, String telefono, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.iniciales = iniciales;
        this.email = email;
        this.telefono = telefono;
        this.estado = estado;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getIniciales() { return iniciales; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public String getEstado() { return estado; }
}
