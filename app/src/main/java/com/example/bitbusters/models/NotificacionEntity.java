package com.example.bitbusters.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Entidad Room — reemplaza Gson-serialized List<AsesorNotif> en SharedPreferences. */
@Entity(tableName = "notificaciones")
public class NotificacionEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String titulo;
    public String descripcion;
    public String tiempo;
    public String tipo;

    /** Room requiere constructor vacío. */
    public NotificacionEntity() {}

    public NotificacionEntity(String titulo, String descripcion, String tiempo, String tipo) {
        this.titulo      = titulo;
        this.descripcion = descripcion;
        this.tiempo      = tiempo;
        this.tipo        = tipo;
    }

    /** Convierte a AsesorNotif para compatibilidad con código existente. */
    public AsesorNotif toAsesorNotif() {
        return new AsesorNotif(titulo, descripcion, tiempo, tipo);
    }

    /** Crea un NotificacionEntity desde un AsesorNotif. */
    public static NotificacionEntity from(AsesorNotif n) {
        return new NotificacionEntity(n.titulo, n.descripcion, n.tiempo, n.tipo);
    }
}
