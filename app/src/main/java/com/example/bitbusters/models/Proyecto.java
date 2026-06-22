package com.example.bitbusters.models;

import java.util.List;
import java.util.Map;

public class Proyecto {

    // ── Campos legacy (public — usados directamente por ProyectoAdapter y SearchResultAdapter) ──
    public String nombre;
    public String precio;      // mapeado desde "precio" o fallback de "precioPublicado"
    public String rating;
    public String ubicacion;   // mapeado desde "ubicacion" o fallback de "distrito"
    public String tipo;
    public String imageUrl;    // mapeado desde "imageUrl" o fallback de imagenesUri[0]

    // ── Campos adicionales de Firestore ──
    private String id;
    private String nombreComercial;
    private String descripcion;
    private String direccion;
    private String distrito;
    private String estado;
    private String precioPublicado;
    private List<String> imagenesUri;
    private List<String> asesores;
    private List<String> uidAsesores;
    private List<Map<String, Object>> tipologias;
    private String costoSeparacion;
    private String fechaCreacion;
    private String fechaEntrega;
    private Boolean esDemo;
    private Double latitud;
    private Double longitud;

    /** Constructor vacío requerido por Firestore toObject(). */
    public Proyecto() {}

    /** Constructor de compatibilidad — usado por ProjectSessionData y código legacy. */
    public Proyecto(String nombre, String precio, String rating,
                    String ubicacion, String tipo, String imageUrl) {
        this.nombre    = nombre;
        this.precio    = precio;
        this.rating    = rating;
        this.ubicacion = ubicacion;
        this.tipo      = tipo;
        this.imageUrl  = imageUrl;
    }

    // ── Getters campos legacy ──
    public String getNombre()    { return nombre; }
    public String getPrecio()    { return precio; }
    public String getRating()    { return rating; }
    public String getUbicacion() { return ubicacion; }
    public String getTipo()      { return tipo; }
    public String getImageUrl()  { return imageUrl; }

    // ── Setters campos legacy (necesarios para que Firestore pueda poblarlos) ──
    public void setNombre(String nombre)       { this.nombre = nombre; }
    public void setPrecio(String precio)       { this.precio = precio; }
    public void setRating(String rating)       { this.rating = rating; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public void setTipo(String tipo)           { this.tipo = tipo; }
    public void setImageUrl(String imageUrl)   { this.imageUrl = imageUrl; }

    // ── Getters/Setters campos adicionales ──
    public String getId()                                           { return id; }
    public void   setId(String id)                                  { this.id = id; }

    public String getNombreComercial()                              { return nombreComercial; }
    public void   setNombreComercial(String v)                      { this.nombreComercial = v; }

    public String getDescripcion()                                  { return descripcion; }
    public void   setDescripcion(String v)                          { this.descripcion = v; }

    public String getDireccion()                                    { return direccion; }
    public void   setDireccion(String v)                            { this.direccion = v; }

    public String getDistrito()                                     { return distrito; }
    public void   setDistrito(String v)                             { this.distrito = v; }

    public String getEstado()                                       { return estado; }
    public void   setEstado(String v)                               { this.estado = v; }

    public String getPrecioPublicado()                              { return precioPublicado; }
    public void   setPrecioPublicado(String v)                      { this.precioPublicado = v; }

    public List<String> getImagenesUri()                            { return imagenesUri; }
    public void         setImagenesUri(List<String> v)              { this.imagenesUri = v; }

    public List<String> getAsesores()                               { return asesores; }
    public void         setAsesores(List<String> v)                 { this.asesores = v; }

    public List<String> getUidAsesores()                            { return uidAsesores; }
    public void         setUidAsesores(List<String> v)              { this.uidAsesores = v; }

    public List<Map<String, Object>> getTipologias()                { return tipologias; }
    public void setTipologias(List<Map<String, Object>> v)          { this.tipologias = v; }

    public String getCostoSeparacion()                              { return costoSeparacion; }
    public void   setCostoSeparacion(String v)                      { this.costoSeparacion = v; }

    public String getFechaCreacion()                                { return fechaCreacion; }
    public void   setFechaCreacion(String v)                        { this.fechaCreacion = v; }

    public String getFechaEntrega()                                 { return fechaEntrega; }
    public void   setFechaEntrega(String v)                         { this.fechaEntrega = v; }

    public Boolean getEsDemo()                                      { return esDemo; }
    public void    setEsDemo(Boolean v)                             { this.esDemo = v; }

    public Double getLatitud()                                      { return latitud; }
    public void   setLatitud(Double v)                              { this.latitud = v; }

    public Double getLongitud()                                     { return longitud; }
    public void   setLongitud(Double v)                             { this.longitud = v; }
}
