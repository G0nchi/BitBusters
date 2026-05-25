package com.example.bitbusters.activities.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.ClientAppointmentsAdapter;
import com.example.bitbusters.data.ClientDataRepository;
import com.example.bitbusters.models.ClientAppointment;
import com.example.bitbusters.utils.PreferencesManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MisCitasActivity extends AppCompatActivity {

    private static final int TAB_TODAS    = 0;
    private static final int TAB_PROXIMAS = 1;
    private static final int TAB_HISTORIAL= 2;

    private TextView tabTodas, tabProximas, tabHistorial;
    private ClientAppointmentsAdapter appointmentsAdapter;

    // Lista maestra con los estados actuales (puede contener cancelaciones del usuario)
    private final List<ClientAppointment> allAppointments = new ArrayList<>();

    // Tab actualmente seleccionado (para restaurar al volver al Activity)
    private int tabActual = TAB_TODAS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_citas);

        tabTodas    = findViewById(R.id.tabTodas);
        tabProximas = findViewById(R.id.tabProximas);
        tabHistorial= findViewById(R.id.tabHistorial);

        // Configurar RecyclerView con el adapter que recibe las acciones de botones
        RecyclerView recyclerView = findViewById(R.id.recyclerViewCitas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        appointmentsAdapter = new ClientAppointmentsAdapter(new ClientAppointmentsAdapter.OnAppointmentActionListener() {

            @Override
            public void onPrimaryAction(ClientAppointment cita) {
                // btnLeft: Reagendar (para CONFIRMADA y COMPLETADA)
                String status = cita.getStatus();
                if (ClientAppointment.STATUS_CONFIRMED.equals(status)
                        || ClientAppointment.STATUS_COMPLETED.equals(status)
                        || ClientAppointment.STATUS_PENDING.equals(status)
                        || ClientAppointment.STATUS_CANCELED.equals(status)) {
                    // Abrir AgendaCitaActivity con el nombre del proyecto para reagendar
                    Intent intent = new Intent(MisCitasActivity.this, AgendaCitaActivity.class);
                    intent.putExtra("proyecto", cita.getProjectName());
                    startActivity(intent);
                }
            }

            @Override
            public void onSecondaryAction(ClientAppointment cita) {
                // btnRight varía según el estado
                String status = cita.getStatus();
                if (ClientAppointment.STATUS_CONFIRMED.equals(status)) {
                    // Cancelar: mostrar diálogo de confirmación
                    mostrarDialogoCancelar(cita);
                } else if (ClientAppointment.STATUS_COMPLETED.equals(status)
                        || ClientAppointment.STATUS_REVIEWED.equals(status)) {
                    // Valorar / Escribir nuevo: ir a AddCommentActivity
                    Intent intent = new Intent(MisCitasActivity.this, AddCommentActivity.class);
                    intent.putExtra("proyecto", cita.getProjectName());
                    startActivity(intent);
                } else if (ClientAppointment.STATUS_PENDING.equals(status)) {
                    // Reagendar desde Pendiente
                    Intent intent = new Intent(MisCitasActivity.this, AgendaCitaActivity.class);
                    intent.putExtra("proyecto", cita.getProjectName());
                    startActivity(intent);
                }
            }
        });

        recyclerView.setAdapter(appointmentsAdapter);

        // Botón volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Listeners de tabs
        tabTodas.setOnClickListener(v     -> seleccionarTab(TAB_TODAS));
        tabProximas.setOnClickListener(v  -> seleccionarTab(TAB_PROXIMAS));
        tabHistorial.setOnClickListener(v -> seleccionarTab(TAB_HISTORIAL));

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_favoritos);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            }
            return id == R.id.nav_favoritos;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar citas aplicando las cancelaciones persistidas en SharedPreferences
        recargarCitas();
        seleccionarTab(tabActual);
    }

    /**
     * Recarga la lista maestra desde el repositorio y aplica encima
     * las cancelaciones guardadas en SharedPreferences.
     */
    private void recargarCitas() {
        Set<String> canceladas = PreferencesManager.obtenerCitasCanceladas(this);
        allAppointments.clear();
        for (ClientAppointment cita : ClientDataRepository.getAppointments()) {
            if (canceladas.contains(cita.getId())) {
                // Reemplazar con copia de estado CANCELADA
                allAppointments.add(cita.withStatus(ClientAppointment.STATUS_CANCELED));
            } else {
                allAppointments.add(cita);
            }
        }
    }

    /**
     * Muestra el diálogo de confirmación antes de cancelar una cita.
     * Si el usuario confirma, persiste en SharedPreferences y actualiza la UI.
     */
    private void mostrarDialogoCancelar(ClientAppointment cita) {
        new AlertDialog.Builder(this)
                .setTitle("Cancelar cita")
                .setMessage("¿Estás seguro que deseas cancelar esta cita?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Persistir la cancelación para sobrevivir a reinicios de la app
                    PreferencesManager.guardarCitaCancelada(this, cita.getId());

                    // Actualizar la lista en memoria buscando por ID
                    for (int i = 0; i < allAppointments.size(); i++) {
                        if (allAppointments.get(i).getId().equals(cita.getId())) {
                            allAppointments.set(i,
                                    cita.withStatus(ClientAppointment.STATUS_CANCELED));
                            break;
                        }
                    }

                    // Refrescar la vista del tab actual
                    seleccionarTab(tabActual);
                })
                .setNegativeButton("No", null)
                .show();
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

    /**
     * Filtra la lista según el tab:
     * - Próximas: solo CONFIRMADA y PENDIENTE
     * - Historial: solo COMPLETADA, VALORADA y CANCELADA
     * - Todas: sin filtro
     */
    private List<ClientAppointment> filtrarPorTab(int tab) {
        List<ClientAppointment> resultado = new ArrayList<>();
        for (ClientAppointment cita : allAppointments) {
            if (coincideConTab(cita, tab)) resultado.add(cita);
        }
        return resultado;
    }

    private boolean coincideConTab(ClientAppointment cita, int tab) {
        if (tab == TAB_TODAS) return true;
        if (tab == TAB_PROXIMAS) {
            return ClientAppointment.STATUS_CONFIRMED.equals(cita.getStatus())
                    || ClientAppointment.STATUS_PENDING.equals(cita.getStatus());
        }
        // TAB_HISTORIAL
        return ClientAppointment.STATUS_COMPLETED.equals(cita.getStatus())
                || ClientAppointment.STATUS_REVIEWED.equals(cita.getStatus())
                || ClientAppointment.STATUS_CANCELED.equals(cita.getStatus());
    }
}
