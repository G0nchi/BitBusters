package com.example.bitbusters.receivers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.bitbusters.utils.AsesorNotificationHelper;

/**
 * Worker de WorkManager que dispara el recordatorio de una cita confirmada.
 *
 * Reemplaza el enfoque de AlarmManager + BroadcastReceiver, que falla en
 * Android 8+ por las restricciones de ejecución en background (Clase 04.1).
 * WorkManager garantiza la ejecución incluso tras reinicio del dispositivo.
 */
public class CitaReminderWorker extends Worker {

    /** Nombre del cliente para mostrar en la notificación. */
    public static final String KEY_CLIENTE = "cliente";

    public CitaReminderWorker(@NonNull Context ctx, @NonNull WorkerParameters params) {
        super(ctx, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx     = getApplicationContext();
        String cliente  = getInputData().getString(KEY_CLIENTE);

        AsesorNotificationHelper.createChannel(ctx);
        AsesorNotificationHelper.showRecordatorioCita(ctx,
                cliente != null ? cliente : "un cliente");

        return Result.success();
    }
}
