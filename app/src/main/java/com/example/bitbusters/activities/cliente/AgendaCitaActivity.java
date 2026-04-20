package com.example.bitbusters.activities.cliente;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.bitbusters.R;
import com.example.bitbusters.utils.ImageUrls;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class AgendaCitaActivity extends AppCompatActivity {

    private static final String EXTRA_PROYECTO = "proyecto";
    private TextView tvFechaSeleccionada;
    private TextView horaSeleccionada = null;
    private TextView[] horas;

    private final Set<Integer> diasDisponibles = new HashSet<>(Arrays.asList(
            2, 3, 5, 8, 9, 10, 15, 16, 17, 22, 23, 24, 29, 30
    ));

    private final Set<Integer> diasOcupados = new HashSet<>(Arrays.asList(
            1, 4, 6, 7, 11, 12, 13, 18, 19, 20, 21, 25, 26, 27, 28
    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda_cita);

        // Recibir datos del proyecto
        String nombreProyecto = getIntent().getStringExtra(EXTRA_PROYECTO);
        if (nombreProyecto != null) {
            ((TextView) findViewById(R.id.tvNombreProyecto)).setText(nombreProyecto);
            ImageView imgProyecto = findViewById(R.id.imgProyecto);
            int imageRes = obtenerImagenProyecto(nombreProyecto);
            Glide.with(this).load(imageRes).into(imgProyecto);
        }

        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada);

        horas = new TextView[]{
                findViewById(R.id.hora900),
                findViewById(R.id.hora1000),
                findViewById(R.id.hora1100),
                findViewById(R.id.hora1200),
                findViewById(R.id.hora200),
                findViewById(R.id.hora400)
        };

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.cardFecha).setOnClickListener(v -> abrirCalendario());

        for (TextView hora : horas) {
            hora.setOnClickListener(v -> seleccionarHora((TextView) v));
        }

        findViewById(R.id.cardTarjeta1).setOnClickListener(v ->
                Toast.makeText(this, "Tarjeta •••• 1222 seleccionada", Toast.LENGTH_SHORT).show());
        findViewById(R.id.cardTarjeta2).setOnClickListener(v ->
                Toast.makeText(this, "Tarjeta •••• 1542 seleccionada", Toast.LENGTH_SHORT).show());

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
                    if (diasOcupados.contains(dayOfMonth)) {
                        Toast.makeText(this, "Este día no tiene disponibilidad. Elige otro.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!diasDisponibles.contains(dayOfMonth)) {
                        Toast.makeText(this, "Este día no está disponible para citas.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String[] meses = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
                    String fechaTexto = dayOfMonth + " " + meses[month] + " " + year;
                    tvFechaSeleccionada.setText(fechaTexto);
                    tvFechaSeleccionada.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
                }, anio, mes, dia);

        dialog.getDatePicker().setMinDate(hoy.getTimeInMillis());
        dialog.setTitle("Selecciona una fecha disponible");
        dialog.show();
    }

    private void seleccionarHora(TextView horaView) {
        for (TextView h : horas) {
            h.setBackgroundResource(R.drawable.bg_hora_normal);
            h.setTextColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
        }
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

        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setPadding(60, 40, 60, 60);
        layout.setBackgroundColor(android.graphics.Color.WHITE);

        android.widget.FrameLayout halo = new android.widget.FrameLayout(this);
        android.widget.LinearLayout.LayoutParams haloParams = new android.widget.LinearLayout.LayoutParams(180, 180);
        haloParams.gravity = android.view.Gravity.CENTER;
        haloParams.bottomMargin = 40;
        halo.setLayoutParams(haloParams);
        halo.setBackground(new android.graphics.drawable.GradientDrawable() {{
            setShape(android.graphics.drawable.GradientDrawable.OVAL);
            setColor(0x224CAF50);
        }});

        android.widget.TextView check = new android.widget.TextView(this);
        android.widget.FrameLayout.LayoutParams checkParams = new android.widget.FrameLayout.LayoutParams(110, 110);
        checkParams.gravity = android.view.Gravity.CENTER;
        check.setLayoutParams(checkParams);
        check.setText("✓");
        check.setTextSize(32f);
        check.setTextColor(android.graphics.Color.WHITE);
        check.setGravity(android.view.Gravity.CENTER);
        check.setBackground(new android.graphics.drawable.GradientDrawable() {{
            setShape(android.graphics.drawable.GradientDrawable.OVAL);
            setColor(0xFF4CAF50);
        }});
        halo.addView(check);
        layout.addView(halo);

        android.widget.TextView titulo = new android.widget.TextView(this);
        titulo.setText("Tu cita ha sido agendada");
        titulo.setTextSize(20f);
        titulo.setTextColor(0xFF1A1A2E);
        titulo.setGravity(android.view.Gravity.CENTER);
        layout.addView(titulo);

        android.widget.TextView subtitulo = new android.widget.TextView(this);
        subtitulo.setText("con éxito");
        subtitulo.setTextSize(20f);
        subtitulo.setTextColor(0xFF1A1A2E);
        subtitulo.setTypeface(null, android.graphics.Typeface.BOLD);
        subtitulo.setGravity(android.view.Gravity.CENTER);
        android.widget.LinearLayout.LayoutParams subParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        subParams.bottomMargin = 16;
        subtitulo.setLayoutParams(subParams);
        layout.addView(subtitulo);

        android.widget.TextView descripcion = new android.widget.TextView(this);
        descripcion.setText("Podrás ver los detalles en Mis Citas");
        descripcion.setTextSize(13f);
        descripcion.setTextColor(0xFF9E9E9E);
        descripcion.setGravity(android.view.Gravity.CENTER);
        android.widget.LinearLayout.LayoutParams descParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        descParams.bottomMargin = 40;
        descripcion.setLayoutParams(descParams);
        layout.addView(descripcion);

        android.widget.TextView btnContinuar = new android.widget.TextView(this);
        android.widget.LinearLayout.LayoutParams btnParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 130);
        btnContinuar.setLayoutParams(btnParams);
        btnContinuar.setText("Aceptar");
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
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        layout.addView(btnContinuar);

        dialog.setContentView(layout);
        dialog.show();
    }

    private int obtenerImagenProyecto(String nombreProyecto) {
        if (nombreProyecto == null) return ImageUrls.HERO_TORRES_UNIDAS;
        switch (nombreProyecto) {
            case "Catalina Ventor": return ImageUrls.PROYECTO_CATALINA_VENTOR;
            case "Residencial Park":
            case "Residencial El Park": return ImageUrls.PROYECTO_RESIDENCIAL_PARK;
            case "Torre Miramar": return ImageUrls.PROYECTO_TORRE_MIRAMAR;
            case "Condominio Las Lomas": return ImageUrls.PROYECTO_CONDOMINIO_LOMAS;
            case "Catalina Sky": return ImageUrls.PROYECTO_CATALINA_SKY;
            default: return ImageUrls.HERO_TORRES_UNIDAS;
        }
    }
}