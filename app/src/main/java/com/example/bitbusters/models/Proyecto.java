package com.example.bitbusters.models;

public class Proyecto {
    public String nombre;
    public String precio;
    public String rating;
    public String ubicacion;
    public String tipo;
    public String imageUrl;


    public Proyecto(String nombre, String precio, String rating, String ubicacion, String tipo, String imageUrl) {
        this.nombre   = nombre;
        this.precio   = precio;
        this.rating   = rating;
        this.ubicacion = ubicacion;
        this.tipo = tipo;
        this.imageUrl  = imageUrl;

    }

    public String getNombre() {
        return nombre;
    }

    public String getPrecio() {
        return precio;
    }

    public String getRating() {
        return rating;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public String getTipo() {
        return tipo;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
