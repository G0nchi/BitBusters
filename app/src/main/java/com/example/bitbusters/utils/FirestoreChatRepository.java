package com.example.bitbusters.utils;

import com.example.bitbusters.activities.asesor.MensajeAdapter;

import java.util.List;

/**
 * Implementación de producción de {@link ChatRepository} usando Firebase Firestore.
 *
 * ══════════════════════════════════════════════════════════════════════
 * PARA ACTIVAR FIREBASE (Clase 08.2):
 *
 * 1. En Firebase Console (console.firebase.google.com):
 *    - Crear proyecto → Agregar app Android → descargar google-services.json
 *    - Colocar google-services.json en app/
 *    - Habilitar Firestore Database en modo test
 *
 * 2. En build.gradle (Project):
 *    plugins { id 'com.google.gms.google-services' version '4.4.2' apply false }
 *
 * 3. En app/build.gradle:
 *    plugins { id 'com.google.gms.google-services' }
 *    implementation platform('com.google.firebase:firebase-bom:33.1.0')
 *    implementation 'com.google.firebase:firebase-firestore'
 *
 * 4. Reemplazar en ConversacionActivity:
 *    ChatRepository repo = new MockChatRepository();
 *    →  ChatRepository repo = new FirestoreChatRepository();
 * ══════════════════════════════════════════════════════════════════════
 *
 * Estructura Firestore:
 *   messages/
 *     {chatId}/
 *       mensajes/   ← subcolección
 *         {autoId}/
 *           texto:     String
 *           hora:      String
 *           sent:      Boolean
 *           timestamp: Timestamp
 */
public class FirestoreChatRepository implements ChatRepository {

    // ── Firebase setup (requiere google-services.json) ──────────────────────
    // private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // private ListenerRegistration listenerReg;

    @Override
    public void loadMessages(String chatId, MessagesCallback callback) {
        // ── Real-time listener Firestore ──────────────────────────────────
        // listenerReg = db.collection("messages")
        //     .document(chatId)
        //     .collection("mensajes")
        //     .orderBy("timestamp", Query.Direction.ASCENDING)
        //     .addSnapshotListener((snapshots, error) -> {
        //         if (error != null) {
        //             callback.onError(error.getMessage());
        //             return;
        //         }
        //         if (snapshots == null) return;
        //
        //         List<MensajeAdapter.Mensaje> msgs = new ArrayList<>();
        //         for (DocumentSnapshot doc : snapshots.getDocuments()) {
        //             String texto = doc.getString("texto");
        //             String hora  = doc.getString("hora");
        //             Boolean sent = doc.getBoolean("sent");
        //             if (texto != null) {
        //                 msgs.add(new MensajeAdapter.Mensaje(
        //                     texto,
        //                     hora != null ? hora : "",
        //                     Boolean.TRUE.equals(sent)
        //                 ));
        //             }
        //         }
        //         callback.onMessages(msgs);   // llamado cada vez que cambia la BD
        //     });

        // Fallback hasta activar Firebase:
        callback.onError("FirestoreChatRepository: agrega google-services.json para activar.");
    }

    @Override
    public void sendMessage(String chatId, String text, SendCallback callback) {
        // ── Escritura en Firestore ────────────────────────────────────────
        // String hora = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
        //
        // Map<String, Object> data = new HashMap<>();
        // data.put("texto",     text);
        // data.put("hora",      hora);
        // data.put("sent",      true);                      // asesor = true
        // data.put("timestamp", FieldValue.serverTimestamp());
        //
        // db.collection("messages")
        //     .document(chatId)
        //     .collection("mensajes")
        //     .add(data)
        //     .addOnSuccessListener(ref -> callback.onSuccess())
        //     .addOnFailureListener(e  -> callback.onError(e.getMessage()));

        callback.onError("Activa Firebase para enviar mensajes en tiempo real.");
    }

    @Override
    public void release() {
        // if (listenerReg != null) listenerReg.remove();
    }
}
