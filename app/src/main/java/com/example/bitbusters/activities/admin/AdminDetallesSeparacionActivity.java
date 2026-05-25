package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.R;
import com.example.bitbusters.data.SeparacionesRepository;
import com.example.bitbusters.models.AdminSeparacion;
import com.example.bitbusters.utils.AdminPreferencesManager;
import com.example.bitbusters.utils.AdminStorageManager;
import com.example.bitbusters.utils.NotificationHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Pantalla de detalle de una separación específica.
 *
 * Correcciones Lab 5:
 *  - Lee el "separacion_id" del Intent y busca los datos en SeparacionesRepository (Corrección 2)
 *  - Al aprobar: actualiza el estado en SeparacionesRepository → la lista refleja "Aprobada" (Corrección 3)
 *  - La notificación "Separación Aprobada" incluye el ID para que la lista haga scroll al ítem (Corrección 3)
 */
public class AdminDetallesSeparacionActivity extends AppCompatActivity {

    private TextView tvProjectName, tvUbication, tvMonto,
                     tvClienteName, tvClienteDNI, tvClientePhone, tvClienteEmail, tvAsesorName;
    private Button btnAprobar, btnRechazar;

    /** ID de la separación actualmente mostrada; null si vino por extras legacy. */
    private String separacionId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_detalles_separacion);

        initializeViews();
        loadSeparacionData();
        setupListeners();
    }

    private void initializeViews() {
        tvProjectName  = findViewById(R.id.tvProjectName);
        tvUbication    = findViewById(R.id.tvUbication);
        tvMonto        = findViewById(R.id.tvMonto);
        tvClienteName  = findViewById(R.id.tvClienteName);
        tvClienteDNI   = findViewById(R.id.tvClienteDNI);
        tvClientePhone = findViewById(R.id.tvClientePhone);
        tvClienteEmail = findViewById(R.id.tvClienteEmail);
        tvAsesorName   = findViewById(R.id.tvAsesorName);

        btnAprobar  = findViewById(R.id.btnAprobar);
        btnRechazar = findViewById(R.id.btnRechazar);
    }

    /**
     * Carga los datos de la separación desde el Intent.
     *
     * Prioridad:
     *  1. Si el Intent tiene "separacion_id" → buscar en SeparacionesRepository
     *  2. Si no hay ID → usar los extras legacy (nombreProyecto, precio, fecha, asesor)
     */
    private void loadSeparacionData() {
        Intent intent = getIntent();

        // ── Corrección 2: Buscar por ID en el repositorio compartido ────────
        separacionId = intent.getStringExtra(AdminSeparacionesActivity.EXTRA_SEPARACION_ID);

        if (separacionId != null) {
            AdminSeparacion separacion = SeparacionesRepository.getById(separacionId);
            if (separacion != null) {
                setTextSafe(tvProjectName, separacion.getNombreProyecto());
                setTextSafe(tvMonto,       separacion.getMonto());
                setTextSafe(tvAsesorName,  separacion.getCliente());
                setTextSafe(tvUbication,   separacion.getFecha());
                // Nombre del cliente
                setTextSafe(tvClienteName, separacion.getCliente());
                return; // datos cargados desde repositorio, no continuar
            }
        }

        // ── Fallback legacy: leer extras individuales (retrocompatibilidad) ──
        String nombreProyecto = intent.getStringExtra("nombreProyecto");
        String precio         = intent.getStringExtra("precio");
        String asesor         = intent.getStringExtra("asesor");

        setTextSafe(tvProjectName, nombreProyecto);
        setTextSafe(tvMonto,       precio);
        setTextSafe(tvAsesorName,  asesor);
    }

    private void setupListeners() {
        // Botón atrás
        ImageButton backButton = findViewById(R.id.btnBackSeparation);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        if (btnAprobar != null)  btnAprobar.setOnClickListener(v  -> showAprobarDialog());
        if (btnRechazar != null) btnRechazar.setOnClickListener(v -> showRechazarDialog());
    }

    // ── Diálogos de confirmación ─────────────────────────────────────────────

    private void showAprobarDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Confirmar aprobación")
            .setMessage("¿Está seguro de que desea aprobar esta separación?")
            .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
            .setPositiveButton("Aprobar", (dialog, which) -> {

                // ── Lab 5 (Parte 1): Actualizar contadores en SharedPreferences ────
                AdminPreferencesManager.decrementarSeparacionesPendientes(this);

                // ── Lab 5 (Parte 3): Actualizar en Internal Storage ───────────────
                AdminStorageManager.actualizarContador(
                        this, AdminStorageManager.CAMPO_SEPARACIONES_APROBADAS);

                // ── Corrección 3: Actualizar estado en SeparacionesRepository ─────
                // Esto hace que la lista muestre "Aprobada" (en verde) al volver
                if (separacionId != null) {
                    SeparacionesRepository.actualizarEstado(separacionId, "Aprobada");
                }

                // ── Corrección 3: Notificación con ID para que la lista haga scroll ─
                // Al tocar → AdminSeparacionesActivity recibe el ID y hace scroll al ítem
                Intent destinoNotif = new Intent(this, AdminSeparacionesActivity.class);
                if (separacionId != null) {
                    destinoNotif.putExtra(AdminSeparacionesActivity.EXTRA_SEPARACION_ID,
                            separacionId);
                }
                NotificationHelper.lanzarNotificacionAdmin(
                        this,
                        "Separación Aprobada",
                        "La separación ha sido aprobada. El cliente tiene 10 minutos para completar el pago",
                        NotificationHelper.NOTIF_ADMIN_SEPARACION_APROBADA,
                        destinoNotif
                );

                Toast.makeText(this, "Separación aprobada exitosamente", Toast.LENGTH_SHORT).show();
                finish();
            })
            .show();
    }

    private void showRechazarDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Confirmar rechazo")
            .setMessage("¿Está seguro de que desea rechazar esta separación?")
            .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
            .setPositiveButton("Rechazar", (dialog, which) -> {
                // Actualizar estado en el repositorio
                if (separacionId != null) {
                    SeparacionesRepository.actualizarEstado(separacionId, "Rechazada");
                }
                // Decrementar pendientes (la separación ya no está pendiente)
                AdminPreferencesManager.decrementarSeparacionesPendientes(this);

                Toast.makeText(this, "Separación rechazada", Toast.LENGTH_SHORT).show();
                finish();
            })
            .show();
    }

    // ── Utilidad ─────────────────────────────────────────────────────────────

    /**
     * Establece el texto de un TextView solo si el valor no es null.
     *
     * @param tv    TextView destino.
     * @param valor Texto a establecer; si es null no hace nada.
     */
    private void setTextSafe(TextView tv, String valor) {
        if (tv != null && valor != null) {
            tv.setText(valor);
        }
    }
}
