package com.example.bitbusters.services;

import android.content.Intent;
import android.util.Log;

import com.example.bitbusters.activities.asesor.AsesorNotificacionesActivity;
import com.example.bitbusters.utils.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Servicio FCM para BitBusters.
 * - onNewToken: guarda el token del dispositivo en Firestore (users/{uid}.fcmToken)
 * - onMessageReceived: muestra notificación local cuando llega un push en background
 */
public class BitBustersMessagingService extends FirebaseMessagingService {

    private static final String TAG = "BitBusters_FCM";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM token renovado: " + token);
        saveTokenToFirestore(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        String title = "BitBusters";
        String body  = "";

        if (message.getNotification() != null) {
            title = message.getNotification().getTitle() != null
                    ? message.getNotification().getTitle() : title;
            body  = message.getNotification().getBody() != null
                    ? message.getNotification().getBody() : body;
        } else if (!message.getData().isEmpty()) {
            title = message.getData().getOrDefault("title", title);
            body  = message.getData().getOrDefault("body", body);
        }

        NotificationHelper.crearCanal(this);
        Intent destino = new Intent(this, AsesorNotificacionesActivity.class);
        NotificationHelper.lanzarNotificacion(
                this, title, body,
                (int) System.currentTimeMillis(),
                destino
        );
    }

    private void saveTokenToFirestore(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .update("fcmToken", token)
                .addOnFailureListener(e ->
                        Log.w(TAG, "Error al guardar FCM token: " + e.getMessage()));
    }
}
