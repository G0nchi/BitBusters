package com.example.bitbusters.activities.asesor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import com.example.bitbusters.R;
import com.example.bitbusters.databinding.ActivityCitasAgendadasBinding;
import com.example.bitbusters.utils.AsesorNotificationHelper;

/**
 * Activity anfitriona del módulo de citas.
 *
 * Delega toda la UI de tabs + detalle al grafo de Navigation Component
 * (nav_citas.xml) mediante un NavHostFragment declarado en el layout.
 * La Activity sólo gestiona permisos, botón atrás y bottom nav.
 */
public class CitasAgendadasActivity extends AppCompatActivity {

    private ActivityCitasAgendadasBinding binding;
    private NavController navController;

    private static final int REQ_NOTIF_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCitasAgendadasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AsesorNotificationHelper.createChannel(this);
        requestNotifPermission();

        // NavController desde el NavHostFragment declarado en el layout
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_citas);
        if (navHost != null) {
            navController = navHost.getNavController();

            // Ocultar el header de la Activity cuando se muestra VerDetalleCitaFragment
            // (ese fragment ya tiene su propio header "Detalle de Cita").
            navController.addOnDestinationChangedListener(
                    (NavController controller, NavDestination destination, Bundle args) -> {
                        if (destination.getId() == R.id.verDetalleCitaFragment) {
                            binding.layoutHeaderCitas.setVisibility(View.GONE);
                            binding.bottomNav.setVisibility(View.GONE);
                        } else {
                            binding.layoutHeaderCitas.setVisibility(View.VISIBLE);
                            binding.bottomNav.setVisibility(View.VISIBLE);
                        }
                    });
        }

        // Botón atrás: NavController maneja el back stack (tabs → detalle → atrás)
        binding.btnBack.setOnClickListener(v -> {
            if (navController != null && !navController.navigateUp()) {
                finish();
            }
        });

        setupBottomNav();
    }

    // ── Permisos ──────────────────────────────────────────────────────────────

    private void requestNotifPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF_PERMISSION);
        }
    }

    // ── Bottom Nav ────────────────────────────────────────────────────────────

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_citas);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, AsesorHomeActivity.class)); finish();
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, MensajesActivity.class)); finish();
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, AsesorPerfilActivity.class)); finish();
            }
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        // Primero intenta navegar atrás en el grafo; si no puede, sale de la Activity
        if (navController == null || !navController.navigateUp()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
