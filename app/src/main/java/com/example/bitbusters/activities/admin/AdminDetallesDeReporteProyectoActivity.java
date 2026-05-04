package com.example.bitbusters.activities.admin;

import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.AdminHistorialSeparacionAdapter;
import com.example.bitbusters.data.AdminDataRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class AdminDetallesDeReporteProyectoActivity extends AppCompatActivity {

    private String[] proyectos = {"Edificio Los Álamos", "Mirador Surco", "Alto San Felipe", "R. Balta"};
    private String selectedPeriodo = "Mensual";
    private String selectedProyecto = "Edificio Los Álamos";
    
    // Valores por proyecto y período
    private int[][] montos = {
        // Edificio Los Álamos
        {12000, 42000, 85000},
        // Mirador Surco
        {8500, 28500, 56000},
        // Alto San Felipe
        {5000, 18200, 36400},
        // R. Balta
        {3200, 6700, 13400}
    };
    
    private int[][] separaciones = {
        // Edificio Los Álamos
        {2, 7, 14},
        // Mirador Surco
        {1, 3, 6},
        // Alto San Felipe
        {1, 2, 4},
        // R. Balta
        {1, 1, 2}
    };
    
    private int[][] asesores = {
        // Edificio Los Álamos
        {1, 2, 3},
        // Mirador Surco
        {1, 1, 2},
        // Alto San Felipe
        {1, 1, 1},
        // R. Balta
        {1, 1, 1}
    };

    private TextView tvResumenMonto, tvResumenSeparaciones, tvResumenAsesores;
    private Button btnDiario, btnMensual, btnAnual;
    private AutoCompleteTextView actvProjecto;
    private RecyclerView rvHistorial;
    private AdminHistorialSeparacionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_detalles_reporte_proyecto);
        setupListeners();
        setupDropdown();
        updateButtonStyles();
        updateResumen();
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        rvHistorial = findViewById(R.id.rvHistorialSeparaciones);
        if (rvHistorial != null) {
            rvHistorial.setLayoutManager(new LinearLayoutManager(this));
            adapter = new AdminHistorialSeparacionAdapter(AdminDataRepository.getHistorialSeparaciones());
            rvHistorial.setAdapter(adapter);
        }
    }

    private void setupListeners() {
        ImageButton backButton = findViewById(R.id.btnBackReporte);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        btnDiario = findViewById(R.id.btnDiario);
        btnMensual = findViewById(R.id.btnMensual);
        btnAnual = findViewById(R.id.btnAnual);
        
        tvResumenMonto = findViewById(R.id.tvResumenMonto);
        tvResumenSeparaciones = findViewById(R.id.tvResumenSeparaciones);
        tvResumenAsesores = findViewById(R.id.tvResumenAsesores);
        actvProjecto = findViewById(R.id.actvProjecto);

        if (btnDiario != null) {
            btnDiario.setOnClickListener(v -> setPeriodo("Diario"));
        }
        if (btnMensual != null) {
            btnMensual.setOnClickListener(v -> setPeriodo("Mensual"));
        }
        if (btnAnual != null) {
            btnAnual.setOnClickListener(v -> setPeriodo("Anual"));
        }
    }

    private void setupDropdown() {
        // Crear adapter para AutoCompleteTextView
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_dropdown_item_1line, proyectos);
        actvProjecto.setAdapter(adapter);
        actvProjecto.setText(selectedProyecto, false);
        
        // Abrir dropdown al hacer click
        actvProjecto.setOnClickListener(v -> {
            actvProjecto.showDropDown();
        });
        
        // Listener para cuando se selecciona un proyecto
        actvProjecto.setOnItemClickListener((parent, view, position, id) -> {
            selectedProyecto = proyectos[position];
            updateResumen();
        });
    }

    private void setPeriodo(String periodo) {
        selectedPeriodo = periodo;
        updateButtonStyles();
        updateResumen();
    }

    private void updateButtonStyles() {
        if (btnDiario != null && btnMensual != null && btnAnual != null) {
            // Resetear todos los botones a estado no seleccionado
            android.content.res.ColorStateList transparentTint = 
                android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT);
            android.content.res.ColorStateList outlineColor = 
                android.content.res.ColorStateList.valueOf(getColor(R.color.text_primary));
            
            // Desseleccionar todos
            setButtonUnselected(btnDiario, outlineColor);
            setButtonUnselected(btnMensual, outlineColor);
            setButtonUnselected(btnAnual, outlineColor);
            
            // Seleccionar el botón correcto
            Button selectedBtn = null;
            if (selectedPeriodo.equals("Diario")) {
                selectedBtn = btnDiario;
            } else if (selectedPeriodo.equals("Mensual")) {
                selectedBtn = btnMensual;
            } else if (selectedPeriodo.equals("Anual")) {
                selectedBtn = btnAnual;
            }
            
            if (selectedBtn != null) {
                setButtonSelected(selectedBtn);
            }
        }
    }
    
    private void setButtonUnselected(Button button, android.content.res.ColorStateList outlineColor) {
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
        button.setTextColor(getColor(R.color.text_primary));
        if (button instanceof MaterialButton) {
            ((MaterialButton) button).setStrokeColor(outlineColor);
        }
    }
    
    private void setButtonSelected(Button button) {
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.brand_deep_blue)));
        button.setTextColor(getColor(android.R.color.white));
        if (button instanceof MaterialButton) {
            android.content.res.ColorStateList blueTint = 
                android.content.res.ColorStateList.valueOf(getColor(R.color.brand_deep_blue));
            ((MaterialButton) button).setStrokeColor(blueTint);
        }
    }

    private void updateResumen() {
        // Solo actualizar valores si es Edificio Los Álamos o Mirador Surco
        int proyectoIndex = -1;
        for (int i = 0; i < proyectos.length; i++) {
            if (proyectos[i].equals(selectedProyecto)) {
                proyectoIndex = i;
                break;
            }
        }

        if (proyectoIndex == 0 || proyectoIndex == 1) { // Solo Alamos y Mirador
            int periodoIndex = getPeriodoIndex();
            
            if (tvResumenMonto != null) {
                tvResumenMonto.setText("S/" + montos[proyectoIndex][periodoIndex] + ",000");
            }
            if (tvResumenSeparaciones != null) {
                tvResumenSeparaciones.setText(String.valueOf(separaciones[proyectoIndex][periodoIndex]));
            }
            if (tvResumenAsesores != null) {
                tvResumenAsesores.setText(String.valueOf(asesores[proyectoIndex][periodoIndex]));
            }
        }
    }

    private int getPeriodoIndex() {
        if (selectedPeriodo.equals("Diario")) return 0;
        if (selectedPeriodo.equals("Mensual")) return 1;
        if (selectedPeriodo.equals("Anual")) return 2;
        return 1; // default Mensual
    }
}
