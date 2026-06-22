package com.example.bitbusters.utils;

import com.example.bitbusters.models.ClientMessage;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Repositorio Firestore para el chat desde el lado del Cliente.
 *
 * Estructura Firestore:
 *   chats/{chatId}/mensajes/{autoId}
 *     texto:     String
 *     hora:      String
 *     esCliente: Boolean  (true = mensaje del cliente)
 *     timestamp: Timestamp
 *
 * chatId = "{clienteUid}_{asesorId}"
 *   ej.: "uid_abc123_asesor_ana_001"
 */
public class ClienteChatRepository {

    public interface MessagesCallback {
        void onMessages(List<ClientMessage> mensajes);
        void onError(String error);
    }

    public interface SendCallback {
        void onSuccess();
        void onError(String error);
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration listenerReg;

    public void loadMessages(String chatId, MessagesCallback callback) {
        if (chatId == null || chatId.isEmpty()) {
            callback.onError("chatId inválido");
            return;
        }

        listenerReg = db.collection("chats")
            .document(chatId)
            .collection("mensajes")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    callback.onError(error.getMessage());
                    return;
                }
                if (snapshots == null) return;

                List<ClientMessage> msgs = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    String texto = doc.getString("texto");
                    String hora  = doc.getString("hora");
                    Boolean esCliente = doc.getBoolean("esCliente");
                    if (texto != null) {
                        msgs.add(new ClientMessage(
                            texto,
                            hora != null ? hora : "",
                            Boolean.TRUE.equals(esCliente)  // sent=true → mensaje propio del cliente
                        ));
                    }
                }
                callback.onMessages(msgs);
            });
    }

    public void sendMessage(String chatId, String text, SendCallback callback) {
        String hora = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());

        Map<String, Object> data = new HashMap<>();
        data.put("texto",     text);
        data.put("hora",      hora);
        data.put("esCliente", true);   // cliente envía
        data.put("timestamp", FieldValue.serverTimestamp());

        db.collection("chats")
            .document(chatId)
            .collection("mensajes")
            .add(data)
            .addOnSuccessListener(ref -> callback.onSuccess())
            .addOnFailureListener(e  -> callback.onError(e.getMessage()));
    }

    public void release() {
        if (listenerReg != null) listenerReg.remove();
    }
}
