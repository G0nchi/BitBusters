package com.example.bitbusters.models;

public class ClienteInmobiliaria {
    private final String id;
    private final String nombre;
    private final String descripcion;
    private final int totalProyectos;

    public ClienteInmobiliaria(String id, String nombre, String descripcion, int totalProyectos) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.totalProyectos = totalProyectos;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getTotalProyectos() { return totalProyectos; }
}
