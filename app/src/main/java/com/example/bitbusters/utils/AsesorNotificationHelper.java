package com.example.bitbusters.utils;

import android.Manifest;
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

import com.example.bitbusters.R;
import com.example.bitbusters.activities.asesor.AsesorNotificacionesActivity;
import com.example.bitbusters.models.AsesorNotif;

import java.util.concurrent.atomic.AtomicInteger;

public class AsesorNotificationHelper {

    public static final String CHANNEL_ID   = "asesor_channel";
    private static final String CHANNEL_NAME = "Notificaciones Asesor";
    private static final AtomicInteger ID_SEQ = new AtomicInteger(2000);

    /** Crea el canal de notificaciones (requerido desde Android 8 / API 26). Idempotente. */
    public static void createChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            ch.setDescription("Citas, separaciones y mensajes del asesor");
            ctx.getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    // ── Eventos concretos ───────────────────────────────────────────────────────

    public static void showCitaConfirmada(Context ctx, String clienteNombre) {
        String titulo = "Cita confirmada";
        String desc   = clienteNombre + " confirmó su visita.";
        show(ctx, titulo, desc, R.drawable.ic_nav_calendar);
        AsesorStorage.addNotificacion(ctx, new AsesorNotif(titulo, desc, "Ahora", AsesorNotif.TIPO_CITA));
    }

    public static void showCitaCancelada(Context ctx, String clienteNombre) {
        String titulo = "Cita cancelada";
        String desc   = "La cita con " + clienteNombre + " fue cancelada.";
        show(ctx, titulo, desc, R.drawable.ic_nav_calendar);
        AsesorStorage.addNotificacion(ctx, new AsesorNotif(titulo, desc, "Ahora", AsesorNotif.TIPO_ALERTA));
    }

    public static void showCitaReagendada(Context ctx, String clienteNombre, String nuevaFecha) {
        String titulo = "Cita reagendada";
        String desc   = "La cita con " + clienteNombre + " se reagendó para el " + nuevaFecha + ".";
        show(ctx, titulo, desc, R.drawable.ic_nav_calendar);
        AsesorStorage.addNotificacion(ctx, new AsesorNotif(titulo, desc, "Ahora", AsesorNotif.TIPO_CITA));
    }

    public static void showNuevaSeparacion(Context ctx, String clienteNombre, String proyecto) {
        String titulo = "Nueva separación registrada";
        String desc   = clienteNombre + " registró una separación en " + proyecto + ".";
        show(ctx, titulo, desc, R.drawable.ic_star_filled);
        AsesorStorage.addNotificacion(ctx, new AsesorNotif(titulo, desc, "Ahora", AsesorNotif.TIPO_SEPARACION));
    }

    public static void showRecordatorioCitas(Context ctx, int numCitas) {
        String titulo = "Recordatorio de citas";
        String desc   = "Tienes " + numCitas + " cita" + (numCitas > 1 ? "s" : "")
                        + " programada" + (numCitas > 1 ? "s" : "") + " para hoy.";
        show(ctx, titulo, desc, R.drawable.ic_bell);
        AsesorStorage.addNotificacion(ctx, new AsesorNotif(titulo, desc, "Ahora", AsesorNotif.TIPO_CITA));
    }

    /** Recordatorio de cita individual — llamado por CitaReminderWorker (WorkManager). */
    public static void showRecordatorioCita(Context ctx, String clienteNombre) {
        String titulo = "Recordatorio de cita";
        String desc   = "En 30 min tienes una cita con " + clienteNombre + ".";
        show(ctx, titulo, desc, R.drawable.ic_bell);
        AsesorStorage.addNotificacion(ctx, new AsesorNotif(titulo, desc, "Ahora", AsesorNotif.TIPO_CITA));
    }

    // ── Core ────────────────────────────────────────────────────────────────────

    private static void show(Context ctx, String titulo, String mensaje, int iconRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Intent intent = new Intent(ctx, AsesorNotificacionesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(ctx, ID_SEQ.get(),
            intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pi)
            .setAutoCancel(true);

        NotificationManagerCompat.from(ctx).notify(ID_SEQ.getAndIncrement(), builder.build());
    }
}
