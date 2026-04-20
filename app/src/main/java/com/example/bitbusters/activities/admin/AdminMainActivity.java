package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        setupBottomNavigation(R.id.nav_dashboard);
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
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, AdminPerfilActivity.class));
                finish();
            }
            return true;
        });
    }
}
