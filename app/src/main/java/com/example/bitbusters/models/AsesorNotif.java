package com.example.bitbusters.models;

public class AsesorNotif {

    public static final String TIPO_CITA       = "cita";
    public static final String TIPO_SEPARACION = "separacion";
    public static final String TIPO_MENSAJE    = "mensaje";
    public static final String TIPO_VALORACION = "valoracion";
    public static final String TIPO_ALERTA     = "alerta";

    public String titulo;
    public String descripcion;
    public String tiempo;
    public String tipo;

    public AsesorNotif() {}

    public AsesorNotif(String titulo, String descripcion, String tiempo, String tipo) {
        this.titulo      = titulo;
        this.descripcion = descripcion;
        this.tiempo      = tiempo;
        this.tipo        = tipo;
    }
}
