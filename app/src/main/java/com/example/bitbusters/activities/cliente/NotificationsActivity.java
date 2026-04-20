package com.example.bitbusters.activities.cliente;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;
import com.example.bitbusters.R;

public class NotificationsActivity extends AppCompatActivity {

    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        tabLayout = findViewById(R.id.tabLayout);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            // TODO: lógica para limpiar notificaciones
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    mostrarNotificaciones();
                } else {
                    mostrarMensajes();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Click en cards
        findViewById(R.id.cardJoseDaniel).setOnClickListener(v -> abrirDetalle("José Daniel"));
        findViewById(R.id.cardGerardo).setOnClickListener(v -> abrirDetalle("Gerardo"));
        findViewById(R.id.cardWendy).setOnClickListener(v -> abrirDetalle("Wendy Cuzca"));
        findViewById(R.id.cardVelma).setOnClickListener(v -> abrirDetalle("Velma Cole"));
    }

    private void mostrarNotificaciones() {
        // Por ahora las cards ya están en el layout (data estática)
        // En Lab 6 se reemplaza con Firebase Database
    }

    private void mostrarMensajes() {
        // TODO: implementar en Lab 5 con Firebase
    }

    private void abrirDetalle(String nombre) {
        // TODO: navegar al detalle de la notificación
        // Intent intent = new Intent(this, NotificationDetailActivity.class);
        // intent.putExtra("nombre", nombre);
        // startActivity(intent);
    }
}