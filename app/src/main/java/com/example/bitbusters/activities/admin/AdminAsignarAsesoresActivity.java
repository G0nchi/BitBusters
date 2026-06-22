package com.example.bitbusters.activities.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.AdminAsesorAdapter;
import com.example.bitbusters.data.FirestoreAsesoresRepository;
import com.example.bitbusters.data.AdminProyectoSessionData;
import com.example.bitbusters.models.AdminAsesor;
import com.example.bitbusters.models.AdminAsesorInmobiliaria;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private TextInputEditText etBuscarAsesor;
    private TextView tvEmptyState;

    private int selectedCount = 0;
    private final List<AdminAsesor> selectedAsesores = new ArrayList<>();
    private final List<AdminAsesor> sourceAsesores = new ArrayList<>();
    private final List<AdminAsesor> visibleAsesores = new ArrayList<>();
    private final Set<String> selectedNames = new HashSet<>();
    private String currentQuery = "";
    private final FirestoreAsesoresRepository asesoresRepository = new FirestoreAsesoresRepository();

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
        etBuscarAsesor         = findViewById(R.id.etBuscarAsesor);
        rvAsesores             = findViewById(R.id.rvAsesores);
        tvEmptyState           = findViewById(R.id.tvEmptyStateAsesores);
    }

    private void setupSearch() {
        if (etBuscarAsesor == null) return;

        etBuscarAsesor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                currentQuery = s == null ? "" : s.toString();
                applySearchAndRender();
            }
        });
    }

    private void setupRecyclerView() {
        if (rvAsesores == null) return;

        rvAsesores.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminAsesorAdapter(new ArrayList<>(), (position, isChecked) -> {
            if (position < 0 || position >= visibleAsesores.size()) return;
            AdminAsesor asesor = visibleAsesores.get(position);
            if (isChecked) {
                if (!selectedNames.contains(asesor.getNombre())) {
                    selectedAsesores.add(asesor);
                    selectedNames.add(asesor.getNombre());
                }
            } else {
                selectedNames.remove(asesor.getNombre());
                for (int i = selectedAsesores.size() - 1; i >= 0; i--) {
                    if (selectedAsesores.get(i).getNombre().equals(asesor.getNombre())) {
                        selectedAsesores.remove(i);
                    }
                }
            }
            adapter.setSelectedNames(selectedNames);
            updateCounter();
        });
        rvAsesores.setAdapter(adapter);

        setupSearch();
        cargarAsesoresActivosDesdeFirestore();
    }

    /**
     * Si el admin ya había seleccionado asesores antes en esta misma sesión de creación,
     * pre-marca los checks correspondientes en la lista.
     */
    private void premarcarAsesoresGuardados() {
        List<String> guardados = AdminProyectoSessionData.getInstance().asesoresAsignados;
        if (guardados == null || guardados.isEmpty()) return;
        selectedNames.clear();
        selectedNames.addAll(guardados);
        selectedAsesores.clear();
        for (AdminAsesor asesor : sourceAsesores) {
            if (selectedNames.contains(asesor.getNombre())) {
                selectedAsesores.add(asesor);
            }
        }
        if (adapter != null) {
            adapter.setSelectedNames(selectedNames);
        }
        updateCounter();
    }

    private void cargarAsesoresActivosDesdeFirestore() {
        asesoresRepository.obtenerAsesoresActivosRegistrados(this, new FirestoreAsesoresRepository.AsesoresCallback() {
            @Override
            public void onSuccess(List<AdminAsesorInmobiliaria> asesores) {
                sourceAsesores.clear();
                if (asesores != null) {
                    for (AdminAsesorInmobiliaria asesor : asesores) {
                        sourceAsesores.add(toAdminAsesor(asesor));
                    }
                }
                applySearchAndRender();
                premarcarAsesoresGuardados();
            }

            @Override
            public void onError(String mensaje) {
                sourceAsesores.clear();
                applySearchAndRender();
            }
        });
    }

    private AdminAsesor toAdminAsesor(AdminAsesorInmobiliaria asesor) {
        return new AdminAsesor(
                asesor.getId(),
                asesor.getNombre(),
                asesor.getIniciales(),
                0,
                asesor.getEstado(),
                asesor.getEmail(),
                asesor.getTelefono()
        );
    }

    private void applySearchAndRender() {
        List<AdminAsesor> filtered = new ArrayList<>();
        String query = currentQuery == null ? "" : currentQuery.trim().toLowerCase();

        for (AdminAsesor asesor : sourceAsesores) {
            String nombre = asesor.getNombre() == null ? "" : asesor.getNombre().toLowerCase();
            if (query.isEmpty() || nombre.contains(query)) {
                filtered.add(asesor);
            }
        }
        visibleAsesores.clear();
        visibleAsesores.addAll(filtered);

        if (adapter != null) {
            adapter.setData(filtered);
            adapter.setSelectedNames(selectedNames);
        }
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(filtered.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        }
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
