package com.example.bitbusters.repository;

import android.util.Log;

import com.example.bitbusters.models.Cita;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Operaciones Firestore para citas/{citaId} y slots/{slotId}.
 *
 * Índices compuestos requeridos (crear en consola Firebase):
 *   slots: proyectoId ASC + ocupado ASC + fechaTimestamp ASC
 *   citas:  uidCliente ASC + fechaTimestamp DESC (solo si se usa orderBy en escucharCitasCliente)
 * Ver PENDING_COORDINATION.md § PC-04.
 */
public class CitaRepository {

    private static final String TAG    = "CitaRepository";
    private static final String CITAS  = "citas";
    private static final String SLOTS  = "slots";
    private static final TimeZone LIMA = TimeZone.getTimeZone("America/Lima");

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ── Interfaces ──────────────────────────────────────────────────────────────

    public interface CitasClienteListener {
        void onCitasActualizadas(List<Cita> citas);
        void onError(String mensaje);
    }

    public interface SlotsOcupadosListener {
        void onSlotsActualizados(Set<String> slotIds);
        void onError(String mensaje);
    }

    // ── Helpers estáticos ───────────────────────────────────────────────────────

    /**
     * Genera el slotId determinístico: "{proyectoId}_{yyyyMMdd}_{HHmm}".
     * @param month1based mes del año (1=enero…12=diciembre)
     * @param hora24      código de 4 dígitos: "0900", "1000", "1400", etc.
     */
    public static String generarSlotId(String proyectoId, int year, int month1based, int day, String hora24) {
        String fecha = String.format(Locale.US, "%04d%02d%02d", year, month1based, day);
        return proyectoId + "_" + fecha + "_" + hora24;
    }

