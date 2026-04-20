package com.example.bitbusters.activities.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.bitbusters.R;

public class MisCitasActivity extends AppCompatActivity {

    private TextView tabTodas, tabProximas, tabHistorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_citas);

        tabTodas    = findViewById(R.id.tabTodas);
        tabProximas = findViewById(R.id.tabProximas);
        tabHistorial= findViewById(R.id.tabHistorial);

        // Botón volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Tabs
        tabTodas.setOnClickListener(v -> seleccionarTab(0));
        tabProximas.setOnClickListener(v -> seleccionarTab(1));
        tabHistorial.setOnClickListener(v -> seleccionarTab(2));

        // Cita 1 - Confirmada: Reagendar / Cancelar
        findViewById(R.id.btnReagendar1).setOnClickListener(v -> {
            // TODO: abrir pantalla para reagendar cita
        });
        findViewById(R.id.btnCancelar1).setOnClickListener(v -> {
            // TODO: confirmar cancelación y actualizar en Firebase Lab 6
        });

        // Cita 3 - Completada: Valorar atención
        findViewById(R.id.btnValorar).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddCommentActivity.class);
            intent.putExtra("proyecto", "Condominio Los Pinos");
            startActivity(intent);
        });

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_favoritos);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_favoritos) return true;
            return false;
        });
    }

    private void seleccionarTab(int pos) {
        int azul = getResources().getColor(android.R.color.holo_blue_dark, getTheme());
        int gris = getResources().getColor(android.R.color.darker_gray, getTheme());

        tabTodas.setTextColor(pos == 0 ? azul : gris);
        tabProximas.setTextColor(pos == 1 ? azul : gris);
        tabHistorial.setTextColor(pos == 2 ? azul : gris);

        tabTodas.setBackgroundResource(pos == 0 ? R.drawable.bg_tab_selected_bottom : 0);
        tabProximas.setBackgroundResource(pos == 1 ? R.drawable.bg_tab_selected_bottom : 0);
        tabHistorial.setBackgroundResource(pos == 2 ? R.drawable.bg_tab_selected_bottom : 0);

        // TODO Lab 5: filtrar citas según tab seleccionado
    }
}