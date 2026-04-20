package com.example.bitbusters.activities.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.R;

public class AdminAsignarAsesoresActivity extends AppCompatActivity {

    private CheckBox cbAsesor1, cbAsesor2, cbAsesor3, cbAsesor4, cbAsesor5;
    private Button btnConfirmarAsignacion, btnCancelarAsesores;
    private ImageButton btnBackAsesores;
    private int selectedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_asignar_asesores);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        cbAsesor1 = findViewById(R.id.cbAsesor1);
        cbAsesor2 = findViewById(R.id.cbAsesor2);
        cbAsesor3 = findViewById(R.id.cbAsesor3);
        cbAsesor4 = findViewById(R.id.cbAsesor4);
        cbAsesor5 = findViewById(R.id.cbAsesor5);

        btnConfirmarAsignacion = findViewById(R.id.btnConfirmarAsignacion);
        btnCancelarAsesores = findViewById(R.id.btnCancelarAsesores);
        btnBackAsesores = findViewById(R.id.btnBackAsesores);
    }

    private void setupListeners() {
        // Checkbox listeners
        cbAsesor1.setOnCheckedChangeListener((buttonView, isChecked) -> updateCounter());
        cbAsesor2.setOnCheckedChangeListener((buttonView, isChecked) -> updateCounter());
        cbAsesor3.setOnCheckedChangeListener((buttonView, isChecked) -> updateCounter());
        cbAsesor4.setOnCheckedChangeListener((buttonView, isChecked) -> updateCounter());
        cbAsesor5.setOnCheckedChangeListener((buttonView, isChecked) -> updateCounter());

        // Button listeners
        btnConfirmarAsignacion.setOnClickListener(v -> confirmAssignment());
        btnCancelarAsesores.setOnClickListener(v -> finish());
        btnBackAsesores.setOnClickListener(v -> finish());
    }

    private void updateCounter() {
        selectedCount = 0;
        
        if (cbAsesor1.isChecked()) selectedCount++;
        if (cbAsesor2.isChecked()) selectedCount++;
        if (cbAsesor3.isChecked()) selectedCount++;
        if (cbAsesor4.isChecked()) selectedCount++;
        if (cbAsesor5.isChecked()) selectedCount++;

        // Update button text with counter
        btnConfirmarAsignacion.setText("Confirmar asignación (" + selectedCount + ")");
    }

    private void confirmAssignment() {
        // TODO: Save selected advisors to database
        // For now, just go back to AdminEditarProyectoActivity
        finish();
    }
}
