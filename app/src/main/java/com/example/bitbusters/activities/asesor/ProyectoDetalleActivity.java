package com.example.bitbusters.activities.asesor;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProyectoDetalleActivity extends AppCompatActivity {

    public static final String EXTRA_PROYECTO_INDEX = "proyecto_index";

    private static final String[] NOMBRES = {
        "Vista Marina Residencial",
        "Torres del Sol",
        "Condominio Los Pinos"
    };
    private static final String[] CIUDADES = {
        "Lima, Callao",
        "Lima, Miraflores",
        "Lima, Surco"
    };
    private static final String[] PRECIOS = {
        "S/ 320,000",
        "S/ 450,000",
        "S/ 580,000"
    };
    private static final String[] ESTADOS = {"En Venta", "Preventa", "En Planos"};
    private static final String[] RATINGS = {"4.9", "4.8", "4.7"};
    private static final int[] PLACEHOLDER_COLORS = {
        Color.parseColor("#B8C8D4"),
        Color.parseColor("#D4B896"),
        Color.parseColor("#A8C8A0")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proyecto_detalle);
        int index = getIntent().getIntExtra(EXTRA_PROYECTO_INDEX, 0);
        bindData(index);
        setupBackButton();
        setupBottomNav();
    }

    private void bindData(int index) {
        ((TextView) findViewById(R.id.tv_nombre)).setText(NOMBRES[index]);
        ((TextView) findViewById(R.id.tv_ciudad)).setText(CIUDADES[index]);
        ((TextView) findViewById(R.id.tv_precio)).setText(PRECIOS[index]);
        ((TextView) findViewById(R.id.tv_rating)).setText(RATINGS[index]);

        View vPlaceholder = findViewById(R.id.v_placeholder);
        vPlaceholder.setBackgroundColor(PLACEHOLDER_COLORS[index]);

        TextView tvEstado = findViewById(R.id.tv_estado);
        tvEstado.setText(ESTADOS[index]);
        switch (ESTADOS[index]) {
            case "En Venta":
                tvEstado.setBackgroundResource(R.drawable.badge_en_venta);
                tvEstado.setTextColor(Color.parseColor("#186A3B"));
                break;
            case "Preventa":
                tvEstado.setBackgroundResource(R.drawable.badge_preventa);
                tvEstado.setTextColor(Color.parseColor("#9A5700"));
                break;
            default:
                tvEstado.setBackgroundResource(R.drawable.badge_en_planos);
                tvEstado.setTextColor(Color.parseColor("#1A5799"));
                break;
        }
    }

    private void setupBackButton() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_inicio);
    }
}
