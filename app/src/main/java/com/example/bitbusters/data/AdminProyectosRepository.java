package com.example.bitbusters.data;

import android.content.Context;

import com.example.bitbusters.models.AdminProyecto;
import com.example.bitbusters.models.Tipologia;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Repositorio estático único para los proyectos del Administrador.
 * Diferente a ProjectSessionData (datos mock del cliente/asesor) — no mezclarlos.
 *
 * Patrón: singleton de lista estática con datos demo iniciales.
 * Más reciente primero (index 0).
 */
public class AdminProyectosRepository {

    /** Lista maestra — se inicializa con 3 proyectos demo */
    private static final List<AdminProyecto> proyectos = new ArrayList<>(crearDemoInicial());
    private static final String COLECCION_PROYECTOS = "proyectos";
    private static final String UID_ADMIN_MOCK = "admin_mock";

    /** Evita instanciación accidental */
    private AdminProyectosRepository() {}

    // ── API pública ──────────────────────────────────────────────────────────

    /**
     * Agrega un proyecto al inicio de la lista (más reciente primero).
     *
     * @param p Proyecto a agregar.
     */
    public static void agregar(AdminProyecto p) {
        proyectos.add(0, p);
    }

    /**
     * Devuelve la lista completa (más reciente primero).
     */
    public static List<AdminProyecto> getTodos() {
        return proyectos;
    }

    public interface ProyectosListener {
        void onProyectosActualizados(List<AdminProyecto> proyectos);
        void onError(String mensaje);
    }

    public interface GuardarCallback {
        void onSuccess(String proyectoId);
        void onError(String mensaje);
    }

    /**
     * Guarda el proyecto en Firestore usando el mismo ID del modelo como ID
     * de documento. También actualiza la lista local para que las pantallas de
     * detalle existentes puedan seguir usando getById().
     */
    public static void guardarEnFirestore(AdminProyecto proyecto, GuardarCallback callback) {
        if (proyecto == null || proyecto.getId().isEmpty()) {
            if (callback != null) callback.onError("Proyecto inválido");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection(COLECCION_PROYECTOS)
                .document(proyecto.getId())
                .set(proyecto)
                .addOnSuccessListener(unused -> {
                    agregarOReemplazarLocal(proyecto);
                    if (callback != null) callback.onSuccess(proyecto.getId());
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    /**
     * Escucha en tiempo real los proyectos creados por el administrador actual.
     * Para el login mock del curso se usa admin_mock.
     */
    public static ListenerRegistration escucharPorAdministrador(
            String adminUid,
            String inmobiliariaId,
            ProyectosListener listener
    ) {
        Query query = FirebaseFirestore.getInstance().collection(COLECCION_PROYECTOS);
        if (adminUid != null && !adminUid.trim().isEmpty()) {
            query = query.whereEqualTo("adminUid", adminUid);
        } else if (inmobiliariaId != null && !inmobiliariaId.trim().isEmpty()) {
            query = query.whereEqualTo("inmobiliariaId", inmobiliariaId);
        }

        return query.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                if (listener != null) listener.onError(error.getMessage());
                return;
            }

            List<AdminProyecto> lista = new ArrayList<>();
            if (snapshot != null) {
                for (QueryDocumentSnapshot doc : snapshot) {
                    AdminProyecto proyecto = doc.toObject(AdminProyecto.class);
                    proyecto.setId(doc.getId());
                    lista.add(proyecto);
                }
            }

            reemplazarListaLocal(lista);
            if (listener != null) listener.onProyectosActualizados(getTodos());
        });
    }

    public static String obtenerAdminUidActual() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : UID_ADMIN_MOCK;
    }

