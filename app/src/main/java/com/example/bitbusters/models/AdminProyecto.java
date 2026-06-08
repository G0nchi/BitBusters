package com.example.bitbusters.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo del proyecto inmobiliario creado por el Administrador.
 * DIFERENTE a Proyecto.java (modelo del cliente/asesor) — no mezclarlos.
 */
public class AdminProyecto {

    private String id;               // UUID aleatorio
    private String nombre;
    private String descripcion;
    private String direccion;
    private String distrito;
    private String costoSeparacion;  // valor numérico como String, ej: "5000"
    private String precioTotal;      // valor numérico como String, ej: "280000"
    private String nombreComercial;
    private String precioPublicado;  // ej: "S/ 320,000"
    private String fechaEntrega;     // ej: "30/12/2026"
    private String estado;           // "En planos" | "Preventa" | "En venta"
    private List<Tipologia> tipologias;
    private List<String>    asesores;     // nombres de asesores asignados
    private List<String>    imagenesUri;  // URIs de las imágenes del proyecto
    private String          fechaCreacion;// timestamp ej: "01/01/2025"
    private String          qrCode;       // ruta local del Bitmap QR generado

    /** Constructor vacío — inicializa las listas para evitar NPE */
    public AdminProyecto() {
        this.tipologias  = new ArrayList<>();
        this.asesores    = new ArrayList<>();
        this.imagenesUri = new ArrayList<>();
        this.qrCode      = "";
    }

    /**
     * Constructor completo.
     */
    public AdminProyecto(String id, String nombre, String descripcion,
                         String direccion, String distrito,
                         String costoSeparacion, String precioTotal,
                         String nombreComercial, String precioPublicado,
                         String fechaEntrega, String estado,
                         List<Tipologia> tipologias, List<String> asesores,
                         List<String> imagenesUri, String fechaCreacion) {
        this.id              = id;
        this.nombre          = nombre;
        this.descripcion     = descripcion;
        this.direccion       = direccion;
        this.distrito        = distrito;
        this.costoSeparacion = costoSeparacion;
        this.precioTotal     = precioTotal;
        this.nombreComercial = nombreComercial;
        this.precioPublicado = precioPublicado;
        this.fechaEntrega    = fechaEntrega;
        this.estado          = estado;
        this.tipologias      = tipologias  != null ? tipologias  : new ArrayList<>();
        this.asesores        = asesores    != null ? asesores    : new ArrayList<>();
        this.imagenesUri     = imagenesUri != null ? imagenesUri : new ArrayList<>();
        this.fechaCreacion   = fechaCreacion;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getId()              { return id              != null ? id              : ""; }
    public String getNombre()          { return nombre          != null ? nombre          : ""; }
    public String getDescripcion()     { return descripcion     != null ? descripcion     : ""; }
    public String getDireccion()       { return direccion       != null ? direccion       : ""; }
    public String getDistrito()        { return distrito        != null ? distrito        : ""; }
    public String getCostoSeparacion() { return costoSeparacion != null ? costoSeparacion : ""; }
    public String getPrecioTotal()     { return precioTotal     != null ? precioTotal     : ""; }
    public String getNombreComercial() { return nombreComercial != null ? nombreComercial : ""; }
    public String getPrecioPublicado() { return precioPublicado != null ? precioPublicado : ""; }
    public String getFechaEntrega()    { return fechaEntrega    != null ? fechaEntrega    : ""; }
    public String getEstado()          { return estado          != null ? estado          : ""; }
    public String getFechaCreacion()   { return fechaCreacion   != null ? fechaCreacion   : ""; }
    public String getQrCode()          { return qrCode          != null ? qrCode          : ""; }

    public List<Tipologia> getTipologias()  { return tipologias  != null ? tipologias  : new ArrayList<>(); }
    public List<String>    getAsesores()    { return asesores    != null ? asesores    : new ArrayList<>(); }
    public List<String>    getImagenesUri() { return imagenesUri != null ? imagenesUri : new ArrayList<>(); }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setId(String id)                            { this.id              = id; }
    public void setNombre(String nombre)                    { this.nombre          = nombre; }
    public void setDescripcion(String descripcion)          { this.descripcion     = descripcion; }
    public void setDireccion(String direccion)              { this.direccion       = direccion; }
    public void setDistrito(String distrito)                { this.distrito        = distrito; }
    public void setCostoSeparacion(String costoSeparacion)  { this.costoSeparacion = costoSeparacion; }
    public void setPrecioTotal(String precioTotal)          { this.precioTotal     = precioTotal; }
    public void setNombreComercial(String nombreComercial)  { this.nombreComercial = nombreComercial; }
    public void setPrecioPublicado(String precioPublicado)  { this.precioPublicado = precioPublicado; }
    public void setFechaEntrega(String fechaEntrega)        { this.fechaEntrega    = fechaEntrega; }
    public void setEstado(String estado)                    { this.estado          = estado; }
    public void setTipologias(List<Tipologia> tipologias)   { this.tipologias      = tipologias; }
    public void setAsesores(List<String> asesores)          { this.asesores        = asesores; }
    public void setImagenesUri(List<String> imagenesUri)    { this.imagenesUri     = imagenesUri; }
    public void setFechaCreacion(String fechaCreacion)      { this.fechaCreacion   = fechaCreacion; }
    public void setQrCode(String qrCode)                    { this.qrCode          = qrCode; }
}
