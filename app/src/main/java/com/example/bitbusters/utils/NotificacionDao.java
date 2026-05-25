package com.example.bitbusters.utils;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.bitbusters.models.NotificacionEntity;

import java.util.List;

@Dao
public interface NotificacionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NotificacionEntity notif);

    /** Retorna las últimas 50 notificaciones, más reciente primero. */
    @Query("SELECT * FROM notificaciones ORDER BY id DESC LIMIT 50")
    List<NotificacionEntity> getAll();

    /** Cantidad total de notificaciones almacenadas. */
    @Query("SELECT COUNT(*) FROM notificaciones")
    int count();

    /** Elimina todas las notificaciones (logout / clearAll). */
    @Query("DELETE FROM notificaciones")
    void deleteAll();

    /** Recorta la tabla para mantener sólo los 50 registros más recientes. */
    @Query("DELETE FROM notificaciones WHERE id NOT IN " +
           "(SELECT id FROM notificaciones ORDER BY id DESC LIMIT 50)")
    void trimToLimit();
}
