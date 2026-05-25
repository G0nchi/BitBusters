package com.example.bitbusters.utils;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.bitbusters.models.CitaEstadoEntity;

import java.util.List;

@Dao
public interface CitaEstadoDao {

    /**
     * REPLACE sobreescribe si ya existe la misma citaKey —
     * permite pasar una cita de "confirmada" a "cancelada".
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CitaEstadoEntity entity);

    /** Todas las citas de un estado determinado (confirmada / cancelada). */
    @Query("SELECT * FROM cita_estado WHERE estado = :estado ORDER BY rowid DESC")
    List<CitaEstadoEntity> getByEstado(String estado);

    /** Solo las claves, para filtrar rápidamente tabs. */
    @Query("SELECT citaKey FROM cita_estado WHERE estado = :estado")
    List<String> getKeysByEstado(String estado);

    /** Comprueba si existe una clave dada (cualquier estado). */
    @Query("SELECT EXISTS(SELECT 1 FROM cita_estado WHERE citaKey = :key)")
    boolean existsKey(String key);

    /** Limpia todo (logout / clearAll). */
    @Query("DELETE FROM cita_estado")
    void deleteAll();
}
