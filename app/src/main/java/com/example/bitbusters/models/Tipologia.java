package com.example.bitbusters.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una tipología de departamento dentro de un proyecto inmobiliario.
 * Ejemplo: "Tipo A — 2 dorm., 65 m², S/ 280,000"
 */
public class Tipologia {

    private String nombre;       // "Tipo A"
    private int    dormitorios;  // 2
    private int    banos;        // 1
    private double metraje;      // 65.5
    private double precioTotal;  // 280000.0
    private String descripcion;  // opcional
    private String imageUri;     // URI de imagen si se subió, vacío si no
    private List<String> imagenesUri; // URIs de imágenes de la tipología

    /** Constructor vacío (requerido para Gson/serialización) */
    public Tipologia() {
        this.imagenesUri = new ArrayList<>();
    }

    /**
     * Constructor completo.
     *
     * @param nombre       Nombre/código de la tipología.
     * @param dormitorios  Número de dormitorios (1, 2 o 3).
     * @param banos        Número de baños (1, 2 o 3).
     * @param metraje      Área en m².
     * @param precioTotal  Precio total en soles.
     * @param descripcion  Descripción breve (puede ser vacío).
     * @param imageUri     URI de la imagen principal de la tipología (puede ser vacío).
     */
    public Tipologia(String nombre, int dormitorios, int banos,
                     double metraje, double precioTotal,
                     String descripcion, String imageUri) {
        this.nombre      = nombre;
        this.dormitorios = dormitorios;
        this.banos       = banos;
        this.metraje     = metraje;
        this.precioTotal = precioTotal;
        this.descripcion = descripcion;
        this.imageUri    = imageUri;
        this.imagenesUri = new ArrayList<>();
        if (imageUri != null && !imageUri.trim().isEmpty()) {
            this.imagenesUri.add(imageUri);
        }
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getNombre()      { return nombre      != null ? nombre      : ""; }
    public int    getDormitorios() { return dormitorios; }
    public int    getBanos()       { return banos; }
    public double getMetraje()     { return metraje; }
    public double getPrecioTotal() { return precioTotal; }
    public String getDescripcion() { return descripcion != null ? descripcion : ""; }
    public String getImageUri() {
        if (imageUri != null && !imageUri.trim().isEmpty()) return imageUri;
        if (imagenesUri != null && !imagenesUri.isEmpty() && imagenesUri.get(0) != null) {
            return imagenesUri.get(0);
        }
        return "";
    }
    public List<String> getImagenesUri() {
        if (imagenesUri == null) imagenesUri = new ArrayList<>();
        return imagenesUri;
    }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setNombre(String nombre)           { this.nombre      = nombre; }
    public void setDormitorios(int dormitorios)    { this.dormitorios = dormitorios; }
    public void setBanos(int banos)                { this.banos       = banos; }
    public void setMetraje(double metraje)         { this.metraje     = metraje; }
    public void setPrecioTotal(double precioTotal) { this.precioTotal = precioTotal; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setImageUri(String imageUri)       { this.imageUri    = imageUri; }
    public void setImagenesUri(List<String> imagenesUri) {
        this.imagenesUri = imagenesUri != null ? imagenesUri : new ArrayList<>();
        if (!this.imagenesUri.isEmpty()) {
            this.imageUri = this.imagenesUri.get(0);
        }
    }
}
