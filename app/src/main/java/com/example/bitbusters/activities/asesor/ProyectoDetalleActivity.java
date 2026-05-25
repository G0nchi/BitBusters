package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.R;
import com.example.bitbusters.databinding.ActivityProyectoDetalleBinding;

public class ProyectoDetalleActivity extends AppCompatActivity {

    public static final String EXTRA_PROYECTO_INDEX = "proyecto_index";

    private ActivityProyectoDetalleBinding binding;

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
        binding = ActivityProyectoDetalleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        int index = getIntent().getIntExtra(EXTRA_PROYECTO_INDEX, 0);
        bindData(index);
        setupBackButton();
        setupActionButtons();
        setupBottomNav();
    }

    private void setupActionButtons() {
        binding.btnRegistrar.setOnClickListener(v ->
            startActivity(new Intent(this, NuevaSeparacionActivity.class)));
        binding.btnContactar.setOnClickListener(v ->
            new ContactarClienteBottomSheet().show(getSupportFragmentManager(), "contactar"));
    }

    private void bindData(int index) {
        binding.tvNombre.setText(NOMBRES[index]);
        binding.tvCiudad.setText(CIUDADES[index]);
        binding.tvPrecio.setText(PRECIOS[index]);
        binding.tvRating.setText(RATINGS[index]);

        binding.vPlaceholder.setBackgroundColor(PLACEHOLDER_COLORS[index]);

        binding.tvEstado.setText(ESTADOS[index]);
        switch (ESTADOS[index]) {
            case "En Venta":
                binding.tvEstado.setBackgroundResource(R.drawable.badge_en_venta);
                binding.tvEstado.setTextColor(Color.parseColor("#186A3B"));
                break;
            case "Preventa":
                binding.tvEstado.setBackgroundResource(R.drawable.badge_preventa);
                binding.tvEstado.setTextColor(Color.parseColor("#9A5700"));
                break;
            default:
                binding.tvEstado.setBackgroundResource(R.drawable.badge_en_planos);
                binding.tvEstado.setTextColor(Color.parseColor("#1A5799"));
                break;
        }
    }

    private void setupBackButton() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_inicio);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_citas) {
                startActivity(new Intent(this, CitasAgendadasActivity.class));
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, MensajesActivity.class));
            } else if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, AsesorHomeActivity.class));
                finish();
            }
            return true;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
