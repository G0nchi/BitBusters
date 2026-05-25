package com.example.bitbusters.models;

/** Modelo serializable para persistir el estado de citas del asesor. */
public class AsesorCita {
    public String nombre;
    public String proyecto;
    public String fecha;
    public String hora;
    public String initials;
    public int    avatarColor;

    public AsesorCita() {}

    public AsesorCita(String nombre, String proyecto, String fecha, String hora,
                      String initials, int avatarColor) {
        this.nombre      = nombre;
        this.proyecto    = proyecto;
        this.fecha       = fecha;
        this.hora        = hora;
        this.initials    = initials;
        this.avatarColor = avatarColor;
    }

    public String key() {
        return nombre + "||" + fecha + "||" + hora;
    }
}
