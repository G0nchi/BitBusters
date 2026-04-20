package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AdminDetallesSeparacionActivity extends AppCompatActivity {

    private TextView tvProjectName, tvUbication, tvMonto, tvClienteName, tvClienteDNI, tvClientePhone, tvClienteEmail, tvAsesorName;
    private Button btnAprobar, btnRechazar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_detalles_separacion);
        
        initializeViews();
        loadSeparacionData();
        setupListeners();
    }

    private void initializeViews() {
        tvProjectName = findViewById(R.id.tvProjectName);
        tvUbication = findViewById(R.id.tvUbication);
        tvMonto = findViewById(R.id.tvMonto);
        tvClienteName = findViewById(R.id.tvClienteName);
        tvClienteDNI = findViewById(R.id.tvClienteDNI);
        tvClientePhone = findViewById(R.id.tvClientePhone);
        tvClienteEmail = findViewById(R.id.tvClienteEmail);
        tvAsesorName = findViewById(R.id.tvAsesorName);
        
        btnAprobar = findViewById(R.id.btnAprobar);
        btnRechazar = findViewById(R.id.btnRechazar);
    }

    private void loadSeparacionData() {
        // Recibir datos de Intent
        Intent intent = getIntent();
        String nombreProyecto = intent.getStringExtra("nombreProyecto");
        String precio = intent.getStringExtra("precio");
        String fecha = intent.getStringExtra("fecha");
        String asesor = intent.getStringExtra("asesor");

        // Mostrar datos en la UI
        if (tvProjectName != null && nombreProyecto != null) {
            tvProjectName.setText(nombreProyecto);
        }
        
        if (tvMonto != null && precio != null) {
            tvMonto.setText(precio);
        }
        
        if (tvAsesorName != null && asesor != null) {
            tvAsesorName.setText(asesor);
        }
    }

    private void setupListeners() {
        // Back button
        ImageButton backButton = findViewById(R.id.btnBackSeparation);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Aprobar button - Show confirmation dialog
        if (btnAprobar != null) {
            btnAprobar.setOnClickListener(v -> showAprobarDialog());
        }

        // Rechazar button - Show confirmation dialog
        if (btnRechazar != null) {
            btnRechazar.setOnClickListener(v -> showRechazarDialog());
        }
    }

    private void showAprobarDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Confirmar aprobación")
            .setMessage("¿Está seguro de que desea aprobar esta separación?")
            .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
            .setPositiveButton("Aprobar", (dialog, which) -> {
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
                Toast.makeText(this, "Separación rechazada", Toast.LENGTH_SHORT).show();
                finish();
            })
            .show();
    }
}
