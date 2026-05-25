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

    // ── Superadmin: sesión ──────────────────────────────────────────────────

    private static final String KEY_SA_NOMBRE          = "sa_nombre";
    private static final String KEY_SA_ULTIMO_ACCESO   = "sa_ultimo_acceso";
    private static final String KEY_SA_TAB_USUARIOS    = "sa_tab_usuarios";
    private static final String KEY_SA_FILTRO_ESTADO   = "sa_filtro_estado";
    private static final String KEY_SA_FILTRO_FECHA    = "sa_filtro_fecha";
    private static final String KEY_SA_FILTRO_UBICACION = "sa_filtro_ubicacion";
    private static final String KEY_SA_FILTRO_EMPRESA  = "sa_filtro_empresa";

    public static void guardarNombreSuperadmin(Context context, String nombre) {
        getPrefs(context).edit().putString(KEY_SA_NOMBRE, nombre).apply();
    }

    public static String obtenerNombreSuperadmin(Context context) {
        return getPrefs(context).getString(KEY_SA_NOMBRE, "Superadmin");
    }

    public static void guardarUltimoAccesoSuperadmin(Context context, String fechaHora) {
        getPrefs(context).edit().putString(KEY_SA_ULTIMO_ACCESO, fechaHora).apply();
    }

    public static String obtenerUltimoAccesoSuperadmin(Context context) {
        return getPrefs(context).getString(KEY_SA_ULTIMO_ACCESO, "");
    }

    // ── Superadmin: preferencias de Usuarios ───────────────────────────────

    public static void guardarTabUsuarios(Context context, String tab) {
        getPrefs(context).edit().putString(KEY_SA_TAB_USUARIOS, tab).apply();
    }

    public static String obtenerTabUsuarios(Context context) {
        return getPrefs(context).getString(KEY_SA_TAB_USUARIOS, "CLIENTS");
    }

    public static void guardarFiltroEstado(Context context, String filtro) {
        getPrefs(context).edit().putString(KEY_SA_FILTRO_ESTADO, filtro).apply();
    }

    public static String obtenerFiltroEstado(Context context) {
        return getPrefs(context).getString(KEY_SA_FILTRO_ESTADO, "ALL");
    }

    // ── Superadmin: filtros de Aprobaciones ────────────────────────────────

    public static void guardarFiltroFecha(Context context, int valor) {
        getPrefs(context).edit().putInt(KEY_SA_FILTRO_FECHA, valor).apply();
    }

    public static int obtenerFiltroFecha(Context context) {
        return getPrefs(context).getInt(KEY_SA_FILTRO_FECHA, 0);
    }

    public static void guardarFiltroUbicacion(Context context, String ubicacion) {
        getPrefs(context).edit().putString(KEY_SA_FILTRO_UBICACION, ubicacion).apply();
    }

    public static String obtenerFiltroUbicacion(Context context) {
        return getPrefs(context).getString(KEY_SA_FILTRO_UBICACION, "");
    }

    public static void guardarFiltroEmpresa(Context context, String empresa) {
        getPrefs(context).edit().putString(KEY_SA_FILTRO_EMPRESA, empresa).apply();
    }

    public static String obtenerFiltroEmpresa(Context context) {
        return getPrefs(context).getString(KEY_SA_FILTRO_EMPRESA, "");
    }

    // ── Superadmin: notificaciones descartadas ─────────────────────────────

    private static final String KEY_SA_NOTIF_DESCARTADAS = "sa_notif_descartadas";

    // ── Superadmin: notificaciones leídas ──────────────────────────────────

    private static final String KEY_SA_NOTIF_LEIDAS = "sa_notif_leidas";

    public static void marcarNotificacionLeidaSA(Context context, String notifId) {
        java.util.Set<String> leidas = new java.util.HashSet<>(obtenerNotificacionesLeidasSA(context));
        leidas.add(notifId);
        getPrefs(context).edit().putStringSet(KEY_SA_NOTIF_LEIDAS, leidas).apply();
    }

    public static java.util.Set<String> obtenerNotificacionesLeidasSA(Context context) {
        java.util.Set<String> guardadas = getPrefs(context).getStringSet(KEY_SA_NOTIF_LEIDAS, null);
        return guardadas != null ? new java.util.HashSet<>(guardadas) : new java.util.HashSet<>();
    }

    /** Mueve todas las leídas a descartadas y las borra de la pantalla. */
    public static void eliminarTodasLeidasSA(Context context) {
        java.util.Set<String> leidas = obtenerNotificacionesLeidasSA(context);
        java.util.Set<String> descartadas = new java.util.HashSet<>(obtenerNotificacionesDescartadasSA(context));
        descartadas.addAll(leidas);
        getPrefs(context).edit()
                .putStringSet(KEY_SA_NOTIF_DESCARTADAS, descartadas)
                .remove(KEY_SA_NOTIF_LEIDAS)
                .apply();
    }

    public static void descartarNotificacionSA(Context context, String notifId) {
        java.util.Set<String> descartadas = new java.util.HashSet<>(obtenerNotificacionesDescartadasSA(context));
        descartadas.add(notifId);
        getPrefs(context).edit().putStringSet(KEY_SA_NOTIF_DESCARTADAS, descartadas).apply();
    }

    public static java.util.Set<String> obtenerNotificacionesDescartadasSA(Context context) {
        java.util.Set<String> guardadas = getPrefs(context).getStringSet(KEY_SA_NOTIF_DESCARTADAS, null);
        return guardadas != null ? new java.util.HashSet<>(guardadas) : new java.util.HashSet<>();
    }
}
