package com.example.bitbusters.activities.asesor;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.databinding.ActivityValorarVisitaBinding;

public class ValorarVisitaActivity extends AppCompatActivity {

    private ActivityValorarVisitaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityValorarVisitaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnGuardar.setOnClickListener(v -> {
            Toast.makeText(this, "¡Valoración guardada!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
