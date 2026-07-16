package com.example.bitbusters.activities.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.ClientAppointmentsAdapter;
import com.example.bitbusters.models.Cita;
import com.example.bitbusters.models.ClientAppointment;
import com.example.bitbusters.repository.CitaRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import android.widget.LinearLayout;

public class MisCitasActivity extends AppCompatActivity {

    private static final int TAB_TODAS    = 0;
    private static final int TAB_PROXIMAS = 1;
    private static final int TAB_HISTORIAL= 2;
    private static final String TAG = "MisCitas";

    private TextView tabTodas, tabProximas, tabHistorial;
    private ClientAppointmentsAdapter appointmentsAdapter;

    // Lista maestra de citas (poblada por el listener de Firestore)
    private final List<ClientAppointment> allAppointments = new ArrayList<>();
    private int tabActual = TAB_TODAS;

    // Firestore
    private final CitaRepository      citaRepository = new CitaRepository();
    private       ListenerRegistration citasListener;

    // Formateadores zona Lima (inicializados una vez)
    private final SimpleDateFormat sdfFecha;
    private final SimpleDateFormat sdfHora;

    {
        Locale esPE = new Locale("es", "PE");
        TimeZone lima = TimeZone.getTimeZone("America/Lima");
        sdfFecha = new SimpleDateFormat("d MMM yyyy", esPE);
        sdfFecha.setTimeZone(lima);
        sdfHora  = new SimpleDateFormat("h:mm a", Locale.US);
        sdfHora.setTimeZone(lima);
    }

    // ── Ciclo de vida ──────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_citas);

