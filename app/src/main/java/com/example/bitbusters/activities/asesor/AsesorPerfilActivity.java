package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AsesorPerfilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_perfil);
        setupRecyclerView();
        setupBottomNav();
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rv_separaciones);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setNestedScrollingEnabled(false);
        rv.setAdapter(new SeparacionAdapter());
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_perfil);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, AsesorHomeActivity.class));
                finish();
            } else if (id == R.id.nav_citas) {
                startActivity(new Intent(this, CitasAgendadasActivity.class));
                finish();
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, MensajesActivity.class));
                finish();
            }
            return true;
        });
    }
}
