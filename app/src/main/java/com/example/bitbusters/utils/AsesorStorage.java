package com.example.bitbusters.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.bitbusters.models.AsesorCita;
import com.example.bitbusters.models.AsesorNotif;
import com.example.bitbusters.models.CitaEstadoEntity;
import com.example.bitbusters.models.DeletedChatEntity;
import com.example.bitbusters.models.NotificacionEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Capa de acceso a datos del módulo asesor.
 *
 * Las listas estructuradas (notificaciones, estados de citas, chats eliminados)
 * se persisten en Room Database.  Los valores escalares de UI (tab, filtro)
 * siguen en SharedPreferences por su bajo coste y acceso inmediato.
 */
public class AsesorStorage {

    private static final String PREFS_NAME          = "asesor_prefs";
    private static final String KEY_CITA_TAB        = "cita_tab";
    private static final String KEY_HOME_FILTER     = "home_filter";
    private static final String KEY_NOTIF_BASELINE  = "notif_read_baseline";
    private static final String KEY_ASESOR_ID       = "asesor_id";

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static AsesorDatabase db(Context ctx) {
        return AsesorDatabase.getInstance(ctx);
    }

    // ── Notificaciones ────────────────────────────────────────────────────────

    public static void addNotificacion(Context ctx, AsesorNotif notif) {
        db(ctx).notificacionDao().insert(NotificacionEntity.from(notif));
        db(ctx).notificacionDao().trimToLimit();   // mantiene máximo 50
    }

    public static void saveNotificaciones(Context ctx, List<AsesorNotif> list) {
        NotificacionDao dao = db(ctx).notificacionDao();
        dao.deleteAll();
        for (AsesorNotif n : list) dao.insert(NotificacionEntity.from(n));
    }

    public static List<AsesorNotif> getNotificaciones(Context ctx) {
        List<NotificacionEntity> entities = db(ctx).notificacionDao().getAll();
        List<AsesorNotif> result = new ArrayList<>(entities.size());
        for (NotificacionEntity e : entities) result.add(e.toAsesorNotif());
        return result;
    }

    /**
     * Badge = total en DB − baseline (registrado la última vez que el
     * usuario abrió la pantalla de notificaciones).
     */
    public static int getNotifCount(Context ctx) {
        int total    = db(ctx).notificacionDao().count();
        int baseline = prefs(ctx).getInt(KEY_NOTIF_BASELINE, 0);
        return Math.max(0, total - baseline);
    }

    /** Llámalo al abrir AsesorNotificacionesActivity para limpiar el badge. */
    public static void resetNotifCount(Context ctx) {
        int total = db(ctx).notificacionDao().count();
        prefs(ctx).edit().putInt(KEY_NOTIF_BASELINE, total).apply();
    }

    // ── UI state ──────────────────────────────────────────────────────────────

    public static void saveCitaTab(Context ctx, int tab) {
        prefs(ctx).edit().putInt(KEY_CITA_TAB, tab).apply();
    }

    public static int getCitaTab(Context ctx) {
        return prefs(ctx).getInt(KEY_CITA_TAB, 0);
    }

    public static void saveHomeFilter(Context ctx, String filter) {
        prefs(ctx).edit().putString(KEY_HOME_FILTER, filter).apply();
    }

    public static String getHomeFilter(Context ctx) {
        return prefs(ctx).getString(KEY_HOME_FILTER, "Todos");
    }

    // ── Estado de citas ───────────────────────────────────────────────────────

    public static void confirmPendienteCita(Context ctx,
                                            String nombre, String proyecto,
                                            String fecha, String hora,
                                            String initials, int color) {
        String key = buildCitaKey(nombre, fecha, hora);
        db(ctx).citaEstadoDao().insert(
            new CitaEstadoEntity(key, "confirmada",
                                 nombre, proyecto, fecha, hora, initials, color));
    }

    public static void cancelCita(Context ctx,
                                  String nombre, String proyecto,
                                  String fecha, String hora,
                                  String initials, int color) {
        String key = buildCitaKey(nombre, fecha, hora);
        // REPLACE: si ya existía "confirmada", la sobreescribe como "cancelada"
        db(ctx).citaEstadoDao().insert(
            new CitaEstadoEntity(key, "cancelada",
                                 nombre, proyecto, fecha, hora, initials, color));
    }

    public static Set<String> getConfirmedPendKeys(Context ctx) {
        return new HashSet<>(db(ctx).citaEstadoDao().getKeysByEstado("confirmada"));
    }

    public static Set<String> getCancelledKeys(Context ctx) {
        return new HashSet<>(db(ctx).citaEstadoDao().getKeysByEstado("cancelada"));
    }

    public static List<AsesorCita> getConfirmedCitas(Context ctx) {
        List<CitaEstadoEntity> entities =
                db(ctx).citaEstadoDao().getByEstado("confirmada");
        List<AsesorCita> result = new ArrayList<>(entities.size());
        for (CitaEstadoEntity e : entities) result.add(e.toAsesorCita());
        return result;
    }

    public static List<AsesorCita> getCancelledCitas(Context ctx) {
        List<CitaEstadoEntity> entities =
                db(ctx).citaEstadoDao().getByEstado("cancelada");
        List<AsesorCita> result = new ArrayList<>(entities.size());
        for (CitaEstadoEntity e : entities) result.add(e.toAsesorCita());
        return result;
    }

    public static String buildCitaKey(String nombre, String fecha, String hora) {
        return nombre + "||" + fecha + "||" + hora;
    }

    // ── Chats eliminados ──────────────────────────────────────────────────────

    public static void saveDeletedChatId(Context ctx, String chatId) {
        db(ctx).deletedChatDao().insert(new DeletedChatEntity(chatId));
    }

    public static Set<String> getDeletedChatIds(Context ctx) {
        return new HashSet<>(db(ctx).deletedChatDao().getAllIds());
    }

    // ── Identidad del asesor ──────────────────────────────────────────────────

    /** Guarda el ID de documento Firestore del asesor (ej. "asesor_ana_001"). */
    public static void saveAsesorId(Context ctx, String asesorId) {
        prefs(ctx).edit().putString(KEY_ASESOR_ID, asesorId).apply();
    }

    /** Devuelve el ID del asesor o null si no se ha establecido. */
    public static String getAsesorId(Context ctx) {
        return prefs(ctx).getString(KEY_ASESOR_ID, null);
    }

    // ── Limpieza de sesión ────────────────────────────────────────────────────

    public static void clearAll(Context ctx) {
        prefs(ctx).edit().clear().apply();
        db(ctx).notificacionDao().deleteAll();
        db(ctx).citaEstadoDao().deleteAll();
        db(ctx).deletedChatDao().deleteAll();
    }
}
