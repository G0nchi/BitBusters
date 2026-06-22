package com.example.bitbusters.repository;

import android.util.Log;

import com.example.bitbusters.models.Proyecto;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class ProyectoRepository {

    private static final String TAG       = "ProyectoRepo";
    private static final String COLECCION = "proyectos";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ── Interfaces ──────────────────────────────────────────────────────────────

    public interface ProyectosListener {
        void onProyectosActualizados(List<Proyecto> proyectos);
        void onError(String mensaje);
    }

    public interface ProyectoCallback {
        void onSuccess(Proyecto p);
        void onError(String mensaje);
    }

    // ── Listener en tiempo real (para HomeActivity y SearchActivity) ─────────────

    /**
     * Se suscribe a la colección "proyectos" y entrega la lista actualizada cada vez
     * que Firestore cambie. Filtra automáticamente los documentos con esDemo == true.
     * Los documentos sin ese campo (proyectos del admin) SÍ se incluyen.
     */
    public ListenerRegistration escucharProyectosCliente(ProyectosListener listener) {
        return db.collection(COLECCION)
            .addSnapshotListener((querySnapshot, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error escuchando proyectos: " + error.getMessage());
                    listener.onError(error.getMessage());
                    return;
                }

                if (querySnapshot == null) {
                    listener.onProyectosActualizados(new ArrayList<>());
                    return;
                }

                List<Proyecto> lista = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    try {
                        // Filtro: excluir solo cuando esDemo == true explícitamente
                        Boolean esDemo = doc.getBoolean("esDemo");
                        if (Boolean.TRUE.equals(esDemo)) continue;
                        Boolean visible = doc.getBoolean("visible");
                        if (Boolean.FALSE.equals(visible)) continue;
                        Boolean activo = doc.getBoolean("activo");
                        if (Boolean.FALSE.equals(activo)) continue;

                        Proyecto p = doc.toObject(Proyecto.class);
                        if (p == null) continue;

                        p.setId(doc.getId());
                        normalizarProyecto(doc, p);

                        lista.add(p);
                    } catch (Exception e) {
                        Log.w(TAG, "No se pudo deserializar proyecto " + doc.getId()
                                + ": " + e.getMessage());
                    }
                }

                Log.d(TAG, "Proyectos cargados (sin demos): " + lista.size());
                for (Proyecto p : lista) {
                    Log.d(TAG, "  - " + p.getNombre() + " | " + p.getUbicacion());
                }

                listener.onProyectosActualizados(lista);
            });
    }

    // ── Consulta de un solo proyecto por nombre (para ProjectDetailActivity) ────

    public void obtenerPorNombre(String nombre, ProyectoCallback callback) {
        db.collection(COLECCION)
            .whereEqualTo("nombre", nombre)
            .limit(1)
            .get()
            .addOnSuccessListener(querySnap -> {
                if (querySnap.isEmpty()) {
                    callback.onError("Proyecto no encontrado: " + nombre);
                    return;
                }
                DocumentSnapshot doc = querySnap.getDocuments().get(0);
                try {
                    Proyecto p = doc.toObject(Proyecto.class);
                    if (p == null) {
                        callback.onError("No se pudo deserializar el proyecto");
                        return;
                    }
                    p.setId(doc.getId());
                    normalizarProyecto(doc, p);
                    callback.onSuccess(p);
                } catch (Exception e) {
                    callback.onError("Error al deserializar proyecto: " + e.getMessage());
                }
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ── Consulta de un solo proyecto por ID (para deep link QR) ─────────────────

    public void obtenerPorId(String id, ProyectoCallback callback) {
        db.collection(COLECCION).document(id).get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    callback.onError("El proyecto no existe");
                    return;
                }
                try {
                    Proyecto p = doc.toObject(Proyecto.class);
                    if (p == null) {
                        callback.onError("No se pudo cargar el proyecto");
                        return;
                    }
                    p.setId(doc.getId());
                    normalizarProyecto(doc, p);
                    callback.onSuccess(p);
                } catch (Exception e) {
                    callback.onError("Error al deserializar proyecto: " + e.getMessage());
                }
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private void normalizarProyecto(DocumentSnapshot doc, Proyecto p) {
        if (p.precio == null || p.precio.isEmpty()) {
            String pub = doc.getString("precioPublicado");
            String total = doc.getString("precioTotal");
            p.precio = pub != null && !pub.isEmpty()
                    ? pub
                    : (total != null && !total.isEmpty() ? "S/ " + total : "");
        }
        if (p.ubicacion == null || p.ubicacion.isEmpty()) {
            String dist = doc.getString("distrito");
            p.ubicacion = dist != null ? dist : "";
        }
        if (p.tipo == null || p.tipo.isEmpty()) {
            String tipoDoc = doc.getString("tipo");
            p.tipo = tipoDoc != null && !tipoDoc.isEmpty() ? tipoDoc : "Departamento";
        }
        if (p.imageUrl == null || p.imageUrl.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<String> imgs = (List<String>) doc.get("imagenesUri");
            if (imgs != null && !imgs.isEmpty()) {
                p.imageUrl = imgs.get(0);
            }
        }
        if (p.rating == null || p.rating.isEmpty()) {
            Double ratingPromedio = doc.getDouble("ratingPromedio");
            p.rating = ratingPromedio != null && ratingPromedio > 0
                    ? String.format(java.util.Locale.getDefault(), "%.1f", ratingPromedio)
                    : "—";
        }
    }
}
