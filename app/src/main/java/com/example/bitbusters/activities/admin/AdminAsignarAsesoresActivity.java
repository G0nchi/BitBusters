package com.example.bitbusters.activities.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.AdminAsesorAdapter;
import com.example.bitbusters.data.AdminDataRepository;
import com.example.bitbusters.data.AdminProyectoSessionData;
import com.example.bitbusters.models.AdminAsesor;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity para que el admin seleccione asesores y los asigne al proyecto
 * que está creando. Al confirmar, guarda los nombres de los asesores seleccionados
 * en AdminProyectoSessionData.getInstance().asesoresAsignados y regresa.
 */
public class AdminAsignarAsesoresActivity extends AppCompatActivity {

    private RecyclerView     rvAsesores;
    private AdminAsesorAdapter adapter;
    private Button btnConfirmarAsignacion, btnCancelarAsesores;
    private ImageButton btnBackAsesores;

    private int selectedCount = 0;
    private final List<AdminAsesor> selectedAsesores = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_asignar_asesores);

        initializeViews();
        setupListeners();
        setupRecyclerView();
        premarcarAsesoresGuardados(); // restaurar selección previa de la sesión
    }

    private void initializeViews() {
        btnConfirmarAsignacion = findViewById(R.id.btnConfirmarAsignacion);
        btnCancelarAsesores    = findViewById(R.id.btnCancelarAsesores);
        btnBackAsesores        = findViewById(R.id.btnBackAsesores);
        rvAsesores             = findViewById(R.id.rvAsesores);
    }

    private void setupRecyclerView() {
        if (rvAsesores == null) return;

        rvAsesores.setLayoutManager(new LinearLayoutManager(this));
        List<AdminAsesor> asesoresList = AdminDataRepository.getAsesores();

        adapter = new AdminAsesorAdapter(asesoresList, (position, isChecked) -> {
            AdminAsesor asesor = asesoresList.get(position);
            if (isChecked) {
                if (!selectedAsesores.contains(asesor)) selectedAsesores.add(asesor);
            } else {
                selectedAsesores.remove(asesor);
            }
            updateCounter();
        });
        rvAsesores.setAdapter(adapter);
    }

    /**
     * Si el admin ya había seleccionado asesores antes en esta misma sesión de creación,
     * pre-marca los checks correspondientes en la lista.
     */
    private void premarcarAsesoresGuardados() {
        List<String> guardados = AdminProyectoSessionData.getInstance().asesoresAsignados;
        if (guardados == null || guardados.isEmpty()) return;

        List<AdminAsesor> todosList = AdminDataRepository.getAsesores();
        for (AdminAsesor asesor : todosList) {
            if (guardados.contains(asesor.getNombre())) {
                selectedAsesores.add(asesor);
            }
        }
        updateCounter();
    }

    private void setupListeners() {
        btnConfirmarAsignacion.setOnClickListener(v -> confirmAssignment());
        btnCancelarAsesores.setOnClickListener(v -> finish());
        if (btnBackAsesores != null) {
            btnBackAsesores.setOnClickListener(v -> finish());
        }
    }

    private void updateCounter() {
        selectedCount = selectedAsesores.size();
        btnConfirmarAsignacion.setText(
                "Confirmar asignación (" + selectedCount + ")");
    }

    // ── Parte 2: Guardar asesores seleccionados en la sesión ─────────────────

    /**
     * Extrae los nombres de los asesores seleccionados, los guarda en
     * AdminProyectoSessionData y cierra la Activity.
     */
    private void confirmAssignment() {
        // Extraer solo los nombres para guardarlos en la sesión
        List<String> nombres = new ArrayList<>();
        for (AdminAsesor asesor : selectedAsesores) {
            nombres.add(asesor.getNombre());
        }
        AdminProyectoSessionData.getInstance().asesoresAsignados = nombres;

        String msg = selectedCount + " asesor" + (selectedCount != 1 ? "es" : "")
                + " asignado" + (selectedCount != 1 ? "s" : "");
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        finish(); // volver a AdminCrearProyectoActivity
    }
}
