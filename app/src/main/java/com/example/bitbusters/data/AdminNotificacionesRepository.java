package com.example.bitbusters.data;

import com.example.bitbusters.models.AdminNotificacion;

import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio estático en memoria para las notificaciones del Administrador (Parte 5 — Lab 5).
 * Persiste durante la sesión de la app (en memoria de proceso).
 *
 * Cada vez que NotificationHelper.lanzarNotificacionAdmin() lanza una notificación,
 * también llama a {@link #agregar(AdminNotificacion)} para registrarla aquí.
 * AdminNotificacionesActivity lee este repositorio para mostrar el historial.
 */
public final class AdminNotificacionesRepository {

    // Lista estática en memoria: las más recientes quedan al inicio (índice 0)
    private static final List<AdminNotificacion> lista = new ArrayList<>();

    // Constructor privado — no instanciar
    private AdminNotificacionesRepository() {}

    /**
     * Agrega una notificación al INICIO de la lista (más reciente primero).
     *
     * @param notificacion La notificación a registrar.
     */
    public static void agregar(AdminNotificacion notificacion) {
        if (notificacion != null) {
            lista.add(0, notificacion);
        }
    }

    /**
     * Retorna una copia de la lista de notificaciones (más reciente primero).
     * La copia previene modificaciones externas accidentales.
     *
     * @return Lista de AdminNotificacion ordenada por más reciente primero.
     */
    public static List<AdminNotificacion> getLista() {
        return new ArrayList<>(lista);
    }

    /**
     * Indica si el repositorio está vacío (no hay notificaciones registradas).
     *
     * @return true si no se ha lanzado ninguna notificación en esta sesión.
     */
    public static boolean estaVacia() {
        return lista.isEmpty();
    }

    /**
     * Retorna la cantidad de notificaciones registradas.
     *
     * @return Número de notificaciones en memoria.
     */
    public static int getCantidad() {
        return lista.size();
    }
}
