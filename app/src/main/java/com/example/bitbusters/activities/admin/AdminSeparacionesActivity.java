package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;

public class AdminSeparacionesActivity extends AdminMainActivity {

    private Button btnPendientes, btnAprobadas, btnRechazadas;
    private MaterialCardView cardSeparacion1, cardSeparacion2, cardSeparacion3, cardSeparacion4;
    private AutoCompleteTextView actvProyectoFilter, actvFechaFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_separaciones);
        
        setupBottomNavigation(R.id.nav_separaciones);
        setupNotificationsButton();
        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        btnPendientes = findViewById(R.id.btnPendientes);
        btnAprobadas = findViewById(R.id.btnAprobadas);
        btnRechazadas = findViewById(R.id.btnRechazadas);
        
        cardSeparacion1 = findViewById(R.id.cardSeparacion1);
        cardSeparacion2 = findViewById(R.id.cardSeparacion2);
        cardSeparacion3 = findViewById(R.id.cardSeparacion3);
        cardSeparacion4 = findViewById(R.id.cardSeparacion4);
        
        actvProyectoFilter = findViewById(R.id.actvProyectoFilter);
        actvFechaFilter = findViewById(R.id.actvFechaFilter);
        
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

    private void setupListeners() {
        // Tab buttons
        if (btnPendientes != null) {
            btnPendientes.setOnClickListener(v -> selectTab(btnPendientes));
        }
        if (btnAprobadas != null) {
            btnAprobadas.setOnClickListener(v -> selectTab(btnAprobadas));
        }
        if (btnRechazadas != null) {
            btnRechazadas.setOnClickListener(v -> selectTab(btnRechazadas));
        }

        // Card click listeners - navigate to AdminDetallesSeparacionActivity
        if (cardSeparacion1 != null) {
            cardSeparacion1.setOnClickListener(v -> goToDetalles("Edificio Los Álamos", "S/ 5,000", "01/abr/2026", "Carlos Ruiz"));
        }
        if (cardSeparacion2 != null) {
            cardSeparacion2.setOnClickListener(v -> goToDetalles("Mirador de Surco", "S/ 8,500", "31/mar/2026", "Ana Torres"));
        }
        if (cardSeparacion3 != null) {
            cardSeparacion3.setOnClickListener(v -> goToDetalles("Alto San Felipe", "S/ 6,200", "28/mar/2026", "Mario Pérez"));
        }
        if (cardSeparacion4 != null) {
            cardSeparacion4.setOnClickListener(v -> goToDetalles("Residencial Verde", "S/ 7,100", "20/abr/2026", "Lucia Vargas"));
        }
    }

    private void selectTab(Button selectedButton) {
        // Reset all buttons
        btnPendientes.setBackground(getDrawable(R.drawable.button_outline_state_bg));
        btnAprobadas.setBackground(getDrawable(R.drawable.button_outline_state_bg));
        btnRechazadas.setBackground(getDrawable(R.drawable.button_outline_state_bg));

        btnPendientes.setTextColor(getColor(R.color.neutral_medium));
        btnAprobadas.setTextColor(getColor(R.color.neutral_medium));
        btnRechazadas.setTextColor(getColor(R.color.neutral_medium));

        // Highlight selected button
        selectedButton.setBackground(getDrawable(R.color.brand_deep_blue));
        selectedButton.setTextColor(getColor(android.R.color.white));
    }

    private void goToDetalles(String nombreProyecto, String precio, String fecha, String asesor) {
        Intent intent = new Intent(AdminSeparacionesActivity.this, AdminDetallesSeparacionActivity.class);
        intent.putExtra("nombreProyecto", nombreProyecto);
        intent.putExtra("precio", precio);
        intent.putExtra("fecha", fecha);
        intent.putExtra("asesor", asesor);
        startActivity(intent);
    }

    private void setupNotificationsButton() {
        ImageButton btnNotifications = findViewById(R.id.btnNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> {
                // TODO: Navigate to notifications activity
                startActivity(new Intent(AdminSeparacionesActivity.this, AdminNotificacionesActivity.class));
            });
        }
    }
}
