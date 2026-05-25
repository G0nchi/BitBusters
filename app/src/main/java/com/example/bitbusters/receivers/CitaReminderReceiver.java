package com.example.bitbusters.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.bitbusters.models.AsesorCita;
import com.example.bitbusters.utils.AsesorNotificationHelper;
import com.example.bitbusters.utils.AsesorStorage;
import com.example.bitbusters.utils.AsesorWorkHelper;

import java.util.List;

/**
 * BroadcastReceiver de compatibilidad.
 *
 * - ACTION_CITA_REMINDER: mantiene compatibilidad con cualquier alarma legacy.
 * - BOOT_COMPLETED: re-programa en WorkManager todos los recordatorios de
 *   citas confirmadas que aún no hayan pasado (WorkManager persiste sus tareas
 *   internamente, pero reprogramamos por si acaso el scheduler se perdió).
 */
public class CitaReminderReceiver extends BroadcastReceiver {

    public static final String ACTION_CITA_REMINDER = "com.example.bitbusters.ACTION_CITA_REMINDER";
    public static final String EXTRA_NUM_CITAS      = "extra_num_citas";
    public static final String EXTRA_CLIENTE        = "extra_cliente";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();

        if (ACTION_CITA_REMINDER.equals(action)) {
            // Compatibilidad legacy: disparar notificación directamente
            AsesorNotificationHelper.createChannel(ctx);
            String cliente  = intent.getStringExtra(EXTRA_CLIENTE);
            int    numCitas = intent.getIntExtra(EXTRA_NUM_CITAS, 1);
            if (cliente != null) {
                AsesorNotificationHelper.showRecordatorioCita(ctx, cliente);
            } else {
                AsesorNotificationHelper.showRecordatorioCitas(ctx, numCitas);
            }

        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // Re-programar recordatorios de citas confirmadas tras reinicio
            List<AsesorCita> confirmadas = AsesorStorage.getConfirmedCitas(ctx);
            for (AsesorCita cita : confirmadas) {
                String key = AsesorStorage.buildCitaKey(cita.nombre, cita.fecha, cita.hora);
                AsesorWorkHelper.scheduleRecordatorio(
                        ctx, key, cita.nombre, cita.fecha, cita.hora);
            }
        }
    }
}
