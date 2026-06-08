package com.example.bitbusters.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Fuente de verdad para coordenadas y dirección de proyectos.
 *
 * IMPLEMENTACIÓN ACTUAL: HashMap estático (datos locales en memoria).
 *
 * TODO Firebase: Cuando migremos a Firestore, reemplazar el HashMap
 * por una llamada a:
 *   FirebaseFirestore.getInstance()
 *       .collection("proyectos")
 *       .document(idProyecto)
 *       .get()
 *       .addOnSuccessListener(doc -> callback.onSuccess(
 *           doc.getDouble("latitud"), doc.getDouble("longitud"), doc.getString("direccion")))
 *       .addOnFailureListener(e -> callback.onError(e.getMessage()));
 *
 * El resto del código (ProjectDetailActivity, MapHelper, etc.) NO necesita cambios
 * porque consume esta clase solo a través de CoordenadasCallback.
 */
public class UbicacionRepository {

    public interface CoordenadasCallback {
        void onSuccess(double latitud, double longitud, String direccion);
        void onError(String mensaje);
    }

    // ── Datos locales (reemplazar por Firestore en la migración) ──────────────

    private static final Map<String, double[]> COORDS = new HashMap<>();
    private static final Map<String, String>   DIRS   = new HashMap<>();

    static {
        // Catalina Ventor — Av. La Marina, San Miguel
        COORDS.put("Catalina Ventor",      new double[]{-12.0775, -77.0830});
        DIRS.put  ("Catalina Ventor",      "Av. La Marina 2355, San Miguel, Lima");

        // Residencial Park — Av. Larco, Miraflores
        COORDS.put("Residencial Park",     new double[]{-12.1196, -77.0292});
        DIRS.put  ("Residencial Park",     "Av. Larco 345, Miraflores, Lima");
        COORDS.put("Residencial El Park",  new double[]{-12.1196, -77.0292});
        DIRS.put  ("Residencial El Park",  "Av. Larco 345, Miraflores, Lima");

        // Torre Miramar — Av. Arequipa, Lince
        COORDS.put("Torre Miramar",        new double[]{-12.0875, -77.0367});
        DIRS.put  ("Torre Miramar",        "Av. Arequipa 2450, Lince, Lima");

        // Condominio Las Lomas — Av. Javier Prado, La Molina
        COORDS.put("Condominio Las Lomas", new double[]{-12.0867, -76.9619});
        DIRS.put  ("Condominio Las Lomas", "Av. Javier Prado Este 4500, La Molina, Lima");

        // Catalina Sky — Av. Pardo, Miraflores
        COORDS.put("Catalina Sky",         new double[]{-12.1158, -77.0339});
        DIRS.put  ("Catalina Sky",         "Av. Pardo 600, Miraflores, Lima");
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    @SuppressWarnings("unused")
    private final Context context;

    public UbicacionRepository(Context context) {
        // Context guardado para cuando la implementación acceda a Room/Firestore
        this.context = context.getApplicationContext();
    }

    // ── Consulta principal ────────────────────────────────────────────────────

    /**
     * Devuelve las coordenadas y dirección de un proyecto.
     * El callback siempre se ejecuta en el hilo principal (preparado para operaciones async).
     *
     * @param idProyecto nombre del proyecto (identificador actual, futuro: Firestore document ID)
     * @param callback   resultado en hilo principal
     */
    public void obtenerCoordenadasProyecto(String idProyecto, CoordenadasCallback callback) {
        // Ejecutar en background para mantener el patrón async que usará Firestore
        Executors.newSingleThreadExecutor().execute(() -> {
            double[] coords   = COORDS.get(idProyecto);
            String   direccion = DIRS.containsKey(idProyecto)
                    ? DIRS.get(idProyecto)
                    : idProyecto;

            if (coords != null) {
                Log.d("UbicacionRepo", "Proyecto '" + idProyecto + "' → " + coords[0] + ", " + coords[1]);
            } else {
                Log.d("UbicacionRepo", "Proyecto '" + idProyecto + "' → sin coordenadas");
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                if (coords != null) {
                    callback.onSuccess(coords[0], coords[1], direccion);
                } else {
                    callback.onError("Coordenadas no disponibles para este proyecto");
                }
            });
        });
    }
}
