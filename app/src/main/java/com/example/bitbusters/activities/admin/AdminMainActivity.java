package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        setupBottomNavigation(R.id.nav_dashboard);
        setupQuickActionListeners();
        setupHeaderListeners();
    }

    protected void setupHeaderListeners() {
        ImageButton btnNotifications = findViewById(R.id.btnNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> {
                startActivity(new Intent(AdminMainActivity.this, AdminNotificacionesActivity.class));
            });
        }

        TextView tvAvatar = findViewById(R.id.tvAvatar);
        if (tvAvatar != null) {
            tvAvatar.setOnClickListener(v -> {
                startActivity(new Intent(AdminMainActivity.this, AdminPerfilActivity.class));
            });
        }

        TextView tvRealEstateName = findViewById(R.id.tvRealEstateName);
        if (tvRealEstateName != null) {
            tvRealEstateName.setOnClickListener(v -> {
                startActivity(new Intent(AdminMainActivity.this, AdminDetallesInmobiliariaActivity.class));
            });
        }
    }

    private void setupQuickActionListeners() {
        // Crear proyecto card
        MaterialCardView cardCreateProject = findViewById(R.id.cardCreateProject);
        if (cardCreateProject != null) {
            cardCreateProject.setOnClickListener(v -> {
                startActivity(new Intent(AdminMainActivity.this, AdminCrearProyectoActivity.class));
            });
        }

        // Ver separaciones card
        MaterialCardView cardViewSeparations = findViewById(R.id.cardViewSeparations);
        if (cardViewSeparations != null) {
            cardViewSeparations.setOnClickListener(v -> {
                startActivity(new Intent(AdminMainActivity.this, AdminSeparacionesActivity.class));
            });
        }

        // Ver reportes card
        MaterialCardView cardViewReports = findViewById(R.id.cardViewReports);
        if (cardViewReports != null) {
            cardViewReports.setOnClickListener(v -> {
                startActivity(new Intent(AdminMainActivity.this, AdminReportesActivity.class));
            });
        }

        // Asignar asesor card - goes to projects list
        MaterialCardView cardAssignAdvisor = findViewById(R.id.cardAssignAdvisor);
        if (cardAssignAdvisor != null) {
            cardAssignAdvisor.setOnClickListener(v -> {
                startActivity(new Intent(AdminMainActivity.this, AdminProyectosActivity.class));
            });
        }
    }

    protected void setupBottomNavigation(int selectedItemId) {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(selectedItemId);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                if (!(this instanceof AdminMainActivity) || selectedItemId != R.id.nav_dashboard) {
                    startActivity(new Intent(this, AdminMainActivity.class));
                    finish();
                }
            } else if (id == R.id.nav_proyectos) {
                startActivity(new Intent(this, AdminProyectosActivity.class));
                finish();
            } else if (id == R.id.nav_separaciones) {
                startActivity(new Intent(this, AdminSeparacionesActivity.class));
                finish();
            } else if (id == R.id.nav_reportes) {
                startActivity(new Intent(this, AdminReportesActivity.class));
                finish();
            }
            return true;
        });
    }
}
