package com.example.bitbusters.repository;

import android.util.Log;

import com.example.bitbusters.models.Chat;
import com.example.bitbusters.models.Mensaje;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRepository {

    private static final String TAG     = "ChatRepository";
    private static final String CHATS   = "chats";
    private static final String MENSAJES = "mensajes";

    private final FirebaseFirestore db;

    public ChatRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /** chatId determinístico: ordena los uids alfabéticamente para garantizar unicidad. */
    public static String generarChatId(String uidA, String uidB, String idProyecto) {
        String menor = uidA.compareTo(uidB) < 0 ? uidA : uidB;
        String mayor = uidA.compareTo(uidB) < 0 ? uidB : uidA;
        String proyectoSeguro = idProyecto.replaceAll("[^a-zA-Z0-9_-]", "_");
        return menor + "_" + mayor + "_" + proyectoSeguro;
    }

    /** Verifica si el chat existe; si no, lo crea. Devuelve el chatId via callback. */
    public void abrirOCrearChat(String uidCliente, String uidAsesor,
                                String idProyecto, String nombreProyecto,
                                String nombreCliente, String nombreAsesor, String fotoAsesor,
                                ChatCallback onSuccess, ErrorCallback onError) {
        String chatId = generarChatId(uidCliente, uidAsesor, idProyecto);
        DocumentReference ref = db.collection(CHATS).document(chatId);

        ref.get()
            .addOnSuccessListener(snap -> {
                if (snap.exists()) {
                    onSuccess.onResult(chatId);
                    return;
                }
                Map<String, Object> data = new HashMap<>();
                data.put("chatId", chatId);
                data.put("participantes", Arrays.asList(uidCliente, uidAsesor));
                data.put("nombreCliente", nombreCliente);
                data.put("nombreAsesor", nombreAsesor);
                data.put("fotoAsesor", fotoAsesor);
                data.put("idProyecto", idProyecto);
                data.put("nombreProyecto", nombreProyecto);
                data.put("ultimoMensaje", "");
                data.put("timestampUltimoMensaje", FieldValue.serverTimestamp());

                ref.set(data)
                    .addOnSuccessListener(v -> onSuccess.onResult(chatId))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error creando chat: " + e.getMessage());
                        onError.onError("No se pudo crear la conversación");
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error verificando chat: " + e.getMessage());
                onError.onError("Error al conectar con el chat: " + e.getMessage());
            });
    }

    /** Agrega el mensaje a la subcolección y actualiza ultimoMensaje en el documento padre. */
    public void enviarMensaje(String chatId, String idEmisor, String texto,
                              MensajeCallback onSuccess, ErrorCallback onError) {
        DocumentReference chatRef = db.collection(CHATS).document(chatId);

        Map<String, Object> mensajeData = new HashMap<>();
        mensajeData.put("idEmisor", idEmisor);
        mensajeData.put("texto", texto);
        mensajeData.put("timestamp", FieldValue.serverTimestamp());
        mensajeData.put("leido", false);

        chatRef.collection(MENSAJES).add(mensajeData)
            .addOnSuccessListener(ref -> {
                Map<String, Object> update = new HashMap<>();
                update.put("ultimoMensaje", texto);
                update.put("timestampUltimoMensaje", FieldValue.serverTimestamp());
                chatRef.update(update);
                onSuccess.onResult();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error enviando mensaje: " + e.getMessage());
                onError.onError("No se pudo enviar el mensaje");
            });
    }

    /** Listener en tiempo real de mensajes, ordenados ASC por timestamp. */
    public ListenerRegistration escucharMensajes(String chatId, MensajesListener listener) {
        return db.collection(CHATS).document(chatId).collection(MENSAJES)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener((snap, e) -> {
                if (e != null) {
                    Log.e(TAG, "Error escuchando mensajes: " + e.getMessage());
                    listener.onError("Error al cargar mensajes");
                    return;
                }
                if (snap != null) {
                    listener.onMensajes(snap.toObjects(Mensaje.class));
                }
            });
    }

    /**
     * Listener en tiempo real de todos los chats donde el usuario participa,
     * ordenados DESC por timestampUltimoMensaje.
     * NOTA: requiere índice compuesto en Firestore (participantes + timestampUltimoMensaje).
     * Si la app lanza "requires an index", usa el link del Logcat para crearlo.
     */
    public ListenerRegistration escucharChatsDelUsuario(String uid, ChatsListener listener) {
        return db.collection(CHATS)
            .whereArrayContains("participantes", uid)
            .orderBy("timestampUltimoMensaje", Query.Direction.DESCENDING)
            .addSnapshotListener((snap, e) -> {
                if (e != null) {
                    Log.e(TAG, "Error escuchando chats: " + e.getMessage());
                    listener.onError("Error al cargar conversaciones");
                    return;
                }
                if (snap != null) {
                    List<Chat> chats = snap.toObjects(Chat.class);
                    for (int i = 0; i < chats.size(); i++) {
                        chats.get(i).setChatId(snap.getDocuments().get(i).getId());
                    }
                    listener.onChats(chats);
                }
            });
    }

    // ── Interfaces ──────────────────────────────────────────────────────────────

    public interface ChatCallback {
        void onResult(String chatId);
    }

    public interface MensajeCallback {
        void onResult();
    }

    public interface MensajesListener {
        void onMensajes(List<Mensaje> mensajes);
        void onError(String mensaje);
    }

    public interface ChatsListener {
        void onChats(List<Chat> chats);
        void onError(String mensaje);
    }

    public interface ErrorCallback {
        void onError(String mensaje);
    }
}
