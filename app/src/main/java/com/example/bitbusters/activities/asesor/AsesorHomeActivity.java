package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.PopupMenu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.activities.access.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class AsesorHomeActivity extends AppCompatActivity {

    private ProyectoAdapter proyectoAdapter;
    private MaterialButton chipTodos, chipDepartamentos, chipVillas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_home);
        setupRecyclerView();
        setupChips();
        setupQuickActions();
        setupBottomNav();

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
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rv_proyectos);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setNestedScrollingEnabled(false);
            proyectoAdapter = new ProyectoAdapter(position -> {
                Intent intent = new Intent(this, ProyectoDetalleActivity.class);
                intent.putExtra(ProyectoDetalleActivity.EXTRA_PROYECTO_INDEX, position);
                startActivity(intent);
            });
            recyclerView.setAdapter(proyectoAdapter);
        }
    }

    private void setupChips() {
        chipTodos = findViewById(R.id.chip_todos);
        chipDepartamentos = findViewById(R.id.chip_departamentos);
        chipVillas = findViewById(R.id.chip_villas);
        if (chipTodos == null) return;
        chipTodos.setOnClickListener(v -> activateChip("Todos"));
        chipDepartamentos.setOnClickListener(v -> activateChip("Departamento"));
        chipVillas.setOnClickListener(v -> activateChip("Villa"));
    }

    private void activateChip(String tipo) {
        int activeColor = getColor(R.color.neutral_dark);
        int inactiveColor = android.graphics.Color.TRANSPARENT;
        int activeText = android.graphics.Color.WHITE;
        int inactiveText = getColor(R.color.text_secondary);

        boolean isTodos = "Todos".equals(tipo);
        boolean isDept = "Departamento".equals(tipo);
        boolean isVilla = "Villa".equals(tipo);

        chipTodos.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isTodos ? activeColor : inactiveColor));
        chipTodos.setTextColor(isTodos ? activeText : inactiveText);
        chipDepartamentos.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isDept ? activeColor : inactiveColor));
        chipDepartamentos.setTextColor(isDept ? activeText : inactiveText);
        chipVillas.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isVilla ? activeColor : inactiveColor));
        chipVillas.setTextColor(isVilla ? activeText : inactiveText);

        if (proyectoAdapter != null) proyectoAdapter.applyFilter(tipo);
    }

    private void setupQuickActions() {
        View cardMapa = findViewById(R.id.card_mapa);
        if (cardMapa != null) {
            cardMapa.setOnClickListener(v ->
                startActivity(new Intent(this, AsesorMapaActivity.class)));
        }

        View cardOfertas = findViewById(R.id.card_ofertas);
        if (cardOfertas != null) {
            cardOfertas.setOnClickListener(v ->
                startActivity(new Intent(this, AsesorOfertasActivity.class)));
        }

        View imgCampana = findViewById(R.id.img_campana);
        if (imgCampana != null) {
            imgCampana.setOnClickListener(v ->
                startActivity(new Intent(this, AsesorNotificacionesActivity.class)));
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
