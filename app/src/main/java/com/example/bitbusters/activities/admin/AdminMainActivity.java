package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.R;
import com.example.bitbusters.utils.AdminPreferencesManager;
import com.example.bitbusters.utils.NotificationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

/**
 * Pantalla principal del Administrador de Inmobiliaria.
 * Actúa como clase base para las Activities hijas del admin (via herencia).
 *
 * Responsabilidades:
 *  - Inicializar canal de notificaciones admin y solicitar permiso (Lab 5)
 *  - Cargar y mostrar datos del admin desde AdminPreferencesManager
 *  - Proveer setupBottomNavigation() y setupHeaderListeners() a las subclases
 */
public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        // ── Lab 5: Crear canal admin y solicitar permiso de notificaciones ──
        NotificationHelper.crearCanalAdmin(this);
        NotificationHelper.solicitarPermiso(this);

        // Configurar navegación y listeners de la pantalla principal
        setupBottomNavigation(R.id.nav_dashboard);
        setupQuickActionListeners();
        setupHeaderListeners();

        // ── Lab 5 (Parte 1): Leer prefs del admin y mostrar en la cabecera ──
        cargarDatosAdmin();
    }

    /**
     * Lee los datos guardados en AdminPreferencesManager y los refleja
     * en los elementos de la cabecera de AdminMainActivity.
     *
     * Actualiza:
     *  - tvGreeting      → "Hola, [nombre del admin]"
     *  - tvRealEstateName → nombre de la inmobiliaria
     *  - tvAvatar         → iniciales del nombre (máximo 2 letras)
     */
    private void cargarDatosAdmin() {
        String nombre       = AdminPreferencesManager.obtenerNombre(this);
        String inmobiliaria = AdminPreferencesManager.obtenerInmobiliaria(this);

        // Saludo personalizado con el nombre guardado
        TextView tvGreeting = findViewById(R.id.tvGreeting);
        if (tvGreeting != null) {
            tvGreeting.setText("Hola, " + nombre);
        }

        // Nombre de la inmobiliaria en el header
        TextView tvRealEstateName = findViewById(R.id.tvRealEstateName);
        if (tvRealEstateName != null) {
            tvRealEstateName.setText(inmobiliaria);
        }

        // Iniciales del admin en el avatar circular
        TextView tvAvatar = findViewById(R.id.tvAvatar);
        if (tvAvatar != null) {
            tvAvatar.setText(obtenerIniciales(nombre));
        }
    }

    /**
     * Genera las iniciales (máximo 2 caracteres) del nombre completo del admin.
     * Ejemplo: "Juan García" → "JG" | "Administrador" → "AD"
     */
    private String obtenerIniciales(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) return "AD";
        String[] partes = nombre.trim().split("\\s+");
        StringBuilder iniciales = new StringBuilder();
        for (int i = 0; i < Math.min(2, partes.length); i++) {
            if (!partes[i].isEmpty()) {
                iniciales.append(partes[i].charAt(0));
            }
        }
        return iniciales.toString().toUpperCase();
    }

    /**
     * Configura los listeners del header (botón de notificaciones, avatar y nombre de inmobiliaria).
     * Método protected para que las Activities hijas puedan reutilizarlo con su propio layout.
     */
    protected void setupHeaderListeners() {
        // Botón de notificaciones → abre AdminNotificacionesActivity
        ImageButton btnNotifications = findViewById(R.id.btnNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v ->
                    startActivity(new Intent(AdminMainActivity.this, AdminNotificacionesActivity.class))
            );
        }

        // Avatar del admin → abre AdminPerfilActivity
        TextView tvAvatar = findViewById(R.id.tvAvatar);
        if (tvAvatar != null) {
            tvAvatar.setOnClickListener(v ->
                    startActivity(new Intent(AdminMainActivity.this, AdminPerfilActivity.class))
            );
        }

        // Nombre de la inmobiliaria → abre AdminDetallesInmobiliariaActivity
        TextView tvRealEstateName = findViewById(R.id.tvRealEstateName);
        if (tvRealEstateName != null) {
            tvRealEstateName.setOnClickListener(v ->
                    startActivity(new Intent(AdminMainActivity.this, AdminDetallesInmobiliariaActivity.class))
            );
        }
    }

    /** Configura las tarjetas de acciones rápidas del dashboard principal. */
    private void setupQuickActionListeners() {
        // Crear proyecto → AdminCrearProyectoActivity
        MaterialCardView cardCreateProject = findViewById(R.id.cardCreateProject);
        if (cardCreateProject != null) {
            cardCreateProject.setOnClickListener(v ->
                    startActivity(new Intent(AdminMainActivity.this, AdminCrearProyectoActivity.class))
            );
        }

        // Ver separaciones → AdminSeparacionesActivity
        MaterialCardView cardViewSeparations = findViewById(R.id.cardViewSeparations);
        if (cardViewSeparations != null) {
            cardViewSeparations.setOnClickListener(v ->
                    startActivity(new Intent(AdminMainActivity.this, AdminSeparacionesActivity.class))
            );
        }

        // Ver reportes → AdminReportesActivity
        MaterialCardView cardViewReports = findViewById(R.id.cardViewReports);
        if (cardViewReports != null) {
            cardViewReports.setOnClickListener(v ->
                    startActivity(new Intent(AdminMainActivity.this, AdminReportesActivity.class))
            );
        }

        // Asignar asesor → AdminProyectosActivity (lista de proyectos para asignar)
        MaterialCardView cardAssignAdvisor = findViewById(R.id.cardAssignAdvisor);
        if (cardAssignAdvisor != null) {
            cardAssignAdvisor.setOnClickListener(v ->
                    startActivity(new Intent(AdminMainActivity.this, AdminProyectosActivity.class))
            );
        }
    }

    /**
     * Configura la barra de navegación inferior.
     * Se llama también desde las Activities hijas pasando su ítem seleccionado.
     *
     * @param selectedItemId ID del ítem del menú que debe quedar resaltado.
     */
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
