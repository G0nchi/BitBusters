package com.example.bitbusters.activities.asesor;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.R;
import com.google.android.material.card.MaterialCardView;

public class ReagendarCitaActivity extends AppCompatActivity {

    public static final String EXTRA_NOMBRE   = "extra_nombre";
    public static final String EXTRA_PROYECTO = "extra_proyecto";
    public static final String EXTRA_FECHA    = "extra_fecha";
    public static final String EXTRA_HORA     = "extra_hora";
    public static final String EXTRA_INITIALS = "extra_initials";
    public static final String EXTRA_AVATAR_COLOR = "extra_avatar_color";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reagendar_cita);

        // Rellenar con los datos recibidos desde la cita
        String nombre   = getIntent().getStringExtra(EXTRA_NOMBRE);
        String proyecto = getIntent().getStringExtra(EXTRA_PROYECTO);
        String fecha    = getIntent().getStringExtra(EXTRA_FECHA);
        String hora     = getIntent().getStringExtra(EXTRA_HORA);
        String initials = getIntent().getStringExtra(EXTRA_INITIALS);
        int avatarColor = getIntent().getIntExtra(EXTRA_AVATAR_COLOR, Color.parseColor("#4ECDC4"));

        if (nombre   != null) ((android.widget.TextView) findViewById(R.id.tv_nombre)).setText(nombre);
        if (proyecto != null) ((android.widget.TextView) findViewById(R.id.tv_proyecto)).setText(proyecto);
        if (fecha    != null) ((android.widget.TextView) findViewById(R.id.tv_fecha_actual)).setText(fecha);
        if (hora     != null) ((android.widget.TextView) findViewById(R.id.tv_hora_actual)).setText(hora);
        if (initials != null) ((android.widget.TextView) findViewById(R.id.tv_initials)).setText(initials);
        ((MaterialCardView) findViewById(R.id.cv_avatar)).setCardBackgroundColor(avatarColor);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_confirmar).setOnClickListener(v -> {
            String nuevaFecha = ((android.widget.EditText) findViewById(R.id.et_fecha)).getText().toString().trim();
            String nuevaHora  = ((android.widget.EditText) findViewById(R.id.et_hora)).getText().toString().trim();
            if (nuevaFecha.isEmpty() || nuevaHora.isEmpty()) {
                Toast.makeText(this, "Completa la nueva fecha y hora", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Cita reagendada para el " + nuevaFecha + " a las " + nuevaHora, Toast.LENGTH_LONG).show();
            finish();
        });
    }
}
