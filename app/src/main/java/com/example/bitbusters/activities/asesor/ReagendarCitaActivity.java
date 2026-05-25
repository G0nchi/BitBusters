package com.example.bitbusters.activities.asesor;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.databinding.ActivityReagendarCitaBinding;
import com.example.bitbusters.utils.AsesorNotificationHelper;

import java.util.Calendar;
import java.util.Locale;

public class ReagendarCitaActivity extends AppCompatActivity {

    public static final String EXTRA_NOMBRE       = "extra_nombre";
    public static final String EXTRA_PROYECTO     = "extra_proyecto";
    public static final String EXTRA_FECHA        = "extra_fecha";
    public static final String EXTRA_HORA         = "extra_hora";
    public static final String EXTRA_INITIALS     = "extra_initials";
    public static final String EXTRA_AVATAR_COLOR = "extra_avatar_color";

    private ActivityReagendarCitaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReagendarCitaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String nombre   = getIntent().getStringExtra(EXTRA_NOMBRE);
        String proyecto = getIntent().getStringExtra(EXTRA_PROYECTO);
        String fecha    = getIntent().getStringExtra(EXTRA_FECHA);
        String hora     = getIntent().getStringExtra(EXTRA_HORA);
        String initials = getIntent().getStringExtra(EXTRA_INITIALS);
        int avatarColor = getIntent().getIntExtra(EXTRA_AVATAR_COLOR, Color.parseColor("#4ECDC4"));

        if (nombre   != null) binding.tvNombre.setText(nombre);
        if (proyecto != null) binding.tvProyecto.setText(proyecto);
        if (fecha    != null) binding.tvFechaActual.setText(fecha);
        if (hora     != null) binding.tvHoraActual.setText(hora);
        if (initials != null) binding.tvInitials.setText(initials);
        binding.cvAvatar.setCardBackgroundColor(avatarColor);

        binding.etFecha.setOnClickListener(v -> showDatePicker());
        binding.etHora.setOnClickListener(v  -> showTimePicker());

        binding.btnBack.setOnClickListener(v -> finish());

        final String clienteNombre = nombre != null ? nombre : "el cliente";
        binding.btnConfirmar.setOnClickListener(v -> {
            String nuevaFecha = binding.etFecha.getText().toString().trim();
            String nuevaHora  = binding.etHora.getText().toString().trim();
            if (nuevaFecha.isEmpty() || nuevaHora.isEmpty()) {
                Toast.makeText(this, "Selecciona la nueva fecha y hora", Toast.LENGTH_SHORT).show();
                return;
            }
            AsesorNotificationHelper.showCitaReagendada(
                this, clienteNombre, nuevaFecha + " a las " + nuevaHora);
            Toast.makeText(this,
                "Cita reagendada para el " + nuevaFecha + " a las " + nuevaHora,
                Toast.LENGTH_LONG).show();
            finish();
        });
    }

    // ── Date picker ─────────────────────────────────────────────────────────────

    private void showDatePicker() {
        Calendar hoy = Calendar.getInstance();
        int anio = hoy.get(Calendar.YEAR);
        int mes  = hoy.get(Calendar.MONTH);
        int dia  = hoy.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
            (view, y, m, d) -> {
                String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                                  "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
                String[] dias  = {"Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};

                Calendar sel = Calendar.getInstance();
                sel.set(y, m, d);
                String diaSem = dias[sel.get(Calendar.DAY_OF_WEEK) - 1];
                String fechaFmt = String.format(Locale.getDefault(),
                    "%s %d %s, %d", diaSem, d, meses[m], y);
                binding.etFecha.setText(fechaFmt);
            }, anio, mes, dia);

        // No permitir fechas pasadas
        dialog.getDatePicker().setMinDate(hoy.getTimeInMillis());
        dialog.show();
    }

    // ── Time picker ─────────────────────────────────────────────────────────────

    private void showTimePicker() {
        Calendar ahora  = Calendar.getInstance();
        int hora   = ahora.get(Calendar.HOUR_OF_DAY);
        int minuto = ahora.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(this,
            (view, h, min) -> {
                String amPm   = h < 12 ? "AM" : "PM";
                int hora12    = h % 12;
                if (hora12 == 0) hora12 = 12;
                String horaFmt = String.format(Locale.getDefault(),
                    "%d:%02d %s", hora12, min, amPm);
                binding.etHora.setText(horaFmt);
            }, hora, minuto, false);   // false = formato 12 horas

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
