package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AsesorHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_home);
        setupRecyclerView();
        setupBottomNav();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rv_proyectos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        ProyectoAdapter adapter = new ProyectoAdapter(position -> {
            Intent intent = new Intent(this, ProyectoDetalleActivity.class);
            intent.putExtra(ProyectoDetalleActivity.EXTRA_PROYECTO_INDEX, position);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_inicio);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_citas) {
                startActivity(new Intent(this, CitasAgendadasActivity.class));
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, MensajesActivity.class));
            }
            return true;
        });
    }
}
