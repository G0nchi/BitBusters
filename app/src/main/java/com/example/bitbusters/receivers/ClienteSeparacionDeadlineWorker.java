package com.example.bitbusters.receivers;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.bitbusters.activities.cliente.HomeActivity;
import com.example.bitbusters.utils.NotificationHelper;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ClienteSeparacionDeadlineWorker extends Worker {

    private static final String MENSAJE_PREFIX_SEPARACION = "La separación de ";
    public static final String KEY_SEPARACION_ID = "separacionId";
    public static final String KEY_PROYECTO_NOMBRE = "proyectoNombre";

    public ClienteSeparacionDeadlineWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        String separacionId = getInputData().getString(KEY_SEPARACION_ID);
        String proyectoNombre = getInputData().getString(KEY_PROYECTO_NOMBRE);
        if (proyectoNombre == null || proyectoNombre.trim().isEmpty()) {
            proyectoNombre = "tu proyecto";
        }

        if (separacionId != null && !separacionId.trim().isEmpty()) {
            try {
                marcarSeparacionComoVencida(separacionId, proyectoNombre);
                crearNotificacionDeVencimiento(proyectoNombre);
            } catch (Exception e) {
                NotificationHelper.crearCanal(context);
                Intent destino = new Intent(context, HomeActivity.class);
                NotificationHelper.lanzarNotificacion(
                        context,
                        "Tu separación está por vencer",
                    MENSAJE_PREFIX_SEPARACION + proyectoNombre + " venció. Revisa tu método de pago o vuelve a registrarlo.",
                        NotificationHelper.NOTIF_SEPARACION,
                        destino
                );
                return Result.success();
            }
        }

        NotificationHelper.crearCanal(context);
        Intent destino = new Intent(context, HomeActivity.class);
        NotificationHelper.lanzarNotificacion(
                context,
                "Tu separación está por vencer",
            MENSAJE_PREFIX_SEPARACION + proyectoNombre + " venció. Revisa tu método de pago o vuelve a registrarlo.",
                NotificationHelper.NOTIF_SEPARACION,
                destino
        );
        return Result.success();
    }

    private void marcarSeparacionComoVencida(String separacionId, String proyectoNombre) throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> update = new HashMap<>();
        update.put("estado", "vencida");
        update.put("updatedAt", FieldValue.serverTimestamp());
        update.put("vencidaEn", System.currentTimeMillis());
        update.put(KEY_PROYECTO_NOMBRE, proyectoNombre);
        Tasks.await(db.collection("separaciones").document(separacionId).update(update), 10, TimeUnit.SECONDS);
    }

    private void crearNotificacionDeVencimiento(String proyectoNombre) throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("role", "cliente");
        data.put("senderName", "BitBusters");
        data.put("descripcion", MENSAJE_PREFIX_SEPARACION + proyectoNombre + " venció. Revisa tu tarjeta o vuelve a registrarla.");
        data.put("tiempo", "Ahora");
        data.put("avatarName", "avatar_jonathan");
        data.put("propertyName", "");
        data.put("isOld", false);
        data.put("order", System.currentTimeMillis());
        data.put("tipo", "separacion_vencida");
        data.put("createdAt", FieldValue.serverTimestamp());
        Tasks.await(db.collection("notifications").add(data), 10, TimeUnit.SECONDS);
    }
}
