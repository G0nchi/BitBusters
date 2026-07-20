package com.example.bitbusters.data;

import com.example.bitbusters.models.AdminSeparacion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private static final String COLECCION_NOTIFICACIONES = "notifications";

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

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference separacionRef = db.collection(COLECCION_SEPARACIONES).document(id);
        FirebaseUser admin = FirebaseAuth.getInstance().getCurrentUser();
        String adminUid = admin != null ? admin.getUid() : "";
        AdminSeparacion separacion = getById(id);

        Map<String, Object> cambios = new HashMap<>();
        cambios.put("estado", nuevoEstado);
        cambios.put("fechaActualizacion", FieldValue.serverTimestamp());

        if ("Aprobada".equalsIgnoreCase(nuevoEstado)) {
            cambios.put("estadoPago", "Pendiente");
            cambios.put("fechaAprobacion", FieldValue.serverTimestamp());
            if (!adminUid.isEmpty()) cambios.put("aprobadoPorUid", adminUid);
        } else if ("Rechazada".equalsIgnoreCase(nuevoEstado)) {
            cambios.put("fechaRechazo", FieldValue.serverTimestamp());
            if (!adminUid.isEmpty()) cambios.put("rechazadoPorUid", adminUid);
        }

        WriteBatch batch = db.batch();
        batch.update(separacionRef, cambios);
        agregarNotificacionesCambioEstado(batch, db, separacion, nuevoEstado, adminUid);

        batch.commit()
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

        AdminSeparacion separacion = new AdminSeparacion(
                doc.getId(),
                proyecto,
                monto,
                fecha,
                cliente,
                estado
        );
        separacion.setClienteUid(firstNonEmpty(
                doc.getString("clienteUid"),
                doc.getString("uidCliente")
        ));
        separacion.setUidAsesor(firstNonEmpty(
                doc.getString("uidAsesor"),
                doc.getString("asesorUid")
        ));
        separacion.setAsesorNombre(firstNonEmpty(
                doc.getString("asesorNombre"),
                doc.getString("asesor"),
                separacion.getUidAsesor()
        ));
        separacion.setProyectoId(firstNonEmpty(
                doc.getString("proyectoId"),
                doc.getString("idProyecto")
        ));
        separacion.setInmobiliariaId(firstNonEmpty(
                doc.getString("inmobiliariaId"),
                doc.getString("empresaId")
        ));
        separacion.setEstadoPago(normalizarEstadoPago(firstNonEmpty(
                doc.getString("estadoPago"),
                doc.getString("pagoEstado"),
                doc.getString("estado_pago")
        ), estado));
        separacion.setMetodoPago(firstNonEmpty(
                doc.getString("metodoPago"),
                doc.getString("medioPago"),
                doc.getString("paymentMethod")
        ));
        separacion.setComprobantePago(firstNonEmpty(
                doc.getString("comprobantePago"),
                doc.getString("comprobante"),
                doc.getString("comprobanteUrl"),
                doc.getString("voucherUrl")
        ));
        separacion.setObservacionPago(firstNonEmpty(
                doc.getString("observacionPago"),
                doc.getString("observacion"),
                doc.getString("comentarioPago")
        ));
        return separacion;
    }

    private static void agregarNotificacionesCambioEstado(
            WriteBatch batch,
            FirebaseFirestore db,
            AdminSeparacion separacion,
            String nuevoEstado,
            String adminUid
    ) {
        if (batch == null || db == null || separacion == null || nuevoEstado == null) return;

        boolean aprobada = "Aprobada".equalsIgnoreCase(nuevoEstado);
        boolean rechazada = "Rechazada".equalsIgnoreCase(nuevoEstado);
        if (!aprobada && !rechazada) return;

        String proyecto = firstNonEmpty(separacion.getNombreProyecto(), "el proyecto");
        String cliente = firstNonEmpty(separacion.getCliente(), "el cliente");
        String type = aprobada ? "separacion_aprobada" : "separacion_rechazada";

        if (!separacion.getClienteUid().isEmpty()) {
            String titulo = aprobada ? "Separación aprobada" : "Separación rechazada";
            String descripcion = aprobada
                    ? "Tu separación para " + proyecto + " fue aprobada. Ya puedes completar el pago."
                    : "Tu separación para " + proyecto + " fue rechazada.";
            batch.set(
                    db.collection(COLECCION_NOTIFICACIONES).document(),
                    crearNotificacion(
                            "cliente",
                            separacion.getClienteUid(),
                            type,
                            titulo,
                            descripcion,
                            separacion,
                            adminUid
                    )
            );
        }

        if (!separacion.getUidAsesor().isEmpty()) {
            String titulo = aprobada ? "Separación aprobada" : "Separación rechazada";
            String descripcion = aprobada
                    ? "La separación de " + cliente + " para " + proyecto + " fue aprobada."
                    : "La separación de " + cliente + " para " + proyecto + " fue rechazada.";
            batch.set(
                    db.collection(COLECCION_NOTIFICACIONES).document(),
                    crearNotificacion(
                            "asesor",
                            separacion.getUidAsesor(),
                            type,
                            titulo,
                            descripcion,
                            separacion,
                            adminUid
                    )
            );
        }
    }

    private static Map<String, Object> crearNotificacion(
            String role,
            String targetUid,
            String type,
            String title,
            String descripcion,
            AdminSeparacion separacion,
            String adminUid
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("role", role);
        data.put("targetRole", role);
        data.put("targetUid", targetUid);
        data.put("type", type);
        data.put("title", title);
        data.put("senderName", "Administrador de inmobiliaria");
        data.put("descripcion", descripcion);
        data.put("tiempo", "ahora");
        data.put("order", System.currentTimeMillis());
        data.put("isOld", false);
        data.put("read", false);
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("separacionId", separacion.getId());
        data.put("proyectoId", separacion.getProyectoId());
        data.put("proyecto", separacion.getNombreProyecto());
        data.put("clienteUid", separacion.getClienteUid());
        data.put("cliente", separacion.getCliente());
        data.put("uidAsesor", separacion.getUidAsesor());
        data.put("asesorNombre", separacion.getAsesorNombre());
        data.put("inmobiliariaId", separacion.getInmobiliariaId());
        if (adminUid != null && !adminUid.isEmpty()) {
            data.put("adminUid", adminUid);
        }
        return data;
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

    private static String normalizarEstadoPago(String estadoPago, String estadoSeparacion) {
        String normalized = estadoPago == null ? "" : estadoPago.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("pagada") || normalized.equals("pagado")
                || normalized.equals("paid") || normalized.equals("completado")
                || normalized.equals("completada")) {
            return "Pagado";
        }
        if (normalized.equals("rechazado") || normalized.equals("rechazada")
                || normalized.equals("failed") || normalized.equals("fallido")) {
            return "Rechazado";
        }
        if (normalized.equals("pendiente") || normalized.equals("pendiente_pago")
                || normalized.equals("pending")) {
            return "Pendiente";
        }
        if ("Aprobada".equalsIgnoreCase(estadoSeparacion)) {
            return "Pendiente";
        }
        return "No habilitado";
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
