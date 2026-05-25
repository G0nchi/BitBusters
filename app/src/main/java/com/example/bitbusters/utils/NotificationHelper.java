package com.example.bitbusters.utils;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.bitbusters.data.AdminNotificacionesRepository;
import com.example.bitbusters.models.AdminNotificacion;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Helper centralizado para notificaciones locales (Lab 5).
 * Encapsula: creación de canales, solicitud de permiso y lanzamiento de notificaciones.
 *
 * Canales disponibles:
 *   - bitbusters_channel       → notificaciones del cliente (canal original)
 *   - bitbusters_admin_channel → notificaciones del administrador (Lab 5, Parte 2)
 */
public class NotificationHelper {

    // ── Canal del cliente (original — no modificar) ─────────────────────────
    public static final String CHANNEL_ID    = "bitbusters_channel";
    private static final String CHANNEL_NAME = "Notificaciones BitBusters";

    // ── Canal del administrador (Lab 5, Parte 2) ────────────────────────────
    /** ID del canal exclusivo para notificaciones del Administrador de Inmobiliaria. */
    public static final String ADMIN_CHANNEL_ID    = "bitbusters_admin_channel";
    private static final String ADMIN_CHANNEL_NAME = "Notificaciones Admin";

    // ── IDs de notificaciones del cliente (originales) ──────────────────────
    public static final int NOTIF_CITA_CONFIRMADA = 1;
    public static final int NOTIF_SEPARACION       = 2;
    public static final int NOTIF_METODO_PAGO      = 3;

    // ── IDs de notificaciones del administrador (Lab 5) ─────────────────────
    /** Se lanza al guardar exitosamente un proyecto en AdminCrearProyectoActivity. */
    public static final int NOTIF_ADMIN_PROYECTO_GUARDADO   = 10;
    /** Se lanza cuando un asesor registra una nueva separación (simulado). */
    public static final int NOTIF_ADMIN_NUEVA_SEPARACION    = 11;
    /** Se lanza cuando el admin aprueba el monto de una separación. */
    public static final int NOTIF_ADMIN_SEPARACION_APROBADA = 12;
    /** Se lanza cuando un cliente agenda una cita para un proyecto del admin (simulado). */
    public static final int NOTIF_ADMIN_NUEVA_CITA          = 13;

    // Código de solicitud para el diálogo de permiso (compartido por ambos roles)
    public static final int REQUEST_CODE_NOTIF = 101;

    // ── Creación de canales ─────────────────────────────────────────────────

    /**
     * Crea el canal de notificaciones del CLIENTE con IMPORTANCE_HIGH.
     * Solo tiene efecto en Android 8.0+ (API 26+); es seguro llamarlo varias veces.
     * Llamar desde onCreate() de cualquier Activity del cliente que use notificaciones.
     */
    public static void crearCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            canal.setDescription("Canal principal de alertas de BitBusters");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(canal);
            }
        }
    }

    /**
     * Crea el canal de notificaciones del ADMINISTRADOR con IMPORTANCE_HIGH.
     * Separado del canal del cliente para permitir configuración independiente.
     * Solo tiene efecto en Android 8.0+ (API 26+); es seguro llamarlo varias veces.
     * Llamar desde onCreate() de AdminMainActivity (punto de entrada del admin).
     */
    public static void crearCanalAdmin(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    ADMIN_CHANNEL_ID,
                    ADMIN_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            canal.setDescription("Canal de alertas del Administrador de Inmobiliaria");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(canal);
            }
        }
    }

    // ── Solicitud de permiso ────────────────────────────────────────────────

    /**
     * Solicita el permiso POST_NOTIFICATIONS en Android 13+ (API 33+).
     * Llamar desde onCreate() de la Activity principal de cada rol.
     */
    public static void solicitarPermiso(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_NOTIF
                );
            }
        }
    }

    // ── Lanzamiento de notificaciones ───────────────────────────────────────

    /**
     * Lanza una notificación local usando el canal del CLIENTE.
     *
     * @param context  Contexto de la Activity que lanza la notificación.
     * @param titulo   Título visible en la barra de notificaciones.
     * @param mensaje  Cuerpo del mensaje de la notificación.
     * @param notifId  ID único para identificar/actualizar esta notificación.
     * @param destino  Intent de la Activity a abrir al tocar la notificación.
     */
    public static void lanzarNotificacion(Context context, String titulo, String mensaje,
                                          int notifId, Intent destino) {
        // Verificar permiso en Android 13+ antes de lanzar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        // Agregar flags para abrir la Activity correctamente desde la notificación
        destino.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // PendingIntent con FLAG_IMMUTABLE (obligatorio desde Android 12)
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notifId,
                destino,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Construir la notificación siguiendo el patrón del curso
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(notifId, builder.build());
    }

    /**
     * Lanza una notificación local usando el canal del ADMINISTRADOR.
     * Misma firma que lanzarNotificacion() pero apunta a ADMIN_CHANNEL_ID.
     *
     * @param context  Contexto de la Activity del admin que lanza la notificación.
     * @param titulo   Título visible en la barra de notificaciones.
     * @param mensaje  Cuerpo del mensaje de la notificación.
     * @param notifId  ID único — usar las constantes NOTIF_ADMIN_* de esta clase.
     * @param destino  Intent de la Activity del admin a abrir al tocar la notificación.
     */
    public static void lanzarNotificacionAdmin(Context context, String titulo, String mensaje,
                                               int notifId, Intent destino) {
        // Verificar permiso en Android 13+ antes de lanzar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        // Flags para abrir la Activity correctamente desde la bandeja de notificaciones
        destino.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // PendingIntent con FLAG_IMMUTABLE (obligatorio desde Android 12)
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notifId,
                destino,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Construir la notificación usando el canal del administrador
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ADMIN_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje)) // texto largo visible
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(notifId, builder.build());

        // ── Parte 5: Registrar en el repositorio en memoria ─────────────────
        // Guarda la notificación para que AdminNotificacionesActivity la muestre
        String fechaHora = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date());
        AdminNotificacionesRepository.agregar(new AdminNotificacion(titulo, mensaje, fechaHora));
    }
}
