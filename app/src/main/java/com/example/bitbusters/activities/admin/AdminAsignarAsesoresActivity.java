package com.example.bitbusters.activities.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.AdminAsesorAdapter;
import com.example.bitbusters.data.AdminDataRepository;
import com.example.bitbusters.models.AdminAsesor;

import java.util.ArrayList;
import java.util.List;

public class AdminAsignarAsesoresActivity extends AppCompatActivity {

    private RecyclerView rvAsesores;
    private AdminAsesorAdapter adapter;
    private Button btnConfirmarAsignacion, btnCancelarAsesores;
    private ImageButton btnBackAsesores;
    private int selectedCount = 0;
    private List<AdminAsesor> selectedAsesores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_asignar_asesores);

        initializeViews();
        setupListeners();
        setupRecyclerView();
    }

    private void initializeViews() {
        btnConfirmarAsignacion = findViewById(R.id.btnConfirmarAsignacion);
        btnCancelarAsesores = findViewById(R.id.btnCancelarAsesores);
        btnBackAsesores = findViewById(R.id.btnBackAsesores);
        rvAsesores = findViewById(R.id.rvAsesores);
        selectedAsesores = new ArrayList<>();
    }

    private void setupRecyclerView() {
        if (rvAsesores != null) {
            rvAsesores.setLayoutManager(new LinearLayoutManager(this));
            List<AdminAsesor> asesoresList = AdminDataRepository.getAsesores();
            adapter = new AdminAsesorAdapter(asesoresList, (position, isChecked) -> {
                AdminAsesor asesor = asesoresList.get(position);
                if (isChecked) {
                    selectedAsesores.add(asesor);
                } else {
                    selectedAsesores.remove(asesor);
                }
                updateCounter();
            });
            rvAsesores.setAdapter(adapter);
        }
    }

    private void setupListeners() {
        btnConfirmarAsignacion.setOnClickListener(v -> confirmAssignment());
        btnCancelarAsesores.setOnClickListener(v -> finish());
        btnBackAsesores.setOnClickListener(v -> finish());
    }

    private void updateCounter() {
        selectedCount = selectedAsesores.size();
        btnConfirmarAsignacion.setText("Confirmar asignación (" + selectedCount + ")");
    }

    private void confirmAssignment() {
        // TODO: Save selected advisors to database
        // For now, just go back to AdminEditarProyectoActivity
        finish();
    }
}