        tabTodas    = findViewById(R.id.tabTodas);
        tabProximas = findViewById(R.id.tabProximas);
        tabHistorial= findViewById(R.id.tabHistorial);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewCitas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        appointmentsAdapter = new ClientAppointmentsAdapter(
                new ClientAppointmentsAdapter.OnAppointmentActionListener() {
                    @Override
                    public void onPrimaryAction(ClientAppointment cita) {
                        abrirDetalleCita(cita);
                    }
                    @Override
                    public void onSecondaryAction(ClientAppointment cita) {
                        accionSecundaria(cita);
                    }
                });
        recyclerView.setAdapter(appointmentsAdapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tabTodas.setOnClickListener(v     -> seleccionarTab(TAB_TODAS));
        tabProximas.setOnClickListener(v  -> seleccionarTab(TAB_PROXIMAS));
        tabHistorial.setOnClickListener(v -> seleccionarTab(TAB_HISTORIAL));

        LinearLayout navHome   = findViewById(R.id.navHome);
        LinearLayout navSearch = findViewById(R.id.navSearch);
        LinearLayout navPerfil = findViewById(R.id.navPerfil);

        if (navHome   != null) navHome.setOnClickListener(v -> { startActivity(new Intent(this, HomeActivity.class)); finish(); });
        if (navSearch != null) navSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        if (navPerfil != null) navPerfil.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        suscribirCitas();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (citasListener != null) {
            citasListener.remove();
            citasListener = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reaplicar estilo del tab actual (el listener refresca los datos)
        seleccionarTab(tabActual);
    }

    // ── Firestore ──────────────────────────────────────────────────────────────

    private void suscribirCitas() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        citasListener = citaRepository.escucharCitasCliente(uid,
                new CitaRepository.CitasClienteListener() {
                    @Override
                    public void onCitasActualizadas(List<Cita> citas) {
                        allAppointments.clear();
                        for (Cita c : citas) {
                            allAppointments.add(mapearCita(c));
                        }
                        seleccionarTab(tabActual);
                    }
                    @Override
                    public void onError(String msg) {
                        Log.e(TAG, "Error escuchando citas: " + msg);
                        Toast.makeText(MisCitasActivity.this,
                                "No se pudieron cargar las citas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private ClientAppointment mapearCita(Cita cita) {
        String fechaStr = "—", horaStr = "—";
        Date ts = cita.getFechaTimestamp();
        if (ts != null) {
            fechaStr = sdfFecha.format(ts);
            horaStr  = sdfHora.format(ts);
        }

        String estadoDisplay = mapearEstado(cita.getEstado());
        String nombreProyecto = cita.getProyectoNombre() != null ? cita.getProyectoNombre() : "—";

        ClientAppointment ca = new ClientAppointment(
                cita.getId(),       // id (para tab filtering)
                nombreProyecto,
                "",                 // location (no está en el esquema de citas)
                fechaStr,
                horaStr,
                "Asesor",           // nombreAsesor (podría cargarse del users/ si se requiere)
                "A",
                0xFF1A7EBD,         // brand_deep_blue
                estadoDisplay
        );
        ca.setFirestoreId(cita.getId())
          .setSlotId(cita.getSlotId())
          .setProyectoId(cita.getProyectoId())
          .setUidAsesorCita(cita.getUidAsesor());
        return ca;
    }

    private String mapearEstado(String estado) {
        if (estado == null) return ClientAppointment.STATUS_PENDING;
        switch (estado) {
            case Cita.ESTADO_CONFIRMADA: return ClientAppointment.STATUS_CONFIRMED;
            case Cita.ESTADO_CANCELADA:  return ClientAppointment.STATUS_CANCELED;
            case Cita.ESTADO_COMPLETADA: return ClientAppointment.STATUS_COMPLETED;
            case Cita.ESTADO_VALORADA:   return ClientAppointment.STATUS_REVIEWED;
            default:                     return ClientAppointment.STATUS_PENDING;
        }
    }

    // ── Acciones del adapter ───────────────────────────────────────────────────

    private void abrirDetalleCita(ClientAppointment cita) {
        Intent intent = new Intent(this, CitaDetailActivity.class);
        intent.putExtra(CitaDetailActivity.EXTRA_FIRESTORE_ID,    cita.getFirestoreId());
        intent.putExtra(CitaDetailActivity.EXTRA_SLOT_ID,         cita.getSlotId());
        intent.putExtra(CitaDetailActivity.EXTRA_PROYECTO_ID,     cita.getProyectoId());
        intent.putExtra(CitaDetailActivity.EXTRA_PROYECTO_NOMBRE, cita.getProjectName());
        intent.putExtra(CitaDetailActivity.EXTRA_UID_ASESOR,      cita.getUidAsesorCita());
        intent.putExtra(CitaDetailActivity.EXTRA_FECHA,           cita.getDate());
        intent.putExtra(CitaDetailActivity.EXTRA_HORA,            cita.getTime());
        intent.putExtra(CitaDetailActivity.EXTRA_ESTADO,          cita.getStatus());
        startActivity(intent);
    }

    private void accionSecundaria(ClientAppointment cita) {
        String status = cita.getStatus();
        if (ClientAppointment.STATUS_CONFIRMED.equals(status)
                || ClientAppointment.STATUS_PENDING.equals(status)) {
            mostrarDialogoCancelar(cita);
        } else if (ClientAppointment.STATUS_COMPLETED.equals(status)
                || ClientAppointment.STATUS_REVIEWED.equals(status)) {
            Intent intent = new Intent(this, AddCommentActivity.class);
            intent.putExtra("proyecto", cita.getProjectName());
            intent.putExtra("citaId", cita.getFirestoreId());
            intent.putExtra("proyectoId", cita.getProyectoId());
            intent.putExtra("uidAsesor", cita.getUidAsesorCita());
            startActivity(intent);
        }
    }

    private void mostrarDialogoCancelar(ClientAppointment cita) {
        android.widget.EditText etMotivo = new android.widget.EditText(this);
        etMotivo.setHint("Indica el motivo de cancelación");
        etMotivo.setMaxLines(3);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);

        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(pad, pad / 2, pad, 0);
        container.addView(etMotivo);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Cancelar cita")
                .setMessage("Esta acción no se puede deshacer.")
                .setView(container)
                .setPositiveButton("Cancelar cita", null) // override en setOnShowListener
                .setNegativeButton("Volver", null)
                .create();

        dialog.setOnShowListener(d -> {
            android.widget.Button btnConfirmar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnConfirmar.setOnClickListener(v -> {
                String motivo = etMotivo.getText().toString().trim();
                if (motivo.isEmpty()) {
                    etMotivo.setError("Indica el motivo");
                    return;
                }
                String firestoreId = cita.getFirestoreId();
                String slotId      = cita.getSlotId();
                if (firestoreId == null || firestoreId.isEmpty()) {
                    Toast.makeText(this, "Error: cita sin ID. Recarga.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }
                btnConfirmar.setEnabled(false);
                citaRepository.cancelarCita(firestoreId, slotId, motivo)
                        .addOnSuccessListener(__ -> {
                            Toast.makeText(this, "Cita cancelada", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            // La UI se actualiza automáticamente por el listener de Firestore
                        })
                        .addOnFailureListener(e -> {
                            btnConfirmar.setEnabled(true);
                            Toast.makeText(this, "No se pudo cancelar. Intenta de nuevo.",
                                    Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error cancelando: " + e.getMessage());
                        });
            });
        });

        dialog.show();
    }

    // ── Tabs ───────────────────────────────────────────────────────────────────

    private void seleccionarTab(int pos) {
        tabActual = pos;

        int azul = getResources().getColor(android.R.color.holo_blue_dark, getTheme());
        int gris = getResources().getColor(android.R.color.darker_gray, getTheme());

        tabTodas.setTextColor(pos == TAB_TODAS     ? azul : gris);
        tabProximas.setTextColor(pos == TAB_PROXIMAS  ? azul : gris);
        tabHistorial.setTextColor(pos == TAB_HISTORIAL ? azul : gris);

        tabTodas.setBackgroundResource(pos == TAB_TODAS     ? R.drawable.bg_tab_selected_bottom : 0);
        tabProximas.setBackgroundResource(pos == TAB_PROXIMAS  ? R.drawable.bg_tab_selected_bottom : 0);
        tabHistorial.setBackgroundResource(pos == TAB_HISTORIAL ? R.drawable.bg_tab_selected_bottom : 0);

        appointmentsAdapter.submitList(filtrarPorTab(pos));
    }

    private List<ClientAppointment> filtrarPorTab(int tab) {
        List<ClientAppointment> res = new ArrayList<>();
        for (ClientAppointment c : allAppointments) {
            if (coincideConTab(c, tab)) res.add(c);
        }
        return res;
    }

    private boolean coincideConTab(ClientAppointment cita, int tab) {
        if (tab == TAB_TODAS) return true;
        if (tab == TAB_PROXIMAS) {
            return ClientAppointment.STATUS_CONFIRMED.equals(cita.getStatus())
                    || ClientAppointment.STATUS_PENDING.equals(cita.getStatus());
        }
        return ClientAppointment.STATUS_COMPLETED.equals(cita.getStatus())
                || ClientAppointment.STATUS_REVIEWED.equals(cita.getStatus())
                || ClientAppointment.STATUS_CANCELED.equals(cita.getStatus());
    }
}
