package com.example.bitbusters.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "comentarios")
public class ComentarioEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String idProyecto;
    public String idUsuario;
    public String nombreUsuario;
    public String fotoUsuarioUrl;
    public int rating;
    public String texto;
    public long timestamp;

    @Ignore
    public ComentarioEntity() {}

    public ComentarioEntity(String idProyecto, String idUsuario, String nombreUsuario,
                            String fotoUsuarioUrl, int rating, String texto, long timestamp) {
        this.idProyecto = idProyecto;
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.fotoUsuarioUrl = fotoUsuarioUrl;
        this.rating = rating;
        this.texto = texto;
        this.timestamp = timestamp;
    }
}
