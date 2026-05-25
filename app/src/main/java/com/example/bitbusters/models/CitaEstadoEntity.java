package com.example.bitbusters.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidad Room — reemplaza los conjuntos Gson de citas confirmadas/canceladas
 * en SharedPreferences. La clave primaria es citaKey = nombre||fecha||hora.
 */
@Entity(tableName = "cita_estado")
public class CitaEstadoEntity {

    @PrimaryKey
    @NonNull
    public String citaKey;      // nombre + "||" + fecha + "||" + hora

    public String estado;       // "confirmada" | "cancelada"
    public String nombre;
    public String proyecto;
    public String fecha;
    public String hora;
    public String initials;
    public int    avatarColor;

    /** Room requiere constructor vacío. */
    public CitaEstadoEntity() { citaKey = ""; }

    public CitaEstadoEntity(@NonNull String citaKey, String estado,
                            String nombre, String proyecto,
                            String fecha, String hora,
                            String initials, int avatarColor) {
        this.citaKey     = citaKey;
        this.estado      = estado;
        this.nombre      = nombre;
        this.proyecto    = proyecto;
        this.fecha       = fecha;
        this.hora        = hora;
        this.initials    = initials;
        this.avatarColor = avatarColor;
    }

    /** Convierte a AsesorCita para compatibilidad con código existente. */
    public AsesorCita toAsesorCita() {
        return new AsesorCita(nombre, proyecto, fecha, hora, initials, avatarColor);
    }
}
