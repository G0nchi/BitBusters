package com.example.bitbusters.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gestor centralizado de preferencias locales para el Administrador (Lab 5).
 * Usa getSharedPreferences("bitbusters_admin_prefs", MODE_PRIVATE).
 * Separado de PreferencesManager del cliente para evitar colisiones de claves.
 * Todos los métodos son estáticos para facilitar el acceso desde cualquier Activity.
 */
public class AdminPreferencesManager {

    // Nombre del archivo de preferencias del admin (separado del cliente)
    private static final String NOMBRE_PREFS = "bitbusters_admin_prefs";

    // Claves de las preferencias guardadas del administrador
    private static final String KEY_ADMIN_NOMBRE                  = "admin_nombre";
    private static final String KEY_ADMIN_INMOBILIARIA             = "admin_inmobiliaria";
    private static final String KEY_ADMIN_ULTIMO_ACCESO            = "admin_ultimo_acceso";
    private static final String KEY_ADMIN_PROYECTOS_COUNT          = "admin_proyectos_count";
    private static final String KEY_ADMIN_SEPARACIONES_PENDIENTES  = "admin_separaciones_pendientes";

    // ── Acceso a las SharedPreferences ─────────────────────────────────────

    /** Retorna la instancia de SharedPreferences del admin en modo privado. */
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(NOMBRE_PREFS, Context.MODE_PRIVATE);
    }

    // ── Nombre del administrador ────────────────────────────────────────────

    /** Guarda el nombre completo del administrador al hacer login. */
    public static void guardarNombre(Context context, String nombre) {
        getPrefs(context).edit().putString(KEY_ADMIN_NOMBRE, nombre).apply();
    }

    /** Retorna el nombre del admin guardado; "Administrador" si nunca se registró. */
    public static String obtenerNombre(Context context) {
        return getPrefs(context).getString(KEY_ADMIN_NOMBRE, "Administrador");
    }

    // ── Nombre de la inmobiliaria ───────────────────────────────────────────

    /** Guarda el nombre de la inmobiliaria que administra. */
    public static void guardarInmobiliaria(Context context, String inmobiliaria) {
        getPrefs(context).edit().putString(KEY_ADMIN_INMOBILIARIA, inmobiliaria).apply();
    }

    /** Retorna el nombre de la inmobiliaria; "Inmobiliaria BitBuilders" si no se guardó. */
    public static String obtenerInmobiliaria(Context context) {
        return getPrefs(context).getString(KEY_ADMIN_INMOBILIARIA, "Inmobiliaria BitBuilders");
    }

    // ── Último acceso del administrador ─────────────────────────────────────

    /** Guarda la fecha y hora del último login del admin (formato "dd/MM/yyyy HH:mm"). */
    public static void guardarUltimoAcceso(Context context, String fechaHora) {
        getPrefs(context).edit().putString(KEY_ADMIN_ULTIMO_ACCESO, fechaHora).apply();
    }

    /** Retorna la fecha/hora del último acceso; "" si nunca se registró. */
    public static String obtenerUltimoAcceso(Context context) {
        return getPrefs(context).getString(KEY_ADMIN_ULTIMO_ACCESO, "");
    }

    // ── Contador de proyectos registrados ───────────────────────────────────

    /**
     * Incrementa en 1 el contador de proyectos registrados.
     * Llamar cada vez que el admin guarda un proyecto en AdminCrearProyectoActivity.
     */
    public static void incrementarProyectosCount(Context context) {
        int actual = obtenerProyectosCount(context);
        getPrefs(context).edit().putInt(KEY_ADMIN_PROYECTOS_COUNT, actual + 1).apply();
    }

    /** Retorna el total de proyectos registrados por el admin; 0 si no hay ninguno. */
    public static int obtenerProyectosCount(Context context) {
        return getPrefs(context).getInt(KEY_ADMIN_PROYECTOS_COUNT, 0);
    }

    // ── Contador de separaciones pendientes ─────────────────────────────────

    /** Guarda directamente la cantidad de separaciones pendientes. */
    public static void guardarSeparacionesPendientes(Context context, int cantidad) {
        getPrefs(context).edit().putInt(KEY_ADMIN_SEPARACIONES_PENDIENTES, cantidad).apply();
    }

    /**
     * Incrementa en 1 el contador de separaciones pendientes.
     * Llamar cuando llega una nueva separación del asesor.
     */
    public static void incrementarSeparacionesPendientes(Context context) {
        int actual = obtenerSeparacionesPendientes(context);
        getPrefs(context).edit().putInt(KEY_ADMIN_SEPARACIONES_PENDIENTES, actual + 1).apply();
    }

    /**
     * Decrementa en 1 el contador de separaciones pendientes (mínimo 0).
     * Llamar cuando el admin aprueba o rechaza una separación.
     */
    public static void decrementarSeparacionesPendientes(Context context) {
        int actual = obtenerSeparacionesPendientes(context);
        if (actual > 0) {
            getPrefs(context).edit().putInt(KEY_ADMIN_SEPARACIONES_PENDIENTES, actual - 1).apply();
        }
    }

    /** Retorna el total de separaciones pendientes de aprobación; 0 si no hay ninguna. */
    public static int obtenerSeparacionesPendientes(Context context) {
        return getPrefs(context).getInt(KEY_ADMIN_SEPARACIONES_PENDIENTES, 0);
    }
}
