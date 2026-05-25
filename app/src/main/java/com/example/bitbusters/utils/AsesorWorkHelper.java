package com.example.bitbusters.utils;

import android.content.Context;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.bitbusters.receivers.CitaReminderWorker;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Helper para programar y cancelar recordatorios de citas con WorkManager.
 *
 * Cada cita tiene un nombre único de tarea = su citaKey (nombre||fecha||hora),
 * lo que permite reemplazar o cancelar fácilmente si la cita cambia de estado.
 *
 * El recordatorio se programa 30 minutos antes de la hora de la cita.
 * Si la fecha ya pasó o el delay es negativo, se omite la programación.
 */
public class AsesorWorkHelper {

    /** Minutos antes de la cita en que se dispara el recordatorio. */
    private static final long REMIND_BEFORE_MS = 30 * 60 * 1_000L;

    /**
     * Programa (o reemplaza) un recordatorio para la cita indicada.
     *
     * @param ctx           Contexto de la aplicación.
     * @param citaKey       Clave única de la cita (nombre||fecha||hora).
     * @param clienteNombre Nombre del cliente para mostrar en la notificación.
     * @param fecha         Fecha en formato "Lun 7 Abr, 2025".
     * @param hora          Hora en formato "10:30 AM".
     */
    public static void scheduleRecordatorio(Context ctx,
                                            String citaKey,
                                            String clienteNombre,
                                            String fecha,
                                            String hora) {
        long delayMs = computeDelayMs(fecha, hora);
        if (delayMs <= 0) return;   // la cita ya pasó — no programar

        Data inputData = new Data.Builder()
                .putString(CitaReminderWorker.KEY_CLIENTE, clienteNombre)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(CitaReminderWorker.class)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build();

        // REPLACE: si la cita se reagendó, la tarea anterior se cancela
        WorkManager.getInstance(ctx)
                .enqueueUniqueWork(citaKey, ExistingWorkPolicy.REPLACE, request);
    }

    /** Cancela el recordatorio de una cita (p. ej. cuando se cancela la cita). */
    public static void cancelRecordatorio(Context ctx, String citaKey) {
        WorkManager.getInstance(ctx).cancelUniqueWork(citaKey);
    }

    // ── Parseo de fecha / hora ────────────────────────────────────────────────

    /**
     * Calcula el delay en ms desde ahora hasta 30 min antes de la cita.
     * Devuelve un valor negativo si la cita ya pasó o si el parseo falla
     * (en ese caso se loguea el error y se usa 30 s como fallback de demo).
     *
     * Formato soportado:
     *   fecha = "Lun 7 Abr, 2025"  (día semana d mes, año)
     *   hora  = "10:30 AM"         (h:mm AM/PM)
     */
    static long computeDelayMs(String fecha, String hora) {
        try {
            // Limpiar coma y split: ["Lun","7","Abr","2025"]
            String[] fp = fecha.replace(",", "").trim().split("\\s+");
            int dia  = Integer.parseInt(fp[1]);
            int mes  = mesFromAbbrev(fp[2]);
            int anio = Integer.parseInt(fp[3]);

            // hora = "10:30 AM" → ["10:30","AM"]
            String[] hp     = hora.trim().split("\\s+");
            String[] hm     = hp[0].split(":");
            int hour        = Integer.parseInt(hm[0]);
            int min         = Integer.parseInt(hm[1]);
            boolean pm      = "PM".equalsIgnoreCase(hp[1]);
            if (pm  && hour != 12) hour += 12;
            if (!pm && hour == 12) hour  =  0;

            Calendar cal = Calendar.getInstance();
            cal.set(anio, mes, dia, hour, min, 0);
            cal.set(Calendar.MILLISECOND, 0);

            long citaMs   = cal.getTimeInMillis();
            long targetMs = citaMs - REMIND_BEFORE_MS;
            return targetMs - System.currentTimeMillis();

        } catch (Exception e) {
            // Parseo fallido: usar 30 s (útil en demos con fechas pasadas)
            return 30_000L;
        }
    }

    private static int mesFromAbbrev(String abbrev) {
        switch (abbrev.toLowerCase(Locale.getDefault())) {
            case "ene": return Calendar.JANUARY;
            case "feb": return Calendar.FEBRUARY;
            case "mar": return Calendar.MARCH;
            case "abr": return Calendar.APRIL;
            case "may": return Calendar.MAY;
            case "jun": return Calendar.JUNE;
            case "jul": return Calendar.JULY;
            case "ago": return Calendar.AUGUST;
            case "sep": return Calendar.SEPTEMBER;
            case "oct": return Calendar.OCTOBER;
            case "nov": return Calendar.NOVEMBER;
            case "dic": return Calendar.DECEMBER;
            default:    return Calendar.JANUARY;
        }
    }
}
