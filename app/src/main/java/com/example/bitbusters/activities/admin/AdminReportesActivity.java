package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.example.bitbusters.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AdminReportesActivity extends AdminMainActivity {

    private String[] tendenciaOptions = {"Este mes", "Esta semana", "Este semestre", "Este año"};
    private int selectedOption = 0;
    private TextView tvTendenciaTitle;
    private Chip chipEstesMes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reportes);
        setupHeaderListeners();
        setupBottomNavigation(R.id.nav_reportes);
        setupListeners();
    }

    private void setupListeners() {
        chipEstesMes = findViewById(R.id.chipEstesMes);
        if (chipEstesMes != null) {
            chipEstesMes.setOnClickListener(v -> showTendenciaDialog());
        }

        Chip chipVerReportes = findViewById(R.id.chipVerReportes);
        if (chipVerReportes != null) {
            chipVerReportes.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminDetallesDeReporteProyectoActivity.class);
                startActivity(intent);
            });
        }

        tvTendenciaTitle = findViewById(R.id.tvTendenciaTitle);
    }

    private void showTendenciaDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Seleccionar período")
            .setSingleChoiceItems(tendenciaOptions, selectedOption, (dialog, which) -> {
                selectedOption = which;
                updateTendenciaTitle();
                updateChipText();
                dialog.dismiss();
            })
            .show();
    }

    private void updateTendenciaTitle() {
        if (tvTendenciaTitle != null) {
            tvTendenciaTitle.setText("Tendencia " + tendenciaOptions[selectedOption].toLowerCase());
        }
    }

    private void updateChipText() {
        if (chipEstesMes != null) {
            chipEstesMes.setText(tendenciaOptions[selectedOption]);
        }
    }
}
