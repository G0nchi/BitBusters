package com.example.bitbusters.data;

import com.example.bitbusters.models.AdminSeparacion;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Repositorio estático en memoria para la lista de separaciones del Administrador.
 * Actúa como fuente única de verdad (single source of truth) compartida entre
 * AdminSeparacionesActivity y AdminDetallesSeparacionActivity.
 *
 * La lista se inicializa una sola vez con los datos de AdminDataRepository y
 * persiste durante la sesión de la app (en memoria de proceso).
 */
public final class SeparacionesRepository {

    // Lista estática compartida — se inicializa solo la primera vez
    private static List<AdminSeparacion> lista = null;
    private static final String COLECCION_SEPARACIONES = "separaciones";

    public interface SeparacionesListener {
        void onSeparacionesActualizadas(List<AdminSeparacion> separaciones);
        void onError(String mensaje);
    }

    public interface ActualizarEstadoCallback {
        void onSuccess();
        void onError(String mensaje);
    }

    // Constructor privado — no instanciar
    private SeparacionesRepository() {}

    /**
     * Retorna la lista viva de separaciones.
     * Si aún no se ha inicializado, la carga desde AdminDataRepository.
     */
    public static List<AdminSeparacion> getLista() {
        if (lista == null) {
            lista = new ArrayList<>(AdminDataRepository.getSeparaciones());
        }
        return lista;
    }

    /**
     * Agrega una nueva separación al INICIO de la lista (más reciente primero).
     *
     * @param separacion La separación recién creada para añadir.
     */
    public static void agregar(AdminSeparacion separacion) {
        getLista().add(0, separacion);
    }

    /**
     * Busca una separación por su ID único.
     *
     * @param id El ID de la separación a buscar.
     * @return La separación encontrada, o null si no existe.
     */
    public static AdminSeparacion getById(String id) {
        if (id == null) return null;
        for (AdminSeparacion s : getLista()) {
            if (id.equals(s.getId())) {
                return s;
            }
        }
        return null;
    }

    /**
     * Actualiza el estado de una separación existente por ID.
     * Los estados válidos son: "Pendiente", "Aprobada", "Rechazada".
     *
     * @param id          El ID de la separación a actualizar.
     * @param nuevoEstado El nuevo estado a asignar.
     */
    public static void actualizarEstado(String id, String nuevoEstado) {
        if (id == null || nuevoEstado == null) return;
        for (AdminSeparacion s : getLista()) {
            if (id.equals(s.getId())) {
                s.setEstado(nuevoEstado);
                return;
            }
        }
    }

    public static ListenerRegistration escucharDesdeFirestore(SeparacionesListener listener) {
        return escucharDesdeFirestore("", listener);
    }

    public static ListenerRegistration escucharDesdeFirestore(
            String inmobiliariaId,
            SeparacionesListener listener
    ) {
        return FirebaseFirestore.getInstance()
                .collection(COLECCION_SEPARACIONES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        if (listener != null) {
                            listener.onError(error.getMessage() != null
                                    ? error.getMessage()
                                    : "No se pudo escuchar separaciones");
                        }
                        return;
                    }

                    List<AdminSeparacion> remotas = new ArrayList<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            if (!perteneceAInmobiliaria(doc, inmobiliariaId)) continue;
                            remotas.add(toAdminSeparacion(doc));
                        }
                    }

                    lista = remotas;
                    if (listener != null) {
                        listener.onSeparacionesActualizadas(getLista());
                    }
                });
    }

    public static void actualizarEstadoEnFirestore(
            String id,
            String nuevoEstado,
            ActualizarEstadoCallback callback
    ) {
        if (id == null || id.trim().isEmpty() || nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            if (callback != null) callback.onError("Separación inválida");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection(COLECCION_SEPARACIONES)
                .document(id)
                .update(
                        "estado", nuevoEstado,
                        "fechaActualizacion", FieldValue.serverTimestamp()
                )
                .addOnSuccessListener(unused -> {
                    actualizarEstado(id, nuevoEstado);
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(e.getMessage() != null
                                ? e.getMessage()
                                : "No se pudo actualizar la separación");
                    }
                });
    }

    /**
     * Retorna el índice (posición) de una separación en la lista por su ID.
     * Útil para hacer scroll hasta ella en el RecyclerView.
     *
     * @param id El ID de la separación.
     * @return El índice (0-based), o -1 si no se encuentra.
     */
    public static int getPosicion(String id) {
        if (id == null) return -1;
        List<AdminSeparacion> l = getLista();
        for (int i = 0; i < l.size(); i++) {
            if (id.equals(l.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    private static AdminSeparacion toAdminSeparacion(DocumentSnapshot doc) {
        String proyecto = firstNonEmpty(
                doc.getString("proyecto"),
                doc.getString("nombreProyecto"),
                doc.getString("proyectoNombre")
        );
        String monto = formatearMonto(firstNonEmpty(doc.getString("monto"), doc.getString("precio")));
        String fecha = firstNonEmpty(doc.getString("fecha"), fechaDesdeTimestamp(doc.getTimestamp("timestamp")));
        String hora = doc.getString("hora");
        if (hora != null && !hora.trim().isEmpty() && !fecha.contains(hora.trim())) {
            fecha = fecha.isEmpty() ? hora.trim() : fecha + " · " + hora.trim();
        }
        String cliente = firstNonEmpty(
                doc.getString("cliente"),
                doc.getString("clienteNombre"),
                doc.getString("nombreCliente")
        );
        String estado = normalizarEstado(firstNonEmpty(doc.getString("estado"), "Pendiente"));

        return new AdminSeparacion(
                doc.getId(),
                proyecto,
                monto,
                fecha,
                cliente,
                estado
        );
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

    private static String formatearMonto(String monto) {
        if (monto == null || monto.trim().isEmpty()) return "";
        String limpio = monto.trim();
        return limpio.toLowerCase(Locale.ROOT).startsWith("s/") ? limpio : "S/ " + limpio;
    }

    private static String fechaDesdeTimestamp(Timestamp timestamp) {
        return timestamp != null ? timestamp.toDate().toString() : "";
    }

    private static String normalizarEstado(String estado) {
        String normalized = estado == null ? "" : estado.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("aprobada") || normalized.equals("aprobado")
                || normalized.equals("approved") || normalized.equals("active")) {
            return "Aprobada";
        }
        if (normalized.equals("rechazada") || normalized.equals("rechazado")
                || normalized.equals("rejected") || normalized.equals("inactive")) {
            return "Rechazada";
        }
        return "Pendiente";
    }

    private static boolean perteneceAInmobiliaria(DocumentSnapshot doc, String inmobiliariaId) {
        if (inmobiliariaId == null || inmobiliariaId.trim().isEmpty()) return true;
        String docInmobiliariaId = firstNonEmpty(
                doc.getString("inmobiliariaId"),
                doc.getString("empresaId")
        );
        if (docInmobiliariaId.isEmpty()) {
            return true; // Compatibilidad con separaciones creadas antes de agregar inmobiliariaId.
        }
        return docInmobiliariaId.equalsIgnoreCase(inmobiliariaId.trim());
    }
}
