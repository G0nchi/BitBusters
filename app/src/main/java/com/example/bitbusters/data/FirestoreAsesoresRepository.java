package com.example.bitbusters.data;

import android.content.Context;

import com.example.bitbusters.models.AdminAsesorInmobiliaria;
import com.example.bitbusters.utils.AdminPreferencesManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FirestoreAsesoresRepository {

    public interface AsesoresCallback {
        void onSuccess(List<AdminAsesorInmobiliaria> asesores);
        void onError(String mensaje);
    }

    public interface GuardarAsesorCallback {
        void onSuccess();
        void onError(String mensaje);
    }

    private static final String COLECCION_USERS_LEGACY = "users";

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void registrarAsesor(
            Context context,
            String nombre,
            String email,
            String telefono,
            String dni,
            GuardarAsesorCallback callback
    ) {
        String inmobiliariaNombre = AdminPreferencesManager.obtenerInmobiliaria(context);
        String inmobiliariaId = AdminProyectosRepository.crearInmobiliariaId(inmobiliariaNombre);
        String emailNormalizado = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);

        Map<String, Object> data = new HashMap<>();
        data.put("email", emailNormalizado);
        data.put("inmobiliaria", inmobiliariaNombre);
        data.put("inmobiliariaId", inmobiliariaId);
        data.put("nombre", nombre == null ? "" : nombre.trim());
        data.put("role", "asesor");
        data.put("status", "pending");
        data.put("telefono", telefono == null ? "" : telefono.trim());
        data.put("dni", dni == null ? "" : dni.trim());
        data.put("fechaRegistro", FieldValue.serverTimestamp());

        DocumentReference asesorRef = firestore.collection(COLECCION_USERS_LEGACY)
                .document(documentIdFromEmail(emailNormalizado));

        asesorRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if (callback != null) callback.onError("Ya existe un asesor con este correo");
                        return;
                    }

                    asesorRef.set(data)
                            .addOnSuccessListener(unused -> {
                                if (callback != null) callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) {
                                    callback.onError(e.getMessage() != null
                                            ? e.getMessage()
                                            : "No se pudo registrar el asesor");
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(e.getMessage() != null
                                ? e.getMessage()
                                : "No se pudo validar el correo del asesor");
                    }
                });
    }

    public void obtenerAsesoresRegistrados(Context context, AsesoresCallback callback) {
        obtenerAsesoresRegistrados(context, false, callback);
    }

    public void obtenerAsesoresActivosRegistrados(Context context, AsesoresCallback callback) {
        obtenerAsesoresRegistrados(context, true, callback);
    }

    private void obtenerAsesoresRegistrados(Context context, boolean soloActivos, AsesoresCallback callback) {
        String inmobiliariaNombre = AdminPreferencesManager.obtenerInmobiliaria(context);
        String inmobiliariaId = AdminProyectosRepository.crearInmobiliariaId(inmobiliariaNombre);

        firestore.collection(COLECCION_USERS_LEGACY).get().addOnSuccessListener(snapshot -> {
            List<QuerySnapshot> snapshots = new ArrayList<>();
            snapshots.add(snapshot);

            List<AdminAsesorInmobiliaria> asesores = mergeSnapshots(
                    snapshots,
                    inmobiliariaId,
                    inmobiliariaNombre,
                    soloActivos
            );
            callback.onSuccess(asesores);
        }).addOnFailureListener(e ->
                callback.onError(e.getMessage() != null ? e.getMessage() : "No se pudo cargar asesores"));
    }

    private List<AdminAsesorInmobiliaria> mergeSnapshots(
            List<QuerySnapshot> snapshots,
            String inmobiliariaId,
            String inmobiliariaNombre,
            boolean soloActivos
    ) {
        Map<String, AsesorRecord> unique = new LinkedHashMap<>();

        for (QuerySnapshot snapshot : snapshots) {
            if (snapshot == null) continue;
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                if (!esAsesor(doc)) continue;
                if (!perteneceAInmobiliaria(doc, inmobiliariaId, inmobiliariaNombre)) continue;
                if (soloActivos && !estaActivo(doc)) continue;

                AdminAsesorInmobiliaria asesor = toItem(doc);
                String key = keyFor(doc, asesor);
                long order = orderFor(doc);

                AsesorRecord current = unique.get(key);
                if (current == null || order > current.order) {
                    unique.put(key, new AsesorRecord(asesor, order));
                }
            }
        }

        List<AsesorRecord> records = new ArrayList<>(unique.values());
        Collections.sort(records, (a, b) -> Long.compare(b.order, a.order));

        List<AdminAsesorInmobiliaria> result = new ArrayList<>();
        for (AsesorRecord record : records) {
            result.add(record.asesor);
        }
        return result;
    }

    private boolean estaActivo(DocumentSnapshot doc) {
        String estado = firstNonEmpty(
                doc.getString("estado"),
                doc.getString("status")
        );
        if (!isBlank(estado)) {
            String normalized = normalize(estado);
            return normalized.contains("activo")
                    || normalized.contains("habilitado")
                    || "active".equals(normalized)
                    || "enabled".equals(normalized);
        }

        Boolean activo = doc.getBoolean("activo");
        if (activo != null) {
            return activo;
        }

        Boolean habilitado = doc.getBoolean("habilitado");
        return habilitado != null && habilitado;
    }

    private boolean esAsesor(DocumentSnapshot doc) {
        String rol = firstNonEmpty(
                doc.getString("rol"),
                doc.getString("role"),
                doc.getString("tipoRol")
        );
        String normalizado = normalize(rol);
        return normalizado.contains("asesor") || "advisor".equals(normalizado);
    }

    private boolean perteneceAInmobiliaria(DocumentSnapshot doc, String inmobiliariaId, String inmobiliariaNombre) {
        String docInmobiliariaId = firstNonEmpty(
                doc.getString("inmobiliariaId"),
                doc.getString("empresaId")
        );
        String docInmobiliariaNombre = firstNonEmpty(
                doc.getString("inmobiliariaNombre"),
                doc.getString("empresa"),
                doc.getString("inmobiliaria")
        );

        if (isBlank(docInmobiliariaId) && isBlank(docInmobiliariaNombre)) {
            return true;
        }
        if (!isBlank(docInmobiliariaId) && docInmobiliariaId.equalsIgnoreCase(inmobiliariaId)) {
            return true;
        }
        return !isBlank(docInmobiliariaNombre)
                && normalize(docInmobiliariaNombre).equals(normalize(inmobiliariaNombre));
    }

    private AdminAsesorInmobiliaria toItem(DocumentSnapshot doc) {
        String nombre = firstNonEmpty(doc.getString("nombre"), doc.getString("fullName"), "Sin nombre");
        String email = firstNonEmpty(doc.getString("email"), doc.getString("correo"), "");
        String telefono = firstNonEmpty(doc.getString("telefono"), doc.getString("phone"), "");
        String estado = buildEstado(doc);
        String iniciales = buildInitials(nombre);
        String id = firstNonEmpty(doc.getString("uid"), doc.getId(), email, nombre);
        return new AdminAsesorInmobiliaria(id, nombre, iniciales, email, telefono, estado);
    }

    private String buildEstado(DocumentSnapshot doc) {
        String estado = firstNonEmpty(
                doc.getString("estado"),
                doc.getString("status")
        );
        if (!isBlank(estado)) {
            String normalized = normalize(estado);
            if (normalized.contains("activo") || "active".equals(normalized) || "enabled".equals(normalized)) {
                return "Activo";
            }
            if (normalized.contains("inactivo") || "inactive".equals(normalized) || "disabled".equals(normalized)) {
                return "Inactivo";
            }
            if (normalized.contains("pendiente") || "pending".equals(normalized)) {
                return "Pendiente";
            }
            return capitalize(estado);
        }

        Boolean activo = doc.getBoolean("activo");
        if (activo != null) {
            return activo ? "Activo" : "Inactivo";
        }

        Boolean habilitado = doc.getBoolean("habilitado");
        if (habilitado != null) {
            return habilitado ? "Activo" : "Inactivo";
        }

        return "Pendiente";
    }

    private long orderFor(DocumentSnapshot doc) {
        Timestamp timestamp = doc.getTimestamp("fechaRegistro");
        if (timestamp == null) {
            timestamp = doc.getTimestamp("createdAt");
        }
        return timestamp != null ? timestamp.toDate().getTime() : 0L;
    }

    private String documentIdFromEmail(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (isBlank(normalized)) {
            return firestore.collection(COLECCION_USERS_LEGACY).document().getId();
        }
        return normalized.replace("/", "_");
    }

    private String keyFor(DocumentSnapshot doc, AdminAsesorInmobiliaria asesor) {
        String uid = firstNonEmpty(doc.getString("uid"), doc.getId());
        if (!isBlank(uid)) {
            return uid.toLowerCase(Locale.ROOT);
        }
        String email = asesor.getEmail();
        if (!isBlank(email)) {
            return email.toLowerCase(Locale.ROOT);
        }
        return normalize(asesor.getNombre());
    }

    private String buildInitials(String nombre) {
        if (isBlank(nombre)) {
            return "--";
        }
        String[] parts = nombre.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        if (parts.length >= 2) {
            sb.append(parts[0].charAt(0));
            sb.append(parts[parts.length - 1].charAt(0));
        } else {
            sb.append(parts[0].charAt(0));
        }
        return sb.toString().toUpperCase(Locale.ROOT);
    }

    private String firstNonEmpty(String... values) {
        if (values == null) return "";
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalize(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}+", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    private String capitalize(String value) {
        if (isBlank(value)) return "";
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private static class AsesorRecord {
        final AdminAsesorInmobiliaria asesor;
        final long order;

        AsesorRecord(AdminAsesorInmobiliaria asesor, long order) {
            this.asesor = asesor;
            this.order = order;
        }
    }
}
