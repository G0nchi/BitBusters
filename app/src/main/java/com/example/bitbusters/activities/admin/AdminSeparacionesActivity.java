package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.AdminSeparacionAdapter;
import com.example.bitbusters.data.AdminDataRepository;
import com.example.bitbusters.models.AdminSeparacion;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;

import java.util.List;

public class AdminSeparacionesActivity extends AdminMainActivity {

    private Button btnPendientes, btnAprobadas, btnRechazadas;
    private AutoCompleteTextView actvProyectoFilter, actvFechaFilter;
    private RecyclerView rvSeparaciones;
    private AdminSeparacionAdapter adapter;
    private List<AdminSeparacion> allSeparaciones;
    private String currentEstadoFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_separaciones);
        setupHeaderListeners();
        setupBottomNavigation(R.id.nav_separaciones);
        setupNotificationsButton();
        initializeViews();
        setupListeners();
        setupRecyclerView();
    }

    private void initializeViews() {
        btnPendientes = findViewById(R.id.btnPendientes);
        btnAprobadas = findViewById(R.id.btnAprobadas);
        btnRechazadas = findViewById(R.id.btnRechazadas);
        
        actvProyectoFilter = findViewById(R.id.actvProyectoFilter);
        actvFechaFilter = findViewById(R.id.actvFechaFilter);
        rvSeparaciones = findViewById(R.id.rvSeparaciones);
        
        allSeparaciones = AdminDataRepository.getSeparaciones();
        setupDropdowns();
    }

    private void setupDropdowns() {
        // Setup Proyectos dropdown
        String[] proyectos = {
            "Todos los proyectos",
            "Edificio Los Álamos",
            "Mirador de Surco",
            "Alto San Felipe",
            "Residencial Verde"
        };
        ArrayAdapter<String> adapterProyectos = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, proyectos);
        actvProyectoFilter.setAdapter(adapterProyectos);
        actvProyectoFilter.setOnClickListener(v -> actvProyectoFilter.showDropDown());

        // Setup Fecha dropdown
        String[] fechas = {
            "Este mes",
            "Esta semana",
            "Este año",
            "Este bimestre"
        };
        ArrayAdapter<String> adapterFechas = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, fechas);
        actvFechaFilter.setAdapter(adapterFechas);
        actvFechaFilter.setOnClickListener(v -> actvFechaFilter.showDropDown());
    }

    private void setupRecyclerView() {
        if (rvSeparaciones != null) {
            rvSeparaciones.setLayoutManager(new LinearLayoutManager(this));
            adapter = new AdminSeparacionAdapter(allSeparaciones, separacion -> {
                Intent intent = new Intent(AdminSeparacionesActivity.this, AdminDetallesSeparacionActivity.class);
                intent.putExtra("nombreProyecto", separacion.getNombreProyecto());
                intent.putExtra("precio", separacion.getMonto());
                intent.putExtra("fecha", separacion.getFecha());
                intent.putExtra("asesor", separacion.getCliente());
                startActivity(intent);
            });
            rvSeparaciones.setAdapter(adapter);
        }
    }

    private void setupListeners() {
        // Tab buttons
        if (btnPendientes != null) {
            btnPendientes.setOnClickListener(v -> filterBySeparacionEstado("Pendiente"));
        }
        if (btnAprobadas != null) {
            btnAprobadas.setOnClickListener(v -> filterBySeparacionEstado("Aprobada"));
        }
        if (btnRechazadas != null) {
            btnRechazadas.setOnClickListener(v -> filterBySeparacionEstado("Rechazada"));
        }
    }

    private void filterBySeparacionEstado(String estado) {
        currentEstadoFilter = estado;
        selectTab(estado);
        List<AdminSeparacion> filtered = new java.util.ArrayList<>();
        for (AdminSeparacion sep : allSeparaciones) {
            if (sep.getEstado().equals(estado)) {
                filtered.add(sep);
            }
        }
        adapter.setData(filtered);
    }

    private void selectTab(String selectedEstado) {
        // Reset all buttons
        btnPendientes.setBackground(getDrawable(R.drawable.button_outline_state_bg));
        btnAprobadas.setBackground(getDrawable(R.drawable.button_outline_state_bg));
        btnRechazadas.setBackground(getDrawable(R.drawable.button_outline_state_bg));

        btnPendientes.setTextColor(getColor(R.color.neutral_medium));
        btnAprobadas.setTextColor(getColor(R.color.neutral_medium));
        btnRechazadas.setTextColor(getColor(R.color.neutral_medium));

        // Highlight selected button
        Button selectedBtn = null;
        if ("Pendiente".equals(selectedEstado)) {
            selectedBtn = btnPendientes;
        } else if ("Aprobada".equals(selectedEstado)) {
            selectedBtn = btnAprobadas;
        } else if ("Rechazada".equals(selectedEstado)) {
            selectedBtn = btnRechazadas;
        }

        if (selectedBtn != null) {
            selectedBtn.setBackground(getDrawable(R.color.brand_deep_blue));
            selectedBtn.setTextColor(getColor(android.R.color.white));
        }
    }

    private void setupNotificationsButton() {
        ImageButton btnNotifications = findViewById(R.id.btnNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> {
                startActivity(new Intent(AdminSeparacionesActivity.this, AdminNotificacionesActivity.class));
            });
        }
    }
}
