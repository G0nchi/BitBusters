package com.example.bitbusters.activities.asesor;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.databinding.ActivityReagendarCitaBinding;
import com.example.bitbusters.repository.CitaRepository;
import com.example.bitbusters.utils.AsesorNotificationHelper;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ReagendarCitaActivity extends AppCompatActivity {

    public static final String EXTRA_NOMBRE       = "extra_nombre";
    public static final String EXTRA_PROYECTO     = "extra_proyecto";
    public static final String EXTRA_FECHA        = "extra_fecha";
    public static final String EXTRA_HORA         = "extra_hora";
    public static final String EXTRA_INITIALS     = "extra_initials";
    public static final String EXTRA_AVATAR_COLOR = "extra_avatar_color";
    public static final String EXTRA_CITA_ID       = "extra_cita_id";
    public static final String EXTRA_SLOT_ID       = "extra_slot_id";
    public static final String EXTRA_PROYECTO_ID   = "extra_proyecto_id";
    public static final String EXTRA_UID_CLIENTE   = "extra_uid_cliente";

    /** Código 24h → etiqueta visible. Mismos horarios fijos que usa AgendaCitaActivity (Cliente). */
    private static final Map<String, String> HORAS_DISPONIBLES = new LinkedHashMap<>();
    static {
        HORAS_DISPONIBLES.put("0900", "9:00 AM");
        HORAS_DISPONIBLES.put("1000", "10:00 AM");
        HORAS_DISPONIBLES.put("1100", "11:00 AM");
        HORAS_DISPONIBLES.put("1200", "12:00 PM");
        HORAS_DISPONIBLES.put("1400", "2:00 PM");
        HORAS_DISPONIBLES.put("1600", "4:00 PM");
    }

    private final CitaRepository citaRepository = new CitaRepository();

    private ActivityReagendarCitaBinding binding;

    private String citaId, slotIdAnterior, proyectoId, uidCliente;
    private Calendar fechaElegida;
    private String hora24Elegida;

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

        citaId         = getIntent().getStringExtra(EXTRA_CITA_ID);
        slotIdAnterior = getIntent().getStringExtra(EXTRA_SLOT_ID);
        proyectoId     = getIntent().getStringExtra(EXTRA_PROYECTO_ID);
        uidCliente     = getIntent().getStringExtra(EXTRA_UID_CLIENTE);

        if (nombre   != null) binding.tvNombre.setText(nombre);
        if (proyecto != null) binding.tvProyecto.setText(proyecto);
        if (fecha    != null) binding.tvFechaActual.setText(fecha);
        if (hora     != null) binding.tvHoraActual.setText(hora);
        if (initials != null) binding.tvInitials.setText(initials);
        binding.cvAvatar.setCardBackgroundColor(avatarColor);

        binding.etFecha.setOnClickListener(v -> showDatePicker());
        binding.etHora.setOnClickListener(v  -> showHoraPicker());

        binding.btnBack.setOnClickListener(v -> finish());

        final String clienteNombre = nombre != null ? nombre : "el cliente";
        binding.btnConfirmar.setOnClickListener(v -> confirmarReagendado(clienteNombre));
    }

    private void confirmarReagendado(String clienteNombre) {
        if (fechaElegida == null || hora24Elegida == null) {
            Toast.makeText(this, "Selecciona la nueva fecha y hora", Toast.LENGTH_SHORT).show();
            return;
        }
        if (citaId == null || citaId.isEmpty() || proyectoId == null || proyectoId.isEmpty()) {
            Toast.makeText(this, "No se pudo identificar la cita a reagendar", Toast.LENGTH_SHORT).show();
            return;
        }

        int year  = fechaElegida.get(Calendar.YEAR);
        int month = fechaElegida.get(Calendar.MONTH);
        int day   = fechaElegida.get(Calendar.DAY_OF_MONTH);

        String nuevoSlotId = CitaRepository.generarSlotId(proyectoId, year, month + 1, day, hora24Elegida);
        java.util.Date nuevaFecha = CitaRepository.calcularFechaTimestamp(year, month, day, hora24Elegida);

        binding.btnConfirmar.setEnabled(false);
        citaRepository.reagendarCita(citaId, slotIdAnterior, nuevoSlotId, nuevaFecha)
            .addOnSuccessListener(unused -> {
                AsesorNotificationHelper.showCitaReagendada(
                    this, clienteNombre,
                    binding.etFecha.getText() + " a las " + binding.etHora.getText());
                Toast.makeText(this,
                    "Cita reagendada para el " + binding.etFecha.getText()
                        + " a las " + binding.etHora.getText(),
                    Toast.LENGTH_LONG).show();
                finish();
            })
            .addOnFailureListener(e -> {
                binding.btnConfirmar.setEnabled(true);
                String mensaje = e.getMessage() != null && e.getMessage().contains("no disponible")
                    ? "Ese horario ya está ocupado, elige otro"
                    : "No se pudo reagendar la cita";
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
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
                fechaElegida = Calendar.getInstance();
                fechaElegida.set(y, m, d, 0, 0, 0);

                String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                                  "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
                String[] dias  = {"Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};
                String diaSem   = dias[fechaElegida.get(Calendar.DAY_OF_WEEK) - 1];
                String fechaFmt = String.format(Locale.getDefault(),
                    "%s %d %s, %d", diaSem, d, meses[m], y);
                binding.etFecha.setText(fechaFmt);
            }, anio, mes, dia);

        // No permitir fechas pasadas
        dialog.getDatePicker().setMinDate(hoy.getTimeInMillis());
        dialog.show();
    }

    // ── Selector de horario fijo ─────────────────────────────────────────────────

    private void showHoraPicker() {
        String[] codigos = HORAS_DISPONIBLES.keySet().toArray(new String[0]);
        String[] etiquetas = HORAS_DISPONIBLES.values().toArray(new String[0]);

        new AlertDialog.Builder(this)
            .setTitle("Selecciona un horario")
            .setItems(etiquetas, (dialog, which) -> {
                hora24Elegida = codigos[which];
                binding.etHora.setText(etiquetas[which]);
            })
            .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
