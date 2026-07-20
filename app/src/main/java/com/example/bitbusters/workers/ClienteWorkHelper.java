package com.example.bitbusters.workers;

import android.content.Context;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.bitbusters.receivers.ClienteSeparacionDeadlineWorker;

import java.util.concurrent.TimeUnit;

public final class ClienteWorkHelper {

    private ClienteWorkHelper() {}

    public static void scheduleSeparacionDeadline(Context context,
                                                  String workKey,
                                                  String separacionId,
                                                  String proyectoNombre) {
        Data inputData = new Data.Builder()
                .putString(ClienteSeparacionDeadlineWorker.KEY_SEPARACION_ID, separacionId)
                .putString(ClienteSeparacionDeadlineWorker.KEY_PROYECTO_NOMBRE, proyectoNombre)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ClienteSeparacionDeadlineWorker.class)
                .setInitialDelay(10, TimeUnit.MINUTES)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context)
                .enqueueUniqueWork(workKey, ExistingWorkPolicy.REPLACE, request);
    }
}
