package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.R;
import com.google.android.material.card.MaterialCardView;

public class VerDetalleCitaActivity extends AppCompatActivity {

    public static final String EXTRA_NOMBRE        = "extra_nombre";
    public static final String EXTRA_PROYECTO      = "extra_proyecto";
    public static final String EXTRA_FECHA         = "extra_fecha";
    public static final String EXTRA_HORA          = "extra_hora";
    public static final String EXTRA_BADGE         = "extra_badge";
    public static final String EXTRA_INITIALS      = "extra_initials";
    public static final String EXTRA_AVATAR_COLOR  = "extra_avatar_color";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_detalle_cita);

        String nombre   = getIntent().getStringExtra(EXTRA_NOMBRE);
        String proyecto = getIntent().getStringExtra(EXTRA_PROYECTO);
        String fecha    = getIntent().getStringExtra(EXTRA_FECHA);
        String hora     = getIntent().getStringExtra(EXTRA_HORA);
        String badge    = getIntent().getStringExtra(EXTRA_BADGE);
        String initials = getIntent().getStringExtra(EXTRA_INITIALS);
        int avatarColor = getIntent().getIntExtra(EXTRA_AVATAR_COLOR, Color.parseColor("#4ECDC4"));

        if (nombre != null) {
            ((TextView) findViewById(R.id.tv_nombre)).setText(nombre);
            // Iniciales del nombre en el proyecto de la cita
        }
        if (proyecto != null) {
            ((TextView) findViewById(R.id.tv_proyecto)).setText(proyecto);
            // Separar proyecto y unidad para mostrar en la card de proyecto
            String[] parts = proyecto.split(" · ");
            if (parts.length >= 2) {
                ((TextView) findViewById(R.id.tv_proyecto_nombre)).setText(parts[0]);
                ((TextView) findViewById(R.id.tv_unidad)).setText(parts[1]);
            } else {
                ((TextView) findViewById(R.id.tv_proyecto_nombre)).setText(proyecto);
            }
        }
        if (fecha != null) ((TextView) findViewById(R.id.tv_fecha)).setText(fecha);
        if (hora  != null) ((TextView) findViewById(R.id.tv_hora)).setText(hora);
        if (initials != null) ((TextView) findViewById(R.id.tv_initials)).setText(initials);
        ((MaterialCardView) findViewById(R.id.cv_avatar)).setCardBackgroundColor(avatarColor);

        // Badge de estado en el header
        if (badge != null) {
            TextView tvBadge = findViewById(R.id.tv_badge_header);
            tvBadge.setText(badge);
            switch (badge) {
                case "Pendiente":
                    tvBadge.setBackgroundResource(R.drawable.badge_pendiente);
                    tvBadge.setTextColor(Color.parseColor("#9A5700"));
                    break;
                case "Confirmada":
                    tvBadge.setBackgroundResource(R.drawable.badge_confirmada);
                    tvBadge.setTextColor(Color.parseColor("#186A3B"));
                    break;
                case "Cancelada":
                    tvBadge.setBackgroundResource(R.drawable.badge_cancelada);
                    tvBadge.setTextColor(Color.parseColor("#CC2222"));
                    break;
                default:
                    tvBadge.setBackgroundResource(R.drawable.badge_pasada);
                    tvBadge.setTextColor(Color.parseColor("#666666"));
                    break;
            }
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Reagendar desde el detalle
        findViewById(R.id.btn_reagendar).setOnClickListener(v -> {
            Intent intent = new Intent(this, ReagendarCitaActivity.class);
            intent.putExtra(ReagendarCitaActivity.EXTRA_NOMBRE, nombre);
            intent.putExtra(ReagendarCitaActivity.EXTRA_PROYECTO, proyecto);
            intent.putExtra(ReagendarCitaActivity.EXTRA_FECHA, fecha);
            intent.putExtra(ReagendarCitaActivity.EXTRA_HORA, hora);
            intent.putExtra(ReagendarCitaActivity.EXTRA_INITIALS, initials);
            intent.putExtra(ReagendarCitaActivity.EXTRA_AVATAR_COLOR, avatarColor);
            startActivity(intent);
        });

        // Separar desde el detalle
        findViewById(R.id.btn_separar).setOnClickListener(v ->
            startActivity(new Intent(this, NuevaSeparacionActivity.class)));
    }
}
