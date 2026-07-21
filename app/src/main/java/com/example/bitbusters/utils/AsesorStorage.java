package com.example.bitbusters.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.bitbusters.models.AsesorNotif;
import com.example.bitbusters.models.DeletedChatEntity;
import com.example.bitbusters.models.NotificacionEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Capa de acceso a datos local del módulo asesor.
 *
 * Las listas estructuradas (notificaciones, chats eliminados) se persisten en
 * Room Database. Los valores escalares de UI (tab, filtro) siguen en
 * SharedPreferences por su bajo coste y acceso inmediato. El estado de las
 * citas ya no vive aquí — se lee/escribe directamente en Firestore vía
 * {@link com.example.bitbusters.repository.CitaRepository}.
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
        db(ctx).deletedChatDao().deleteAll();
    }
}
