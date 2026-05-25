package com.example.bitbusters.utils;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.bitbusters.models.CitaEstadoEntity;
import com.example.bitbusters.models.DeletedChatEntity;
import com.example.bitbusters.models.NotificacionEntity;

/**
 * Base de datos Room del módulo asesor.
 *
 * Usa allowMainThreadQueries() apropiado para proyecto de curso;
 * en producción se usaría LiveData / Coroutines / Executor.
 *
 * fallbackToDestructiveMigration() simplifica iteraciones de desarrollo:
 * al subir version se recrea el esquema en lugar de requerir Migration.
 */
@Database(
    entities = {
        NotificacionEntity.class,
        CitaEstadoEntity.class,
        DeletedChatEntity.class
    },
    version  = 1,
    exportSchema = false
)
public abstract class AsesorDatabase extends RoomDatabase {

    public abstract NotificacionDao notificacionDao();
    public abstract CitaEstadoDao   citaEstadoDao();
    public abstract DeletedChatDao  deletedChatDao();

    // ── Singleton thread-safe ─────────────────────────────────────────────────

    private static volatile AsesorDatabase INSTANCE;

    public static AsesorDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AsesorDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AsesorDatabase.class,
                                    "asesor_db"
                            )
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