    private static String generarClienteSlotId(String uidCliente, Date fechaTimestamp) {
        Calendar cal = Calendar.getInstance(LIMA);
        cal.setTime(fechaTimestamp);
        return "cliente_" + uidCliente + "_"
                + String.format(Locale.US, "%04d%02d%02d_%02d%02d",
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE));
    }

    /**
     * Calcula el java.util.Date de la cita en zona America/Lima.
     * @param month0based mes (0=enero…11=diciembre), igual que Calendar.MONTH
     * @param hora24      "0900", "1000", "1400", "1600", etc.
     */
    public static Date calcularFechaTimestamp(int year, int month0based, int day, String hora24) {
        int hh = Integer.parseInt(hora24.substring(0, 2));
        int mm = Integer.parseInt(hora24.substring(2, 4));
        Calendar cal = Calendar.getInstance(LIMA);
        cal.set(year, month0based, day, hh, mm, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /** Primer instante del mes dado en zona Lima. */
    public static Date inicioDelMes(int year, int month0based) {
        Calendar cal = Calendar.getInstance(LIMA);
        cal.set(year, month0based, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /** Primer instante del mes siguiente al dado en zona Lima. */
    public static Date inicioDelMesSiguiente(int year, int month0based) {
        Calendar cal = Calendar.getInstance(LIMA);
        cal.set(year, month0based, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.MONTH, 1);
        return cal.getTime();
    }

    // ── Reservar cita (runTransaction anti-colisión) ────────────────────────────

    public Task<Void> reservarCita(String uidCliente, String nombreCliente,
                                    String uidAsesor, String proyectoId, String proyectoNombre,
                                    String slotId, Date fechaTimestamp) {
        DocumentReference slotRef = db.collection(SLOTS).document(slotId);
        String clienteSlotId = generarClienteSlotId(uidCliente, fechaTimestamp);
        DocumentReference clienteSlotRef = db.collection(SLOTS).document(clienteSlotId);
        DocumentReference citaRef = db.collection(CITAS).document(); // ID pre-generado

        return db.runTransaction(tx -> {
            DocumentSnapshot slotSnap = tx.get(slotRef);
            if (slotSnap.exists() && Boolean.TRUE.equals(slotSnap.getBoolean("ocupado"))) {
                throw new FirebaseFirestoreException(
                        "Slot ya reservado", FirebaseFirestoreException.Code.ABORTED);
            }
            DocumentSnapshot clienteSlotSnap = tx.get(clienteSlotRef);
            if (clienteSlotSnap.exists() && Boolean.TRUE.equals(clienteSlotSnap.getBoolean("ocupado"))) {
                throw new FirebaseFirestoreException(
                        "Ya tienes una cita en este horario", FirebaseFirestoreException.Code.ABORTED);
            }

            Map<String, Object> slotData = new HashMap<>();
            slotData.put("ocupado",        true);
            slotData.put("citaId",         citaRef.getId());
            slotData.put("uidCliente",     uidCliente);
            slotData.put("proyectoId",     proyectoId);
            slotData.put("fechaTimestamp", fechaTimestamp);
            slotData.put("creadoEn",       FieldValue.serverTimestamp());
            tx.set(slotRef, slotData);

            Map<String, Object> clienteSlotData = new HashMap<>();
            clienteSlotData.put("ocupado",        true);
            clienteSlotData.put("citaId",         citaRef.getId());
            clienteSlotData.put("uidCliente",     uidCliente);
            clienteSlotData.put("proyectoId",     proyectoId);
            clienteSlotData.put("slotProyectoId", slotId);
            clienteSlotData.put("fechaTimestamp", fechaTimestamp);
            clienteSlotData.put("tipo",           "cliente_global");
            clienteSlotData.put("creadoEn",       FieldValue.serverTimestamp());
            tx.set(clienteSlotRef, clienteSlotData);

            Map<String, Object> citaData = new HashMap<>();
            citaData.put("uidCliente",                 uidCliente);
            citaData.put("uidAsesor",                  uidAsesor);
            citaData.put("proyectoId",                 proyectoId);
            citaData.put("proyectoNombre",             proyectoNombre);
            citaData.put("nombreCliente",              nombreCliente);
            citaData.put("slotId",                     slotId);
            citaData.put("clienteSlotId",              clienteSlotId);
            citaData.put("fechaTimestamp",             fechaTimestamp);
            citaData.put("estado",                     Cita.ESTADO_PENDIENTE);
            citaData.put("creadoEn",                   FieldValue.serverTimestamp());
            citaData.put("actualizadoEn",              FieldValue.serverTimestamp());
            citaData.put("historialReagendamientos",   Collections.emptyList());
            tx.set(citaRef, citaData);

            return null;
        });
    }

    // ── Cancelar cita (WriteBatch) ──────────────────────────────────────────────

    public Task<Void> cancelarCita(String citaId, String slotId, String motivoCancelacion) {
        DocumentReference citaRef = db.collection(CITAS).document(citaId);
        return db.runTransaction(tx -> {
            DocumentSnapshot citaSnap = tx.get(citaRef);
            String clienteSlotId = citaSnap.getString("clienteSlotId");

            Map<String, Object> citaUpdates = new HashMap<>();
            citaUpdates.put("estado",              Cita.ESTADO_CANCELADA);
            citaUpdates.put("actualizadoEn",       FieldValue.serverTimestamp());
            citaUpdates.put("motivoCancelacion",   motivoCancelacion);
            tx.update(citaRef, citaUpdates);

            if (slotId != null && !slotId.isEmpty()) {
                liberarSlot(tx, db.collection(SLOTS).document(slotId), citaId);
            }
            if (clienteSlotId != null && !clienteSlotId.isEmpty()) {
                liberarSlot(tx, db.collection(SLOTS).document(clienteSlotId), citaId);
            }
            return null;
        });
    }

    // ── Reagendar cita (runTransaction) ─────────────────────────────────────────

    public Task<Void> reagendarCita(String citaId, String slotIdAnterior,
                                     String nuevoSlotId, Date nuevaFechaTimestamp) {
        DocumentReference citaRef      = db.collection(CITAS).document(citaId);
        DocumentReference slotAntRef   = db.collection(SLOTS).document(slotIdAnterior);
        DocumentReference nuevoSlotRef = db.collection(SLOTS).document(nuevoSlotId);

        return db.runTransaction(tx -> {
            // 1. Verificar nuevo slot disponible
            DocumentSnapshot nuevoSnap = tx.get(nuevoSlotRef);
            if (nuevoSnap.exists() && Boolean.TRUE.equals(nuevoSnap.getBoolean("ocupado"))) {
                throw new FirebaseFirestoreException(
                        "Horario no disponible", FirebaseFirestoreException.Code.ABORTED);
            }

            // 2. Leer cita actual para construir entrada de historial
            DocumentSnapshot citaSnap  = tx.get(citaRef);
            Date   fechaAnterior = citaSnap.getDate("fechaTimestamp");
            String slotAnterior  = citaSnap.getString("slotId");
            String uidCliente    = citaSnap.getString("uidCliente");
            String proyectoId    = citaSnap.getString("proyectoId");
            String clienteSlotAnterior = citaSnap.getString("clienteSlotId");
            String nuevoClienteSlotId = generarClienteSlotId(uidCliente, nuevaFechaTimestamp);
            DocumentReference nuevoClienteSlotRef = db.collection(SLOTS).document(nuevoClienteSlotId);
            DocumentSnapshot nuevoClienteSlotSnap = tx.get(nuevoClienteSlotRef);
            if (nuevoClienteSlotSnap.exists() && Boolean.TRUE.equals(nuevoClienteSlotSnap.getBoolean("ocupado"))) {
                throw new FirebaseFirestoreException(
                        "Ya tienes una cita en este horario", FirebaseFirestoreException.Code.ABORTED);
            }

            Map<String, Object> histEntry = new HashMap<>();
            histEntry.put("slotAnterior",           slotAnterior != null ? slotAnterior : slotIdAnterior);
            histEntry.put("fechaTimestampAnterior",  fechaAnterior);
            histEntry.put("reagendadoEn",            new Date()); // serverTimestamp no válido dentro de arrays

            // 3. Liberar slot anterior
            if (slotIdAnterior != null && !slotIdAnterior.isEmpty()) {
                liberarSlot(tx, slotAntRef, citaId);
            }
            if (clienteSlotAnterior != null && !clienteSlotAnterior.isEmpty()) {
                liberarSlot(tx, db.collection(SLOTS).document(clienteSlotAnterior), citaId);
            }

            // 4. Ocupar nuevo slot
            Map<String, Object> nuevoSlotData = new HashMap<>();
            nuevoSlotData.put("ocupado",        true);
            nuevoSlotData.put("citaId",         citaId);
            nuevoSlotData.put("uidCliente",     uidCliente);
            nuevoSlotData.put("proyectoId",     proyectoId);
            nuevoSlotData.put("fechaTimestamp", nuevaFechaTimestamp);
            nuevoSlotData.put("creadoEn",       FieldValue.serverTimestamp());
            tx.set(nuevoSlotRef, nuevoSlotData);

            Map<String, Object> nuevoClienteSlotData = new HashMap<>();
            nuevoClienteSlotData.put("ocupado",        true);
            nuevoClienteSlotData.put("citaId",         citaId);
            nuevoClienteSlotData.put("uidCliente",     uidCliente);
            nuevoClienteSlotData.put("proyectoId",     proyectoId);
            nuevoClienteSlotData.put("slotProyectoId", nuevoSlotId);
            nuevoClienteSlotData.put("fechaTimestamp", nuevaFechaTimestamp);
            nuevoClienteSlotData.put("tipo",           "cliente_global");
            nuevoClienteSlotData.put("creadoEn",       FieldValue.serverTimestamp());
            tx.set(nuevoClienteSlotRef, nuevoClienteSlotData);

            // 5. Actualizar la cita
            Map<String, Object> citaUpdate = new HashMap<>();
            citaUpdate.put("slotId",                    nuevoSlotId);
            citaUpdate.put("clienteSlotId",             nuevoClienteSlotId);
            citaUpdate.put("fechaTimestamp",            nuevaFechaTimestamp);
            citaUpdate.put("actualizadoEn",             FieldValue.serverTimestamp());
            citaUpdate.put("historialReagendamientos",  FieldValue.arrayUnion(histEntry));
            tx.update(citaRef, citaUpdate);

            return null;
        });
    }

    private void liberarSlot(com.google.firebase.firestore.Transaction tx,
                             DocumentReference slotRef,
                             String citaId) {
        Map<String, Object> slotUpdate = new HashMap<>();
        slotUpdate.put("ocupado",        false);
        slotUpdate.put("canceladoEn",    FieldValue.serverTimestamp());
        slotUpdate.put("citaIdAnterior", citaId);
        tx.set(slotRef, slotUpdate, com.google.firebase.firestore.SetOptions.merge());
    }

    // ── Listeners en tiempo real ─────────────────────────────────────────────────

    /**
     * Escucha las citas del cliente en tiempo real.
     * No usa orderBy para evitar requerir índice compuesto.
     * El orden se aplica en memoria dentro de MisCitasActivity.
     */
    public ListenerRegistration escucharCitasCliente(String uidCliente, CitasClienteListener listener) {
        return db.collection(CITAS)
                .whereEqualTo("uidCliente", uidCliente)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error escuchando citas: " + e.getMessage());
                        listener.onError(e.getMessage());
                        return;
                    }
                    if (snap == null) {
                        listener.onCitasActualizadas(new ArrayList<>());
                        return;
                    }
                    List<Cita> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        try {
                            Cita c = doc.toObject(Cita.class);
                            if (c != null) {
                                c.setId(doc.getId());
                                lista.add(c);
                            }
                        } catch (Exception ex) {
                            Log.w(TAG, "No se pudo deserializar cita " + doc.getId(), ex);
                        }
                    }
                    // Ordenar por fecha descendente (más reciente primero)
                    lista.sort((a, b) -> {
                        Date da = a.getFechaTimestamp();
                        Date db2 = b.getFechaTimestamp();
                        if (da == null && db2 == null) return 0;
                        if (da == null) return 1;
                        if (db2 == null) return -1;
                        return db2.compareTo(da);
                    });
                    listener.onCitasActualizadas(lista);
                });
    }

    /**
     * Escucha los slots ocupados de un proyecto en un rango de mes.
     * Requiere índice compuesto: proyectoId ASC + ocupado ASC + fechaTimestamp ASC.
     */
    public ListenerRegistration escucharSlotsOcupados(String proyectoId, Date inicioMes, Date finMes,
                                                       SlotsOcupadosListener listener) {
        return db.collection(SLOTS)
                .whereEqualTo("proyectoId", proyectoId)
                .whereEqualTo("ocupado", true)
                .whereGreaterThanOrEqualTo("fechaTimestamp", inicioMes)
                .whereLessThan("fechaTimestamp", finMes)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error escuchando slots: " + e.getMessage());
                        listener.onError(e.getMessage());
                        return;
                    }
                    Set<String> ocupados = new HashSet<>();
                    if (snap != null) {
                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            ocupados.add(doc.getId());
                        }
                    }
                    listener.onSlotsActualizados(ocupados);
                });
    }
}
