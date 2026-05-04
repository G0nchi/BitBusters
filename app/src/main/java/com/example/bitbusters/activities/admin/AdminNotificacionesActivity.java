package com.example.bitbusters.activities.admin;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.AdminNotificationsAdapter;
import com.example.bitbusters.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class AdminNotificacionesActivity extends AppCompatActivity {

    private RecyclerView rvNotificaciones;
    private AdminNotificationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notificaciones);
        
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        initializeRecyclerView();
    }

    private void initializeRecyclerView() {
        rvNotificaciones = findViewById(R.id.rvNotificaciones);
        if (rvNotificaciones != null) {
            rvNotificaciones.setLayoutManager(new LinearLayoutManager(this));
            adapter = new AdminNotificationsAdapter(getNotificationsList(), notification -> {
                // Handle notification click if needed
            });
            rvNotificaciones.setAdapter(adapter);
        }
    }

    private List<Notification> getNotificationsList() {
        List<Notification> notifications = new ArrayList<>();
        notifications.add(new Notification("María García", "Pagó S/ 5,000", "hace 2h"));
        notifications.add(new Notification("Carlos Ruiz", "Nueva separación registrada", "hace 4h"));
        notifications.add(new Notification("Inmobiliaria XYZ", "Reporte mensual disponible", "hace 1d"));
        notifications.add(new Notification("Ana Torres", "Proyecto actualizado", "hace 2d"));
        notifications.add(new Notification("Sistema", "Revisión de perfiles completada", "hace 3d"));
        notifications.add(new Notification("Roberto Silva", "Asesor asignado al proyecto", "hace 4d"));
        notifications.add(new Notification("Patricia Mendez", "Cambio de estado aprobado", "hace 5d"));
        notifications.add(new Notification("Lucia Vargas", "Nueva cita agendada", "hace 6d"));
        notifications.add(new Notification("Diego Castillo", "Documento compartido", "hace 1w"));
        notifications.add(new Notification("Valeria Romero", "Invitación aceptada", "hace 1w"));
        notifications.add(new Notification("Gustavo Morales", "Informe de actividad", "hace 2w"));
        notifications.add(new Notification("Sofía Delgado", "Proyecto completado", "hace 2w"));
        notifications.add(new Notification("Javier Campos", "Reunión programada", "hace 3w"));
        notifications.add(new Notification("Isabel Herrera", "Documento vencido", "hace 3w"));
        notifications.add(new Notification("Miguel Ángel Soto", "Cambio de contraseña", "hace 1m"));
        return notifications;
    }
}
