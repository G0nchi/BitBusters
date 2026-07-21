package com.example.bitbusters.utils;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Escribe eventos de negocio en la coleccion Firestore "logs", consumida por
 * SuperadminLogsActivity. Esa pantalla ordena por "timestamp" (Timestamp real
 * del servidor); "id" se mantiene solo como identificador legible/de auditoria,
 * asignado atomicamente via el contador en app_meta/logs_counter.
 */
public final class LogHelper {

    public static final String TYPE_RESERVATION = "RESERVATION";
    public static final String TYPE_PAYMENT     = "PAYMENT";
    public static final String TYPE_APPOINTMENT = "APPOINTMENT";
    public static final String TYPE_ENABLEMENT  = "ENABLEMENT";

    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_SUSPENDED = "SUSPENDED";
    public static final String STATUS_FAILED    = "FAILED";

    private static final String TAG = "LogHelper";
    private static final String COLLECTION_LOGS = "logs";
    private static final String COUNTER_DOC_PATH = "app_meta/logs_counter";
    private static final TimeZone LIMA_TZ = TimeZone.getTimeZone("America/Lima");

    private LogHelper() {}

    public static void logEvent(String type, String status, String title, String source,
                                 String detailAction, String metaLabel, String metaPrimary,
                                 String metaSecondary, List<String> tags) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference counterRef = db.document(COUNTER_DOC_PATH);

        counterRef.get()
                .addOnSuccessListener(counterSnap -> {
                    if (counterSnap.exists()) {
                        writeLog(db, counterRef, type, status, title, source, detailAction,
                                metaLabel, metaPrimary, metaSecondary, tags);
                    } else {
                        // Primer log real: alinear el contador con el maximo id ya
                        // presente (datos mock sembrados por SuperadminLogsActivity)
                        // para no colisionar con esos ids.
                        db.collection(COLLECTION_LOGS)
                                .orderBy("id", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(snap -> {
                                    long maxId = 0L;
                                    if (!snap.isEmpty()) {
                                        Long v = snap.getDocuments().get(0).getLong("id");
                                        if (v != null) maxId = v;
                                    }
                                    Map<String, Object> seed = new HashMap<>();
                                    seed.put("value", maxId);
                                    counterRef.set(seed)
                                            .addOnSuccessListener(unused -> writeLog(db, counterRef,
                                                    type, status, title, source, detailAction,
                                                    metaLabel, metaPrimary, metaSecondary, tags))
                                            .addOnFailureListener(e ->
                                                    Log.e(TAG, "No se pudo inicializar el contador de logs", e));
                                })
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "No se pudo leer el maximo id de logs", e));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "No se pudo leer el contador de logs", e));
    }

    private static void writeLog(FirebaseFirestore db, DocumentReference counterRef,
                                  String type, String status, String title, String source,
                                  String detailAction, String metaLabel, String metaPrimary,
                                  String metaSecondary, List<String> tags) {
        db.runTransaction(transaction -> {
            Long current = transaction.get(counterRef).getLong("value");
            long nextId = (current != null ? current : 0L) + 1;
            transaction.update(counterRef, "value", nextId);

            Map<String, Object> data = new HashMap<>();
            data.put("id", nextId);
            data.put("type", type);
            data.put("status", status);
            data.put("timestamp", FieldValue.serverTimestamp());
            data.put("time", currentTime());
            data.put("title", title);
            data.put("source", source);
            data.put("detailLabel", "Accion Especifica");
            data.put("detailAction", detailAction);
            data.put("metaLabel", metaLabel);
            data.put("metaPrimary", metaPrimary);
            data.put("metaSecondary", metaSecondary);
            data.put("tags", tags);

            transaction.set(db.collection(COLLECTION_LOGS).document(), data);
            return null;
        }).addOnFailureListener(e -> Log.e(TAG, "No se pudo registrar el log", e));
    }

    private static String currentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        formatter.setTimeZone(LIMA_TZ);
        return formatter.format(new Date()) + " hrs";
    }
}
