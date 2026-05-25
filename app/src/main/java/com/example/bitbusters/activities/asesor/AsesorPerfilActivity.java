package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bitbusters.R;
import com.example.bitbusters.databinding.ActivityAsesorPerfilBinding;

public class AsesorPerfilActivity extends AppCompatActivity {

    private ActivityAsesorPerfilBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAsesorPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupRecyclerView();
        setupBottomNav();
    }

    private void setupRecyclerView() {
        binding.rvSeparaciones.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSeparaciones.setNestedScrollingEnabled(false);
        binding.rvSeparaciones.setAdapter(new SeparacionAdapter());
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_perfil);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, AsesorHomeActivity.class));
                finish();
            } else if (id == R.id.nav_citas) {
                startActivity(new Intent(this, CitasAgendadasActivity.class));
                finish();
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, MensajesActivity.class));
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
