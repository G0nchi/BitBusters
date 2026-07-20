package com.example.bitbusters.models;

import java.util.UUID;

/**
 * Modelo de datos para las notificaciones del Administrador (Parte 5 — Lab 5).
 * Se almacena en memoria via AdminNotificacionesRepository cada vez que
 * NotificationHelper.lanzarNotificacionAdmin() es invocado.
 *
 * Campos:
 *   id        → UUID único generado automáticamente
 *   titulo    → Título mostrado en la barra de notificaciones
 *   mensaje   → Cuerpo del mensaje de la notificación
 *   timestamp → Fecha y hora de creación ("dd/MM/yyyy HH:mm")
 */
public class AdminNotificacion {

    /** Identificador único de esta notificación (UUID). */
    private final String id;

    /** Título de la notificación (visible en la barra de estado). */
    private final String titulo;

    /** Cuerpo del mensaje de la notificación. */
    private final String mensaje;

    /** Fecha y hora en que se lanzó la notificación (formato "dd/MM/yyyy HH:mm"). */
    private final String timestamp;

    /** Indica si viene de Firestore y puede marcarse como leída. */
    private final boolean remota;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Crea una nueva AdminNotificacion con ID generado automáticamente.
     *
     * @param titulo    Título de la notificación.
     * @param mensaje   Mensaje/cuerpo de la notificación.
     * @param timestamp Fecha y hora de creación en formato "dd/MM/yyyy HH:mm".
     */
    public AdminNotificacion(String titulo, String mensaje, String timestamp) {
        this.id        = UUID.randomUUID().toString();
        this.titulo    = titulo;
        this.mensaje   = mensaje;
        this.timestamp = timestamp;
        this.remota    = false;
    }

    /**
     * Crea una notificación usando un ID externo, por ejemplo el ID del documento
     * en Firestore. Permite actualizar campos como read en la fuente remota.
     */
    public AdminNotificacion(String id, String titulo, String mensaje, String timestamp, boolean remota) {
        this.id        = id != null && !id.trim().isEmpty() ? id : UUID.randomUUID().toString();
        this.titulo    = titulo;
        this.mensaje   = mensaje;
        this.timestamp = timestamp;
        this.remota    = remota;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    /** Retorna el ID único de la notificación. */
    public String getId() { return id; }

    /** Retorna el título de la notificación. */
    public String getTitulo() { return titulo; }

    /** Retorna el mensaje/cuerpo de la notificación. */
    public String getMensaje() { return mensaje; }

    /** Retorna el timestamp de creación de la notificación. */
    public String getTimestamp() { return timestamp; }

    /** Retorna true si la notificación corresponde a un documento remoto. */
    public boolean isRemota() { return remota; }
}
