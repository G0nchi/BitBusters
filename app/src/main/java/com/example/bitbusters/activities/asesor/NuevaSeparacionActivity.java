package com.example.bitbusters.activities.asesor;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.databinding.ActivityNuevaSeparacionBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class NuevaSeparacionActivity extends AppCompatActivity {

    public static final String EXTRA_CLIENTE  = "extra_cliente";
    public static final String EXTRA_PROYECTO = "extra_proyecto";
    public static final String EXTRA_INITIALS = "extra_initials";
    public static final String EXTRA_COLOR    = "extra_color";

    private ActivityNuevaSeparacionBinding binding;

    private String clienteNombre;
    private String proyectoNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNuevaSeparacionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clienteNombre  = getIntent().getStringExtra(EXTRA_CLIENTE);
        proyectoNombre = getIntent().getStringExtra(EXTRA_PROYECTO);
        String initials = getIntent().getStringExtra(EXTRA_INITIALS);
        int    color    = getIntent().getIntExtra(EXTRA_COLOR, Color.parseColor("#4DB6AC"));

        bindHeader(proyectoNombre);
        bindClienteCard(clienteNombre, initials, color);

        binding.etSepFecha.setOnClickListener(v -> showDatePicker());
        binding.etSepHora.setOnClickListener(v  -> showTimePicker());

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnContinuar.setOnClickListener(v -> {
            String monto = binding.etSepValor != null
                ? binding.etSepValor.getText().toString().trim() : "";

            Intent intent = new Intent(this, PagoSeparacionActivity.class);
            intent.putExtra(PagoSeparacionActivity.EXTRA_CLIENTE,  clienteNombre);
            intent.putExtra(PagoSeparacionActivity.EXTRA_PROYECTO, proyectoNombre);
            intent.putExtra(PagoSeparacionActivity.EXTRA_MONTO,    monto.isEmpty() ? "0" : monto);
            startActivity(intent);
        });
    }

    // ── Selector de fecha ────────────────────────────────────────────────────────

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
                Calendar sel   = Calendar.getInstance();
                sel.set(y, m, d);
                String diaSem   = dias[sel.get(Calendar.DAY_OF_WEEK) - 1];
                String fechaFmt = String.format(Locale.getDefault(),
                    "%s %d %s, %d", diaSem, d, meses[m], y);
                binding.etSepFecha.setText(fechaFmt);
            }, anio, mes, dia);

        dialog.getDatePicker().setMinDate(hoy.getTimeInMillis());
        dialog.show();
    }

    // ── Selector de hora ─────────────────────────────────────────────────────────

    private void showTimePicker() {
        Calendar ahora = Calendar.getInstance();
        int h   = ahora.get(Calendar.HOUR_OF_DAY);
        int min = ahora.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(this,
            (view, hour, minute) -> {
                String amPm   = hour < 12 ? "AM" : "PM";
                int hora12    = hour % 12;
                if (hora12 == 0) hora12 = 12;
                String horaFmt = String.format(Locale.getDefault(),
                    "%d:%02d %s", hora12, minute, amPm);
                binding.etSepHora.setText(horaFmt);
            }, h, min, false);   // false = formato 12 horas

        dialog.show();
    }

    private void bindHeader(String proyecto) {
        if (binding.tvSepHeaderSubtitle != null && proyecto != null && !proyecto.isEmpty()) {
            binding.tvSepHeaderSubtitle.setText(proyecto);
        }
        if (binding.etSepProyectoValue != null && proyecto != null && !proyecto.isEmpty()) {
            binding.etSepProyectoValue.setText(proyecto);
        }
    }

    private void bindClienteCard(String nombre, String initials, int color) {
        if (binding.tvSepClienteNombre != null && nombre != null)
            binding.tvSepClienteNombre.setText(nombre);
        if (binding.tvSepClienteInitials != null && initials != null)
            binding.tvSepClienteInitials.setText(initials);
        if (binding.cvSepClienteAvatar != null)
            binding.cvSepClienteAvatar.setCardBackgroundColor(color);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
