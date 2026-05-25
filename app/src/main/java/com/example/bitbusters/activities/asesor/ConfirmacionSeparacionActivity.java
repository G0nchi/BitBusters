package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.databinding.ActivityConfirmacionSeparacionBinding;
import com.example.bitbusters.utils.AsesorNotificationHelper;

public class ConfirmacionSeparacionActivity extends AppCompatActivity {

    public static final String EXTRA_CLIENTE  = "extra_cliente";
    public static final String EXTRA_PROYECTO = "extra_proyecto";
    public static final String EXTRA_MONTO    = "extra_monto";

    private ActivityConfirmacionSeparacionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfirmacionSeparacionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String cliente  = getIntent().getStringExtra(EXTRA_CLIENTE);
        String proyecto = getIntent().getStringExtra(EXTRA_PROYECTO);

        // Disparar notificación del sistema al confirmar la separación
        String clienteDisplay  = (cliente  != null && !cliente.isEmpty())  ? cliente  : "el cliente";
        String proyectoDisplay = (proyecto != null && !proyecto.isEmpty()) ? proyecto : "el proyecto";
        AsesorNotificationHelper.showNuevaSeparacion(this, clienteDisplay, proyectoDisplay);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(this, AsesorHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
