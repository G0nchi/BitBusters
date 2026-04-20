package com.example.bitbusters.activities.cliente;

import com.example.bitbusters.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;

public class MessagesActivity extends AppCompatActivity {

    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        tabLayout = findViewById(R.id.tabLayout);

        // Seleccionar el tab Mensajes por defecto (posición 1)
        TabLayout.Tab tabMensajes = tabLayout.getTabAt(1);
        if (tabMensajes != null) tabMensajes.select();

        // Botón volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Botón eliminar todos los chats
        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            // TODO: lógica para limpiar todos los chats
        });

        // Listener de tabs — al tocar Notificaciones vuelve a esa pantalla
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    startActivity(new Intent(MessagesActivity.this, NotificationsActivity.class));
                    finish();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Click en cada chat abre el detalle del chat
        findViewById(R.id.cardMilano).setOnClickListener(v -> abrirChat("Milano"));
        findViewById(R.id.cardSamuel).setOnClickListener(v -> abrirChat("Samuel Ella"));
        findViewById(R.id.cardSanta).setOnClickListener(v -> abrirChat("Santa Lcyua"));
        findViewById(R.id.cardSandra).setOnClickListener(v -> abrirChat("Sandra Sotomayor"));
        findViewById(R.id.cardValerai).setOnClickListener(v -> abrirChat("Valerai CW"));

        // Botón eliminar del chat Santa (swipe state visible)
        findViewById(R.id.btnDeleteChat).setOnClickListener(v -> eliminarChat("Santa Lcyua"));
    }

    private void abrirChat(String nombreContacto) {
        Intent intent = new Intent(this, ChatDetailActivity.class);
        intent.putExtra("contacto", nombreContacto);
        startActivity(intent);
    }

    private void eliminarChat(String nombreContacto) {
        // TODO: eliminar chat de Firebase en Lab 6
        // Por ahora ocultar la card visualmente
        findViewById(R.id.cardSanta).setVisibility(View.GONE);
    }
}