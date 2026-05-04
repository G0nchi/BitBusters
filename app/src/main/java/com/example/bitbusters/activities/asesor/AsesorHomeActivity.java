package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.activities.access.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AsesorHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_home);
        setupRecyclerView();
        setupBottomNav();

        // Configurar dropdown de perfil para cerrar sesión
        View imgPerfil = findViewById(R.id.imgPerfilAsesor);
        if (imgPerfil != null) {
            imgPerfil.setOnClickListener(v -> showProfileMenu(v));
        }
    }

    private void showProfileMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("Cerrar Sesión");
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Cerrar Sesión")) {
                logout();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void logout() {
        Intent intent = new Intent(AsesorHomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rv_proyectos);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setNestedScrollingEnabled(false);
            ProyectoAdapter adapter = new ProyectoAdapter(position -> {
                Intent intent = new Intent(this, ProyectoDetalleActivity.class);
                intent.putExtra(ProyectoDetalleActivity.EXTRA_PROYECTO_INDEX, position);
                startActivity(intent);
            });
            recyclerView.setAdapter(adapter);
        }
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_inicio);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_citas) {
                    startActivity(new Intent(this, CitasAgendadasActivity.class));
                } else if (id == R.id.nav_chat) {
                    startActivity(new Intent(this, MensajesActivity.class));
                } else if (id == R.id.nav_perfil) {
                    startActivity(new Intent(this, AsesorPerfilActivity.class));
                }
                return true;
            });
        }
    }
}