    public static String crearInmobiliariaId(String inmobiliariaNombre) {
        String base = inmobiliariaNombre != null && !inmobiliariaNombre.trim().isEmpty()
                ? inmobiliariaNombre.trim()
                : "inmobiliaria";
        String sinTildes = Normalizer.normalize(base, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String normalizado = sinTildes.toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return normalizado.isEmpty() ? "inmobiliaria" : normalizado;
    }

    /**
     * Busca un proyecto por su ID único.
     *
     * @param id ID del proyecto (UUID o "demo-xxx").
     * @return El proyecto si existe, o null si no se encontró.
     */
    public static AdminProyecto getById(String id) {
        if (id == null || id.isEmpty()) return null;
        for (AdminProyecto p : proyectos) {
            if (id.equals(p.getId())) return p;
        }
        return null;
    }

    /**
     * Reemplaza el proyecto con el mismo ID por la versión actualizada.
     */
    public static void actualizar(AdminProyecto actualizado) {
        if (actualizado == null || actualizado.getId() == null) return;
        for (int i = 0; i < proyectos.size(); i++) {
            if (actualizado.getId().equals(proyectos.get(i).getId())) {
                proyectos.set(i, actualizado);
                return;
            }
        }
    }

    private static void agregarOReemplazarLocal(AdminProyecto proyecto) {
        if (proyecto == null) return;
        for (int i = 0; i < proyectos.size(); i++) {
            if (proyecto.getId().equals(proyectos.get(i).getId())) {
                proyectos.set(i, proyecto);
                return;
            }
        }
        proyectos.add(0, proyecto);
    }

    private static void reemplazarListaLocal(List<AdminProyecto> nuevaLista) {
        proyectos.clear();
        if (nuevaLista != null) proyectos.addAll(nuevaLista);
    }

    /** Elimina todos los proyectos (útil para tests). */
    public static void limpiar() {
        proyectos.clear();
    }

    // ── Persistencia local (JSON) ────────────────────────────────────────────

    private static final String ARCHIVO_PROYECTOS = "admin_proyectos.json";

    /**
     * Serializa la lista a JSON y la escribe en getFilesDir().
     *
     * TODO-Firebase: reemplazar por un batch write a Firestore y subida
     * de imágenes a FirebaseStorage.
     */
    public static void guardar(Context ctx) {
        try {
            String json = new Gson().toJson(proyectos);
            File file = new File(ctx.getFilesDir(), ARCHIVO_PROYECTOS);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json);
            }
        } catch (Exception ignored) {
            // Lista en memoria sigue siendo válida aunque falle el guardado
        }
    }

    /**
     * Carga la lista desde el JSON guardado en disco.
     * Si el archivo no existe o está corrupto, conserva los datos demo en memoria.
     *
     * TODO-Firebase: reemplazar por una query a Firestore.
     */
    public static void cargar(Context ctx) {
        try {
            File file = new File(ctx.getFilesDir(), ARCHIVO_PROYECTOS);
            if (!file.exists()) return;
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }
            Type type = new TypeToken<List<AdminProyecto>>() {}.getType();
            List<AdminProyecto> guardados = new Gson().fromJson(sb.toString(), type);
            if (guardados != null && !guardados.isEmpty()) {
                proyectos.clear();
                proyectos.addAll(guardados);
            }
        } catch (Exception ignored) {
            // Si falla la carga, se conservan los datos demo iniciales
        }
    }

    // ── Datos demo iniciales ─────────────────────────────────────────────────

    /**
     * Crea 3 proyectos ficticios para que la lista no aparezca vacía en primera apertura.
     */
    private static List<AdminProyecto> crearDemoInicial() {
        List<AdminProyecto> demo = new ArrayList<>();

        // ── Proyecto 1: Edificio Los Álamos ──────────────────────────────────
        List<Tipologia> tipologias1 = new ArrayList<>();
        tipologias1.add(new Tipologia("Tipo A", 2, 1, 65.0,  280000.0, "Vista exterior",  ""));
        tipologias1.add(new Tipologia("Tipo B", 3, 2, 90.0,  380000.0, "Vista al jardín", ""));
        tipologias1.add(new Tipologia("Penthouse", 3, 3, 130.0, 680000.0, "Azotea privada", ""));
        demo.add(new AdminProyecto(
                "demo-001",
                "Edificio Los Álamos",
                "Proyecto residencial de 15 pisos con unidades de 2 y 3 dormitorios " +
                        "en la mejor zona de Miraflores.",
                "Av. Benavides 1520",
                "Miraflores",
                "5000",
                "280000",
                "Álamos Residencial",
                "S/ 320,000",
                "30/12/2026",
                "Preventa",
                tipologias1,
                Arrays.asList("Carlos Ruiz", "Ana Torres"),
                new ArrayList<>(),
                "01/01/2025"
        ));

        // ── Proyecto 2: Mirador de Surco ─────────────────────────────────────
        List<Tipologia> tipologias2 = new ArrayList<>();
        tipologias2.add(new Tipologia("Loft A", 1, 1, 45.0, 180000.0, "Diseño moderno", ""));
        tipologias2.add(new Tipologia("Flat B", 2, 1, 65.0, 240000.0, "Amplio y luminoso", ""));
        demo.add(new AdminProyecto(
                "demo-002",
                "Mirador de Surco",
                "Departamentos modernos con vista panorámica a todo Santiago de Surco.",
                "Calle Las Flores 230",
                "Santiago de Surco",
                "3000",
                "180000",
                "Surco Mirador",
                "S/ 195,000",
                "15/06/2026",
                "En planos",
                tipologias2,
                Arrays.asList("Luis Medina"),
                new ArrayList<>(),
                "15/02/2025"
        ));

        // ── Proyecto 3: Alto San Felipe ───────────────────────────────────────
        demo.add(new AdminProyecto(
                "demo-003",
                "Alto San Felipe",
                "Exclusivo condominio en zona residencial privilegiada con acceso directo " +
                        "a clubhouse y áreas verdes.",
                "Jr. San Felipe 890",
                "Jesús María",
                "8000",
                "450000",
                "San Felipe Premium",
                "S/ 480,000",
                "01/03/2027",
                "En venta",
                new ArrayList<>(),
                Arrays.asList("María Quispe", "Pedro Salinas", "Rosa Díaz"),
                new ArrayList<>(),
                "20/03/2025"
        ));

        return demo;
    }
}
