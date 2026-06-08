package com.example.bitbusters.utils;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.bitbusters.models.ComentarioEntity;

import java.util.List;

@Dao
public interface ComentarioDao {

    @Query("SELECT * FROM comentarios WHERE idProyecto = :idProyecto ORDER BY timestamp DESC")
    List<ComentarioEntity> obtenerPorProyecto(String idProyecto);

    @Query("SELECT AVG(rating) FROM comentarios WHERE idProyecto = :idProyecto")
    Float obtenerRatingPromedio(String idProyecto);

    @Query("SELECT COUNT(*) FROM comentarios WHERE idProyecto = :idProyecto")
    int contarPorProyecto(String idProyecto);

    @Insert
    long insertar(ComentarioEntity comentario);

    @Delete
    void eliminar(ComentarioEntity comentario);
}
