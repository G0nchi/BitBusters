package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.R;
import com.example.bitbusters.databinding.ActivityVerDetalleCitaBinding;

public class VerDetalleCitaActivity extends AppCompatActivity {

    public static final String EXTRA_NOMBRE        = "extra_nombre";
    public static final String EXTRA_PROYECTO      = "extra_proyecto";
    public static final String EXTRA_FECHA         = "extra_fecha";
    public static final String EXTRA_HORA          = "extra_hora";
    public static final String EXTRA_BADGE         = "extra_badge";
    public static final String EXTRA_INITIALS      = "extra_initials";
    public static final String EXTRA_AVATAR_COLOR  = "extra_avatar_color";

    private ActivityVerDetalleCitaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerDetalleCitaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String nombre   = getIntent().getStringExtra(EXTRA_NOMBRE);
        String proyecto = getIntent().getStringExtra(EXTRA_PROYECTO);
        String fecha    = getIntent().getStringExtra(EXTRA_FECHA);
        String hora     = getIntent().getStringExtra(EXTRA_HORA);
        String badge    = getIntent().getStringExtra(EXTRA_BADGE);
        String initials = getIntent().getStringExtra(EXTRA_INITIALS);
        int avatarColor = getIntent().getIntExtra(EXTRA_AVATAR_COLOR, Color.parseColor("#4ECDC4"));

        if (nombre != null) {
            binding.tvNombre.setText(nombre);
        }
        if (proyecto != null) {
            binding.tvProyecto.setText(proyecto);
            // Separar proyecto y unidad para mostrar en la card de proyecto
            String[] parts = proyecto.split(" · ");
            if (parts.length >= 2) {
                binding.tvProyectoNombre.setText(parts[0]);
                binding.tvUnidad.setText(parts[1]);
            } else {
                binding.tvProyectoNombre.setText(proyecto);
            }
        }
        if (fecha != null) binding.tvFecha.setText(fecha);
        if (hora  != null) binding.tvHora.setText(hora);
        if (initials != null) binding.tvInitials.setText(initials);
        binding.cvAvatar.setCardBackgroundColor(avatarColor);

        // Badge de estado en el header
        if (badge != null) {
            binding.tvBadgeHeader.setText(badge);
            switch (badge) {
                case "Pendiente":
                    binding.tvBadgeHeader.setBackgroundResource(R.drawable.badge_pendiente);
                    binding.tvBadgeHeader.setTextColor(Color.parseColor("#9A5700"));
                    break;
                case "Confirmada":
                    binding.tvBadgeHeader.setBackgroundResource(R.drawable.badge_confirmada);
                    binding.tvBadgeHeader.setTextColor(Color.parseColor("#186A3B"));
                    break;
                case "Cancelada":
                    binding.tvBadgeHeader.setBackgroundResource(R.drawable.badge_cancelada);
                    binding.tvBadgeHeader.setTextColor(Color.parseColor("#CC2222"));
                    break;
                default:
                    binding.tvBadgeHeader.setBackgroundResource(R.drawable.badge_pasada);
                    binding.tvBadgeHeader.setTextColor(Color.parseColor("#666666"));
                    break;
            }
        }

        binding.btnBack.setOnClickListener(v -> finish());

        // Reagendar desde el detalle
        binding.btnReagendar.setOnClickListener(v -> {
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
        binding.btnSeparar.setOnClickListener(v ->
            startActivity(new Intent(this, NuevaSeparacionActivity.class)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
