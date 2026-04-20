package com.example.bitbusters.activities.cliente;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class AgendaCitaActivity extends AppCompatActivity {

    private TextView tvFechaSeleccionada;
    private TextView horaSeleccionada = null;
    private TextView[] horas;

    // Días disponibles del mes actual (simulado - en Lab 6 vendrá de Firebase)
    // Formato: día del mes que tiene disponibilidad
    private final Set<Integer> diasDisponibles = new HashSet<>(Arrays.asList(
            2, 3, 5, 8, 9, 10, 15, 16, 17, 22, 23, 24, 29, 30
    ));

    // Días ocupados (ya tienen cita agendada)
    private final Set<Integer> diasOcupados = new HashSet<>(Arrays.asList(
            1, 4, 6, 7, 11, 12, 13, 18, 19, 20, 21, 25, 26, 27, 28
    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda_cita);

        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada);

        horas = new TextView[]{
                findViewById(R.id.hora900),
                findViewById(R.id.hora1000),
                findViewById(R.id.hora1100),
                findViewById(R.id.hora1200),
                findViewById(R.id.hora200),
                findViewById(R.id.hora400)
        };

        // Botón volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Abrir calendario al tocar la card de fecha
        findViewById(R.id.cardFecha).setOnClickListener(v -> abrirCalendario());

        // Selección de hora
        for (TextView hora : horas) {
            hora.setOnClickListener(v -> seleccionarHora((TextView) v));
        }

        // Tarjetas de pago
        findViewById(R.id.cardTarjeta1).setOnClickListener(v ->
                Toast.makeText(this, "Tarjeta •••• 1222 seleccionada", Toast.LENGTH_SHORT).show());
        findViewById(R.id.cardTarjeta2).setOnClickListener(v ->
                Toast.makeText(this, "Tarjeta •••• 1542 seleccionada", Toast.LENGTH_SHORT).show());

        // Botón Next
        findViewById(R.id.btnNext).setOnClickListener(v -> confirmarCita());
        findViewById(R.id.btnNextArrow).setOnClickListener(v -> confirmarCita());
    }

    private void abrirCalendario() {
        Calendar hoy = Calendar.getInstance();
        int anio  = hoy.get(Calendar.YEAR);
        int mes   = hoy.get(Calendar.MONTH);
        int dia   = hoy.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {

                    // Verificar si el día seleccionado tiene disponibilidad
                    if (diasOcupados.contains(dayOfMonth)) {
                        Toast.makeText(this,
                                "Este día no tiene disponibilidad. Elige otro.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!diasDisponibles.contains(dayOfMonth)) {
                        Toast.makeText(this,
                                "Este día no está disponible para citas.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Día válido — mostrar fecha seleccionada
                    String[] meses = {"Ene","Feb","Mar","Abr","May","Jun",
                            "Jul","Ago","Sep","Oct","Nov","Dic"};
                    String fechaTexto = dayOfMonth + " " + meses[month] + " " + year;
                    tvFechaSeleccionada.setText(fechaTexto);
                    tvFechaSeleccionada.setTextColor(
                            getResources().getColor(android.R.color.black, getTheme()));

                }, anio, mes, dia);

        // No permitir fechas pasadas
        dialog.getDatePicker().setMinDate(hoy.getTimeInMillis());

        // Título del calendario
        dialog.setTitle("Selecciona una fecha disponible");
        dialog.show();
    }

    private void seleccionarHora(TextView horaView) {
        // Resetear todas las horas al estilo normal
        for (TextView h : horas) {
            h.setBackgroundResource(R.drawable.bg_hora_normal);
            h.setTextColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
        }

        // Marcar la hora seleccionada
        horaView.setBackgroundResource(R.drawable.bg_hora_selected);
        horaView.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
        horaSeleccionada = horaView;
    }

    private void confirmarCita() {
        if (tvFechaSeleccionada.getText().toString().equals("Agenda tu cita")) {
            Toast.makeText(this, "Por favor selecciona una fecha", Toast.LENGTH_SHORT).show();
            return;
        }
        if (horaSeleccionada == null) {
            Toast.makeText(this, "Por favor selecciona una hora", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bottom sheet de éxito
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setPadding(60, 40, 60, 60);
        layout.setBackgroundColor(android.graphics.Color.WHITE);

        // Ícono check verde con halo
        android.widget.FrameLayout halo = new android.widget.FrameLayout(this);
        android.widget.LinearLayout.LayoutParams haloParams =
                new android.widget.LinearLayout.LayoutParams(180, 180);
        haloParams.gravity = android.view.Gravity.CENTER;
        haloParams.bottomMargin = 40;
        halo.setLayoutParams(haloParams);
        halo.setBackground(new android.graphics.drawable.GradientDrawable() {{
            setShape(android.graphics.drawable.GradientDrawable.OVAL);
            setColor(0x224CAF50); // verde transparente (halo)
        }});

        android.widget.TextView check = new android.widget.TextView(this);
        android.widget.FrameLayout.LayoutParams checkParams =
                new android.widget.FrameLayout.LayoutParams(110, 110);
        checkParams.gravity = android.view.Gravity.CENTER;
        check.setLayoutParams(checkParams);
        check.setText("✓");
        check.setTextSize(32f);
        check.setTextColor(android.graphics.Color.WHITE);
        check.setGravity(android.view.Gravity.CENTER);
        check.setBackground(new android.graphics.drawable.GradientDrawable() {{
            setShape(android.graphics.drawable.GradientDrawable.OVAL);
            setColor(0xFF4CAF50); // verde sólido
        }});
        halo.addView(check);
        layout.addView(halo);

        // Título
        android.widget.TextView titulo = new android.widget.TextView(this);
        titulo.setText("Tu transición ha sido");
        titulo.setTextSize(20f);
        titulo.setTextColor(0xFF1A1A2E);
        titulo.setGravity(android.view.Gravity.CENTER);
        layout.addView(titulo);

        // Subtítulo bold
        android.widget.TextView subtitulo = new android.widget.TextView(this);
        subtitulo.setText("satisfactoria");
        subtitulo.setTextSize(20f);
        subtitulo.setTextColor(0xFF1A1A2E);
        subtitulo.setTypeface(null, android.graphics.Typeface.BOLD);
        subtitulo.setGravity(android.view.Gravity.CENTER);
        android.widget.LinearLayout.LayoutParams subParams =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        subParams.bottomMargin = 16;
        subtitulo.setLayoutParams(subParams);
        layout.addView(subtitulo);

        // Descripción
        android.widget.TextView descripcion = new android.widget.TextView(this);
        descripcion.setText("Gracias por la confianza");
        descripcion.setTextSize(13f);
        descripcion.setTextColor(0xFF9E9E9E);
        descripcion.setGravity(android.view.Gravity.CENTER);
        android.widget.LinearLayout.LayoutParams descParams =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        descParams.bottomMargin = 40;
        descripcion.setLayoutParams(descParams);
        layout.addView(descripcion);

        // Botón Continua Explorando
        android.widget.TextView btnContinuar = new android.widget.TextView(this);
        android.widget.LinearLayout.LayoutParams btnParams =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 130);
        btnContinuar.setLayoutParams(btnParams);
        btnContinuar.setText("Continua Explorando");
        btnContinuar.setTextSize(15f);
        btnContinuar.setTextColor(android.graphics.Color.WHITE);
        btnContinuar.setTypeface(null, android.graphics.Typeface.BOLD);
        btnContinuar.setGravity(android.view.Gravity.CENTER);
        btnContinuar.setBackground(new android.graphics.drawable.GradientDrawable() {{
            setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            setCornerRadius(60f);
            setColor(0xFF4CAF50);
        }});
        btnContinuar.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new android.content.Intent(this, HomeActivity.class));
            finish();
        });
        layout.addView(btnContinuar);

        dialog.setContentView(layout);
        dialog.show();
    }
}