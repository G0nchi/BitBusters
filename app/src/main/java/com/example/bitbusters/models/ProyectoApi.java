package com.example.bitbusters.models;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo de respuesta de la API REST para proyectos inmobiliarios.
 *
 * Los campos coinciden con el JSON que devolvería el endpoint:
 *   GET /api/v1/proyectos
 *
 * Gson mapea los nombres JSON (snake_case) a los campos Java usando
 * la anotación @SerializedName.
 */
public class ProyectoApi {

    @SerializedName("id")
    public int id;

    @SerializedName("nombre")
    public String nombre;

    @SerializedName("ubicacion")
    public String ubicacion;

    @SerializedName("precio")
    public String precio;

    @SerializedName("estado")
    public String estado;       // "En Venta" | "Preventa" | "En Planos"

    @SerializedName("rating")
    public String rating;

    @SerializedName("tipo")
    public String tipo;         // "Departamento" | "Villa"

    @SerializedName("imagen_key")
    public String imagenKey;    // Clave para mapear a drawable local

    public ProyectoApi() {}

    public ProyectoApi(int id, String nombre, String ubicacion, String precio,
                       String estado, String rating, String tipo, String imagenKey) {
        this.id        = id;
        this.nombre    = nombre;
        this.ubicacion = ubicacion;
        this.precio    = precio;
        this.estado    = estado;
        this.rating    = rating;
        this.tipo      = tipo;
        this.imagenKey = imagenKey;
    }
}
