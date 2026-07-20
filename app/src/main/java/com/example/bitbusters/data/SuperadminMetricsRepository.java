package com.example.bitbusters.data;

import com.example.bitbusters.utils.MoneyParser;
import com.example.bitbusters.utils.SeparacionFechaParser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Arma un {@link SuperadminMetricsSnapshot} real a partir de Firestore para el
 * dashboard y los reportes de superadmin. Sigue el mismo patrón de Tasks anidados
 * que {@code AsesorReportesActivity.cargarKPIs()}: no hay coroutines ni streams en
 * el proyecto, así que se encadenan `.get()` + `addOnSuccessListener` colección por
 * colección, cayendo a listas vacías si alguna falla en vez de romper el conteo.
 */
public class SuperadminMetricsRepository {

    public interface MetricsCallback {
        void onLoaded(SuperadminMetricsSnapshot snapshot);
    }

    public void loadSnapshot(MetricsCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").get().addOnSuccessListener(usersSnap -> {
            List<SuperadminMetricsSnapshot.UserRecord> usuarios = new ArrayList<>();
            // uidAsesor de `separaciones` es el Firebase Auth UID; el doc-id de `users`
            // debería coincidir con ese uid (así lo asume el resto de la app), pero el
            // alta real de asesores vía FirestoreAsesoresRepository usa el email como
            // doc-id y nunca crea la cuenta de Auth. Guardamos por doc-id (mejor caso)
            // para poder unir `separaciones` -> inmobiliaria cuando sí coincide.
            Map<String, String> inmobiliariaPorDocId = new HashMap<>();

            for (DocumentSnapshot doc : usersSnap.getDocuments()) {
                String role = doc.getString("role");
                if (role == null) continue;
                String status = doc.getString("status");
                if (status == null) status = "active";
                String inmobiliaria = doc.getString("inmobiliaria");
                java.util.Date fechaRegistro = doc.getTimestamp("fechaRegistro") != null
                        ? doc.getTimestamp("fechaRegistro").toDate() : null;

                usuarios.add(new SuperadminMetricsSnapshot.UserRecord(role, status, inmobiliaria, fechaRegistro));
                if (inmobiliaria != null && !inmobiliaria.trim().isEmpty()) {
                    inmobiliariaPorDocId.put(doc.getId(), inmobiliaria);
                }
            }

            db.collection("separaciones").get().addOnSuccessListener(sepsSnap ->
                    loadCitas(db, callback, usuarios, buildReservas(sepsSnap, inmobiliariaPorDocId))
            ).addOnFailureListener(e ->
                    loadCitas(db, callback, usuarios, new ArrayList<>())
            );
        }).addOnFailureListener(e ->
                loadCitas(db, callback, new ArrayList<>(), new ArrayList<>())
        );
    }

    private List<SuperadminMetricsSnapshot.ReservationRecord> buildReservas(
            QuerySnapshot sepsSnap, Map<String, String> inmobiliariaPorDocId) {
        List<SuperadminMetricsSnapshot.ReservationRecord> reservas = new ArrayList<>();
        for (QueryDocumentSnapshot doc : sepsSnap) {
            String uidAsesor = doc.getString("uidAsesor");
            String inmobiliaria = uidAsesor != null ? inmobiliariaPorDocId.get(uidAsesor) : null;
            Double monto = MoneyParser.parse(doc.get("monto"));
            java.util.Date fechaVisita = SeparacionFechaParser.parse(doc.get("fecha"));
            java.util.Date fechaCreacion = doc.getTimestamp("timestamp") != null
                    ? doc.getTimestamp("timestamp").toDate() : null;
            reservas.add(new SuperadminMetricsSnapshot.ReservationRecord(inmobiliaria, monto, fechaVisita, fechaCreacion));
        }
        return reservas;
    }

    private void loadCitas(FirebaseFirestore db, MetricsCallback callback,
                            List<SuperadminMetricsSnapshot.UserRecord> usuarios,
                            List<SuperadminMetricsSnapshot.ReservationRecord> reservas) {
        db.collection("citas").get().addOnSuccessListener(citasSnap -> {
            List<SuperadminMetricsSnapshot.CitaRecord> citas = new ArrayList<>();
            for (QueryDocumentSnapshot doc : citasSnap) {
                String estado = doc.getString("estado");
                java.util.Date fechaCreacion = doc.getTimestamp("creadoEn") != null
                        ? doc.getTimestamp("creadoEn").toDate() : null;
                citas.add(new SuperadminMetricsSnapshot.CitaRecord(estado, fechaCreacion));
            }
            loadLogs(db, callback, usuarios, reservas, citas);
        }).addOnFailureListener(e -> loadLogs(db, callback, usuarios, reservas, new ArrayList<>()));
    }

    private void loadLogs(FirebaseFirestore db, MetricsCallback callback,
                           List<SuperadminMetricsSnapshot.UserRecord> usuarios,
                           List<SuperadminMetricsSnapshot.ReservationRecord> reservas,
                           List<SuperadminMetricsSnapshot.CitaRecord> citas) {
        db.collection("logs").get().addOnSuccessListener(logsSnap ->
                callback.onLoaded(new SuperadminMetricsSnapshot(usuarios, reservas, citas, logsSnap.size()))
        ).addOnFailureListener(e ->
                callback.onLoaded(new SuperadminMetricsSnapshot(usuarios, reservas, citas, 0))
        );
    }
}
