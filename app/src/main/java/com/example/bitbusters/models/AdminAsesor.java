package com.example.bitbusters.models;

public class AdminAsesor {
    public String id;
    public String nombre;
    public String iniciales;
    public int numSeparaciones;
    public String estado; // "Activo", "Inactivo"
    public String email;
    public String telefono;

    public AdminAsesor(String id, String nombre, String iniciales, int numSeparaciones, String estado, String email, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.iniciales = iniciales;
        this.numSeparaciones = numSeparaciones;
        this.estado = estado;
        this.email = email;
        this.telefono = telefono;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getIniciales() { return iniciales; }
    public int getNumSeparaciones() { return numSeparaciones; }
    public String getEstado() { return estado; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
}
