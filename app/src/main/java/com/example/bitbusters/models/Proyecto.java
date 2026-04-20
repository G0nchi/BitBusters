package com.example.bitbusters.models;

public class Proyecto {
    public String nombre;
    public String precio;
    public String rating;
    public String ubicacion;

    public Proyecto(String nombre, String precio, String rating, String ubicacion) {
        this.nombre   = nombre;
        this.precio   = precio;
        this.rating   = rating;
        this.ubicacion = ubicacion;
    }
}
