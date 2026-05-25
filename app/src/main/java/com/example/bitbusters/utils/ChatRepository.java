package com.example.bitbusters.utils;

import com.example.bitbusters.activities.asesor.MensajeAdapter;

import java.util.List;

/**
 * Abstracción del repositorio de mensajes de chat.
 *
 * Define el contrato que deben cumplir tanto la implementación de
 * desarrollo ({@link MockChatRepository}) como la de producción
 * ({@link FirestoreChatRepository} con Firebase Firestore).
 *
 * Este patrón Repository (Android Architecture Components / Clase 08.2)
 * desacopla la UI de la fuente de datos, permitiendo cambiar de mock a
 * Firestore real sin modificar ConversacionActivity.
 *
 * Estructura en Firestore:
 *   messages/
 *     {chatId}/
 *       mensajes/
 *         {autoId}/
 *           texto:     String
 *           hora:      String  ("10:30 AM")
 *           sent:      Boolean (true = asesor, false = cliente)
 *           timestamp: Timestamp  (ordenación)
 */
public interface ChatRepository {

    // ── Callbacks ─────────────────────────────────────────────────────────────

    interface MessagesCallback {
        /** Llamado cuando llegan nuevos mensajes (inicial o en tiempo real). */
        void onMessages(List<MensajeAdapter.Mensaje> mensajes);
        /** Llamado si ocurre un error al cargar. */
        void onError(String error);
    }

    interface SendCallback {
        void onSuccess();
        void onError(String error);
    }

    // ── Operaciones ───────────────────────────────────────────────────────────

    /**
     * Carga los mensajes de un chat y escucha actualizaciones en tiempo real.
     *
     * En {@link MockChatRepository}: devuelve datos estáticos inmediatamente.
     * En {@link FirestoreChatRepository}: registra un SnapshotListener que
     * invoca el callback cada vez que se agrega un nuevo mensaje.
     *
     * @param chatId   ID único del chat (ej. "1", "2", "new_8")
     * @param callback recibe la lista de mensajes
     */
    void loadMessages(String chatId, MessagesCallback callback);

    /**
     * Envía un mensaje del asesor.
     *
     * @param chatId   ID del chat donde enviar
     * @param text     Texto del mensaje
     * @param callback resultado de la operación
     */
    void sendMessage(String chatId, String text, SendCallback callback);

    /** Libera listeners activos (llamar en onDestroy). */
    void release();
}
