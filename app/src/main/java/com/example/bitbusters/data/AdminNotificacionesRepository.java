package com.example.bitbusters.data;

import android.content.Context;

import com.example.bitbusters.models.AdminNotificacion;
import com.example.bitbusters.utils.AdminPreferencesManager;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

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
    private static final String COLECCION_NOTIFICACIONES = "notifications";

    public interface NotificacionesListener {
        void onNotificacionesActualizadas(List<AdminNotificacion> notificaciones);
        void onError(String mensaje);
    }

    public interface ActualizarCallback {
        void onSuccess();
        void onError(String mensaje);
    }

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

    /**
     * Escucha notificaciones persistidas de Firestore para el administrador actual.
     *
     * Mantiene compatibilidad con la lista local:
     * - si Firestore falla, entrega las notificaciones locales;
     * - si Firestore no tiene documentos aplicables, entrega las locales;
     * - si Firestore tiene datos, estos son la fuente principal.
     */
    public static ListenerRegistration escucharDesdeFirestore(
            Context context,
            NotificacionesListener listener
    ) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String adminUid = user != null ? user.getUid() : "";
        String inmobiliariaId = context != null
                ? AdminPreferencesManager.obtenerInmobiliariaId(context)
                : "";

        return FirebaseFirestore.getInstance()
                .collection(COLECCION_NOTIFICACIONES)
                .whereEqualTo("role", "admin")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        if (listener != null) {
                            listener.onError(error.getMessage() != null
                                    ? error.getMessage()
                                    : "No se pudieron escuchar notificaciones");
                            listener.onNotificacionesActualizadas(getLista());
                        }
                        return;
                    }

                    List<DocumentSnapshot> documentos = new ArrayList<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            if (perteneceAlAdmin(doc, adminUid, inmobiliariaId)) {
                                documentos.add(doc);
                            }
                        }
                    }

                    Collections.sort(documentos, (a, b) ->
                            Long.compare(obtenerOrden(b), obtenerOrden(a)));

                    List<AdminNotificacion> remotas = new ArrayList<>();
                    for (DocumentSnapshot doc : documentos) {
                        remotas.add(toAdminNotificacion(doc));
                    }

                    if (remotas.isEmpty()) {
                        remotas = getLista();
                    }

                    if (listener != null) {
                        listener.onNotificacionesActualizadas(remotas);
                    }
                });
    }

    private static AdminNotificacion toAdminNotificacion(DocumentSnapshot doc) {
        String titulo = firstNonEmpty(
                doc.getString("title"),
                doc.getString("titulo"),
                doc.getString("senderName"),
                "Notificación"
        );
        String mensaje = firstNonEmpty(
                doc.getString("descripcion"),
                doc.getString("mensaje"),
                doc.getString("message"),
                ""
        );
        String timestamp = firstNonEmpty(
                doc.getString("tiempo"),
                doc.getString("time"),
                formatearTimestamp(doc.getTimestamp("createdAt")),
                formatearTimestamp(doc.getTimestamp("timestamp"))
        );
        return new AdminNotificacion(doc.getId(), titulo, mensaje, timestamp, true);
    }

    public static void marcarComoLeida(AdminNotificacion notificacion, ActualizarCallback callback) {
        if (notificacion == null || !notificacion.isRemota()) {
            if (callback != null) callback.onSuccess();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection(COLECCION_NOTIFICACIONES)
                .document(notificacion.getId())
                .update("read", true)
                .addOnSuccessListener(unused -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(e.getMessage() != null
                                ? e.getMessage()
                                : "No se pudo marcar la notificación como leída");
                    }
                });
    }

    private static boolean perteneceAlAdmin(
            DocumentSnapshot doc,
            String adminUid,
            String inmobiliariaId
    ) {
        String targetUid = firstNonEmpty(
                doc.getString("targetUid"),
                doc.getString("adminUid"),
                doc.getString("uidAdmin")
        );
        if (!targetUid.isEmpty() && !adminUid.isEmpty()) {
            return targetUid.equals(adminUid);
        }

        String docInmobiliariaId = firstNonEmpty(
                doc.getString("inmobiliariaId"),
                doc.getString("empresaId")
        );
        if (!docInmobiliariaId.isEmpty() && !inmobiliariaId.isEmpty()) {
            return docInmobiliariaId.equalsIgnoreCase(inmobiliariaId);
        }

        // Compatibilidad con notificaciones admin legacy sin destinatario explícito.
        return targetUid.isEmpty() && docInmobiliariaId.isEmpty();
    }

    private static long obtenerOrden(DocumentSnapshot doc) {
        Long order = doc.getLong("order");
        if (order != null) return order;

        Timestamp createdAt = doc.getTimestamp("createdAt");
        if (createdAt != null) return createdAt.toDate().getTime();

        Timestamp timestamp = doc.getTimestamp("timestamp");
        if (timestamp != null) return timestamp.toDate().getTime();

        return 0L;
    }

    private static String formatearTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "";
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(timestamp.toDate());
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) return "";
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }
}
