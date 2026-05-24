package com.example.bitbusters.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gestor centralizado de preferencias locales (Storage Local — Lab 5).
 * Usa getSharedPreferences("bitbusters_prefs", MODE_PRIVATE).
 * Todos los métodos son estáticos para evitar repetir código en cada Activity.
 */
public class PreferencesManager {

    // Nombre del archivo de preferencias
    private static final String NOMBRE_PREFS = "bitbusters_prefs";

    // Claves de las preferencias guardadas
    private static final String KEY_NOMBRE             = "nombre";
    private static final String KEY_ULTIMO_ACCESO      = "ultimo_acceso";
    private static final String KEY_TIPOLOGIA_FAVORITA = "tipologia_favorita";
    private static final String KEY_CITAS_CANCELADAS   = "citas_canceladas";

    // Devuelve la instancia de SharedPreferences (modo privado)
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(NOMBRE_PREFS, Context.MODE_PRIVATE);
    }

    // ── Nombre del cliente ──────────────────────────────────────────────────

    public static void guardarNombre(Context context, String nombre) {
        getPrefs(context).edit().putString(KEY_NOMBRE, nombre).apply();
    }

    /** Devuelve el nombre guardado; "Jonathan" si nunca se guardó. */
    public static String obtenerNombre(Context context) {
        return getPrefs(context).getString(KEY_NOMBRE, "Jonathan");
    }

    // ── Último acceso ───────────────────────────────────────────────────────

    public static void guardarUltimoAcceso(Context context, String fechaHora) {
        getPrefs(context).edit().putString(KEY_ULTIMO_ACCESO, fechaHora).apply();
    }

    /** Devuelve la fecha/hora del último login; "" si nunca se registró. */
    public static String obtenerUltimoAcceso(Context context) {
        return getPrefs(context).getString(KEY_ULTIMO_ACCESO, "");
    }

    // ── Tipología favorita (último filtro seleccionado) ─────────────────────

    public static void guardarTipologiaFavorita(Context context, String tipologia) {
        getPrefs(context).edit().putString(KEY_TIPOLOGIA_FAVORITA, tipologia).apply();
    }

    /** Devuelve la tipología guardada; "Todos" si nunca se seleccionó. */
    public static String obtenerTipologiaFavorita(Context context) {
        return getPrefs(context).getString(KEY_TIPOLOGIA_FAVORITA, "Todos");
    }

    // ── Citas canceladas por el cliente ────────────────────────────────────

    /** Agrega el ID de una cita al conjunto de canceladas para persistirla. */
    public static void guardarCitaCancelada(Context context, String citaId) {
        java.util.Set<String> canceladas = new java.util.HashSet<>(obtenerCitasCanceladas(context));
        canceladas.add(citaId);
        getPrefs(context).edit().putStringSet(KEY_CITAS_CANCELADAS, canceladas).apply();
    }

    /** Devuelve el conjunto de IDs de citas canceladas; vacío si ninguna. */
    public static java.util.Set<String> obtenerCitasCanceladas(Context context) {
        java.util.Set<String> guardadas = getPrefs(context).getStringSet(KEY_CITAS_CANCELADAS, null);
        return guardadas != null ? new java.util.HashSet<>(guardadas) : new java.util.HashSet<>();
    }
}
