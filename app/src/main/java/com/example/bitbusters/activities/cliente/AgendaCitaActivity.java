package com.example.bitbusters.activities.cliente;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bitbusters.R;
import com.example.bitbusters.repository.CitaRepository;
import com.example.bitbusters.utils.ImageUrls;
import com.example.bitbusters.utils.NotificationHelper;
import com.example.bitbusters.utils.PreferencesManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class AgendaCitaActivity extends AppCompatActivity {

    // Extras públicos para callers (ProjectDetailActivity, MisCitasActivity)
    static final String EXTRA_PROYECTO         = "proyecto";
    static final String EXTRA_PROYECTO_ID      = "proyectoId";
    static final String EXTRA_UID_ASESOR       = "uidAsesor";
    static final String EXTRA_MODO             = "modo";
    static final String EXTRA_CITA_ID          = "citaId";
    static final String EXTRA_SLOT_ID_ANTERIOR = "slotIdAnterior";

    private static final String MODO_REAGENDAR = "reagendar";
    private static final String TAG            = "AgendaCita";

    // Extras leídos en onCreate
    private String  proyectoNombre;
    private String  proyectoId;
    private String  uidAsesor;
    private boolean modoReagendar;
    private String  citaId;
    private String  slotIdAnterior;

    // Views
    private TextView tvFechaSeleccionada;
    private TextView btnNext;
    private View     btnNextArrow;
    // TV → código hora 24h ("0900", "1000", "1100", "1200", "1400", "1600")
    private final Map<TextView, String> horaViews = new LinkedHashMap<>();

    // Estado de selección
    private int      selectedYear, selectedMonth, selectedDay; // month = 0-based (Calendar.MONTH)
    private String   selectedHora24;
    private TextView selectedHoraView;

    // Firestore
    private final CitaRepository      citaRepository = new CitaRepository();
    private       ListenerRegistration slotListener;
    private int    listenerYear = -1, listenerMonth = -1;
    private final Set<String> slotsOcupados = new HashSet<>();

    // ── Ciclo de vida ──────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda_cita);
        leerExtras();
        inicializarVistas();
        configurarProyectoUI();
        NotificationHelper.crearCanal(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (proyectoId != null && !proyectoId.isEmpty()) {
            Calendar hoy = Calendar.getInstance(TimeZone.getTimeZone("America/Lima"));
            iniciarSlotListener(hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (slotListener != null) {
            slotListener.remove();
            slotListener = null;
        }
        listenerYear = listenerMonth = -1;
    }

    // ── Inicialización ─────────────────────────────────────────────────────────

    private void leerExtras() {
        Intent i    = getIntent();
        proyectoNombre = i.getStringExtra(EXTRA_PROYECTO);
        proyectoId     = i.getStringExtra(EXTRA_PROYECTO_ID);
        uidAsesor      = i.getStringExtra(EXTRA_UID_ASESOR);
        modoReagendar  = MODO_REAGENDAR.equals(i.getStringExtra(EXTRA_MODO));
        citaId         = i.getStringExtra(EXTRA_CITA_ID);
        slotIdAnterior = i.getStringExtra(EXTRA_SLOT_ID_ANTERIOR);
    }

    private void inicializarVistas() {
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada);
        btnNext             = findViewById(R.id.btnNext);
        btnNextArrow        = findViewById(R.id.btnNextArrow);

        // Mapear cada view de hora a su código 24h
        horaViews.put((TextView) findViewById(R.id.hora900),  "0900");
        horaViews.put((TextView) findViewById(R.id.hora1000), "1000");
        horaViews.put((TextView) findViewById(R.id.hora1100), "1100");
        horaViews.put((TextView) findViewById(R.id.hora1200), "1200");
        horaViews.put((TextView) findViewById(R.id.hora200),  "1400");
        horaViews.put((TextView) findViewById(R.id.hora400),  "1600");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.cardFecha).setOnClickListener(v -> abrirCalendario());

        for (Map.Entry<TextView, String> entry : horaViews.entrySet()) {
            entry.getKey().setOnClickListener(v -> seleccionarHora((TextView) v));
        }

        if (btnNext     != null) btnNext.setOnClickListener(v -> confirmarCita());
        if (btnNextArrow != null) btnNextArrow.setOnClickListener(v -> confirmarCita());

        View c1 = findViewById(R.id.cardTarjeta1);
        View c2 = findViewById(R.id.cardTarjeta2);
        if (c1 != null) c1.setOnClickListener(v ->
                Toast.makeText(this, "Tarjeta •••• 1222 seleccionada", Toast.LENGTH_SHORT).show());
        if (c2 != null) c2.setOnClickListener(v ->
                Toast.makeText(this, "Tarjeta •••• 1542 seleccionada", Toast.LENGTH_SHORT).show());
    }

    private void configurarProyectoUI() {
        if (proyectoNombre != null) {
            TextView tv = findViewById(R.id.tvNombreProyecto);
            if (tv != null) tv.setText(proyectoNombre);
            ImageView img = findViewById(R.id.imgProyecto);
            if (img != null) Glide.with(this).load(obtenerImagenProyecto(proyectoNombre)).into(img);
        }
        if (modoReagendar && btnNext != null) btnNext.setText("Reagendar");
    }

    // ── Slot listener ──────────────────────────────────────────────────────────

    private void iniciarSlotListener(int year, int month0based) {
        if (proyectoId == null || proyectoId.isEmpty()) return;
        if (year == listenerYear && month0based == listenerMonth) return;

        if (slotListener != null) slotListener.remove();
        listenerYear  = year;
        listenerMonth = month0based;

        Date inicio = CitaRepository.inicioDelMes(year, month0based);
        Date fin    = CitaRepository.inicioDelMesSiguiente(year, month0based);

        Log.d(TAG, "DIAG iniciarSlotListener: proyectoId=[" + proyectoId + "] year=" + year + " month0=" + month0based + " inicio=" + inicio + " fin=" + fin);
        slotListener = citaRepository.escucharSlotsOcupados(proyectoId, inicio, fin,
                new CitaRepository.SlotsOcupadosListener() {
                    @Override public void onSlotsActualizados(Set<String> slots) {
                        Log.d(TAG, "DIAG onSlotsActualizados: recibidos=" + slots.size() + " ids=" + slots);
                        slotsOcupados.clear();
                        slotsOcupados.addAll(slots);
                        actualizarDisponibilidadHoras();
                    }
                    @Override public void onError(String msg) {
                        Log.e(TAG, "DIAG ERROR slot listener: " + msg);
                    }
                });
    }

    // ── Calendario ─────────────────────────────────────────────────────────────

    private void abrirCalendario() {
        Calendar hoy = Calendar.getInstance(TimeZone.getTimeZone("America/Lima"));
        int anio = selectedYear  > 0  ? selectedYear  : hoy.get(Calendar.YEAR);
        int mes  = selectedDay   > 0  ? selectedMonth : hoy.get(Calendar.MONTH);
        int dia  = selectedDay   > 0  ? selectedDay   : hoy.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedYear  = year;
            selectedMonth = month;      // 0-based
            selectedDay   = dayOfMonth;

            String[] meses = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
            tvFechaSeleccionada.setText(dayOfMonth + " " + meses[month] + " " + year);
            tvFechaSeleccionada.setTextColor(
                    getResources().getColor(android.R.color.black, getTheme()));

            iniciarSlotListener(year, month);   // reinicia si cambia de mes
            actualizarDisponibilidadHoras();    // recalcula disponibilidad para el día elegido
        }, anio, mes, dia)
                .show();
    }

    // ── Selección de hora ──────────────────────────────────────────────────────

    private void seleccionarHora(TextView horaView) {
        if (!horaView.isEnabled()) {
            Toast.makeText(this, "Este horario ya está reservado", Toast.LENGTH_SHORT).show();
            return;
        }
        for (TextView tv : horaViews.keySet()) {
            if (tv.isEnabled()) {
                tv.setBackgroundResource(R.drawable.bg_hora_normal);
                tv.setTextColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
            }
        }
        horaView.setBackgroundResource(R.drawable.bg_hora_selected);
        horaView.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
        selectedHoraView = horaView;
        selectedHora24   = horaViews.get(horaView);
    }

    private void actualizarDisponibilidadHoras() {
        if (selectedYear == 0 || proyectoId == null) return;
        Log.d(TAG, "DIAG actualizarDisponibilidadHoras: fecha=" + selectedYear + "/" + (selectedMonth + 1) + "/" + selectedDay + " proyectoId=[" + proyectoId + "] slotsOcupados.total=" + slotsOcupados.size() + " contenido=" + slotsOcupados);
        for (Map.Entry<TextView, String> entry : horaViews.entrySet()) {
            TextView tv   = entry.getKey();
            String hora24 = entry.getValue();
            String slotId = CitaRepository.generarSlotId(
                    proyectoId, selectedYear, selectedMonth + 1, selectedDay, hora24);
            boolean ocupado = slotsOcupados.contains(slotId);
            Log.d(TAG, "DIAG cruce: generado=[" + slotId + "] match=" + ocupado);

            tv.setAlpha(1f);
            tv.setEnabled(!ocupado);
            if (ocupado) {
                tv.setBackgroundResource(R.drawable.bg_hora_ocupada);
                tv.setTextColor(getResources().getColor(R.color.colorTextHint, getTheme()));
                if (tv == selectedHoraView) {
                    selectedHoraView = null;
                    selectedHora24   = null;
                    Toast.makeText(this,
                            "El horario que seleccionaste acaba de ser reservado. Elige otro.",
                            Toast.LENGTH_LONG).show();
                }
            } else if (tv == selectedHoraView) {
                tv.setBackgroundResource(R.drawable.bg_hora_selected);
                tv.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
            } else {
                tv.setBackgroundResource(R.drawable.bg_hora_normal);
                tv.setTextColor(getResources().getColor(R.color.colorTextSecondary, getTheme()));
            }
        }
    }

    // ── Confirmación ───────────────────────────────────────────────────────────

    private void confirmarCita() {
        if (selectedYear == 0) {
            Toast.makeText(this, "Por favor selecciona una fecha", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedHora24 == null) {
            Toast.makeText(this, "Por favor selecciona una hora", Toast.LENGTH_SHORT).show();
            return;
        }
        if (proyectoId == null || proyectoId.isEmpty()) {
            Toast.makeText(this, "Datos del proyecto no disponibles. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
            return;
        }

        String slotId = CitaRepository.generarSlotId(
                proyectoId, selectedYear, selectedMonth + 1, selectedDay, selectedHora24);

        // Verificación optimista antes de la transacción
        if (slotsOcupados.contains(slotId)) {
            Toast.makeText(this, "Este horario ya está reservado. Elige otro.", Toast.LENGTH_SHORT).show();
            return;
        }

        Date fechaTimestamp = CitaRepository.calcularFechaTimestamp(
                selectedYear, selectedMonth, selectedDay, selectedHora24);

        setLoadingState(true);

        if (modoReagendar && citaId != null && !citaId.isEmpty()) {
            ejecutarReagendar(slotId, fechaTimestamp);
        } else {
            if (uidAsesor == null || uidAsesor.isEmpty()) {
                setLoadingState(false);
                Toast.makeText(this, "Datos del asesor no disponibles. Intenta desde el detalle del proyecto.", Toast.LENGTH_LONG).show();
                return;
            }
            ejecutarReserva(slotId, fechaTimestamp);
        }
    }

    private void ejecutarReserva(String slotId, Date fechaTimestamp) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        String nombre = PreferencesManager.obtenerNombre(this);
        if (nombre == null || nombre.isEmpty()) nombre = "Cliente";

        citaRepository.reservarCita(uid, nombre, uidAsesor, proyectoId, proyectoNombre, slotId, fechaTimestamp)
                .addOnSuccessListener(v -> {
                    setLoadingState(false);
                    mostrarDialogoExito();
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    if (e instanceof FirebaseFirestoreException
                            && ((FirebaseFirestoreException) e).getCode()
                               == FirebaseFirestoreException.Code.ABORTED) {
                        Toast.makeText(this,
                                "Este horario acaba de ser reservado. Elige otro.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "No se pudo agendar. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error reservando: " + e.getMessage());
                    }
                });
    }

    private void ejecutarReagendar(String nuevoSlotId, Date nuevaFecha) {
        citaRepository.reagendarCita(citaId, slotIdAnterior, nuevoSlotId, nuevaFecha)
                .addOnSuccessListener(v -> {
                    setLoadingState(false);
                    mostrarDialogoExito();
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    if (e instanceof FirebaseFirestoreException
                            && ((FirebaseFirestoreException) e).getCode()
                               == FirebaseFirestoreException.Code.ABORTED) {
                        Toast.makeText(this,
                                "Ese horario no está disponible. Elige otro.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "No se pudo reagendar. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error reagendando: " + e.getMessage());
                    }
                });
    }

    private void setLoadingState(boolean loading) {
        if (btnNext != null) {
            btnNext.setEnabled(!loading);
            btnNext.setAlpha(loading ? 0.5f : 1f);
        }
        if (btnNextArrow != null) btnNextArrow.setEnabled(!loading);
    }

    // ── Diálogo de éxito ───────────────────────────────────────────────────────

    private void mostrarDialogoExito() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setPadding(60, 40, 60, 60);
        layout.setBackgroundColor(android.graphics.Color.WHITE);

        android.widget.FrameLayout halo = new android.widget.FrameLayout(this);
        android.widget.LinearLayout.LayoutParams haloLp =
                new android.widget.LinearLayout.LayoutParams(180, 180);
        haloLp.gravity = android.view.Gravity.CENTER;
        haloLp.bottomMargin = 40;
        halo.setLayoutParams(haloLp);
        halo.setBackground(new android.graphics.drawable.GradientDrawable() {{
            setShape(android.graphics.drawable.GradientDrawable.OVAL);
            setColor(0x224CAF50);
        }});

        android.widget.TextView check = new android.widget.TextView(this);
        android.widget.FrameLayout.LayoutParams checkLp =
                new android.widget.FrameLayout.LayoutParams(110, 110);
        checkLp.gravity = android.view.Gravity.CENTER;
        check.setLayoutParams(checkLp);
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

        android.widget.TextView tvTitulo = new android.widget.TextView(this);
        tvTitulo.setText(modoReagendar ? "Cita reagendada" : "Tu cita ha sido agendada");
        tvTitulo.setTextSize(20f);
        tvTitulo.setTextColor(0xFF1A1A2E);
        tvTitulo.setGravity(android.view.Gravity.CENTER);
        layout.addView(tvTitulo);

        android.widget.TextView tvSub = new android.widget.TextView(this);
        tvSub.setText("con éxito");
        tvSub.setTextSize(20f);
        tvSub.setTextColor(0xFF1A1A2E);
        tvSub.setTypeface(null, android.graphics.Typeface.BOLD);
        tvSub.setGravity(android.view.Gravity.CENTER);
        android.widget.LinearLayout.LayoutParams subLp =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        subLp.bottomMargin = 16;
        tvSub.setLayoutParams(subLp);
        layout.addView(tvSub);

        android.widget.TextView tvDesc = new android.widget.TextView(this);
        tvDesc.setText("Podrás ver los detalles en Mis Citas");
        tvDesc.setTextSize(13f);
        tvDesc.setTextColor(0xFF9E9E9E);
        tvDesc.setGravity(android.view.Gravity.CENTER);
        android.widget.LinearLayout.LayoutParams descLp =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        descLp.bottomMargin = 40;
        tvDesc.setLayoutParams(descLp);
        layout.addView(tvDesc);

        android.widget.TextView btnAceptar = new android.widget.TextView(this);
        android.widget.LinearLayout.LayoutParams btnLp =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 130);
        btnAceptar.setLayoutParams(btnLp);
        btnAceptar.setText("Aceptar");
        btnAceptar.setTextSize(15f);
        btnAceptar.setTextColor(android.graphics.Color.WHITE);
        btnAceptar.setTypeface(null, android.graphics.Typeface.BOLD);
        btnAceptar.setGravity(android.view.Gravity.CENTER);
        btnAceptar.setBackground(new android.graphics.drawable.GradientDrawable() {{
            setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            setCornerRadius(60f);
            setColor(0xFF4CAF50);
        }});
        btnAceptar.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, MisCitasActivity.class));
            finish();
        });
        layout.addView(btnAceptar);

        dialog.setContentView(layout);
        dialog.show();

        NotificationHelper.lanzarNotificacion(this,
                modoReagendar ? "Cita Reagendada" : "Cita Confirmada",
                "Tu cita ha sido " + (modoReagendar ? "reagendada" : "agendada") + " correctamente",
                NotificationHelper.NOTIF_CITA_CONFIRMADA,
                new Intent(this, MisCitasActivity.class));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private int obtenerImagenProyecto(String nombre) {
        if (nombre == null) return ImageUrls.HERO_TORRES_UNIDAS;
        switch (nombre) {
            case "Catalina Ventor":       return ImageUrls.PROYECTO_CATALINA_VENTOR;
            case "Residencial Park":
            case "Residencial El Park":   return ImageUrls.PROYECTO_RESIDENCIAL_PARK;
            case "Torre Miramar":         return ImageUrls.PROYECTO_TORRE_MIRAMAR;
            case "Condominio Las Lomas":  return ImageUrls.PROYECTO_CONDOMINIO_LOMAS;
            case "Catalina Sky":          return ImageUrls.PROYECTO_CATALINA_SKY;
            default:                      return ImageUrls.HERO_TORRES_UNIDAS;
        }
    }
}
