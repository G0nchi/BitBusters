package com.example.bitbusters.activities.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.bitbusters.R;
import com.example.bitbusters.adapters.ClientAppointmentsAdapter;
import com.example.bitbusters.data.ClientDataRepository;
import com.example.bitbusters.models.ClientAppointment;

import java.util.ArrayList;
import java.util.List;

public class MisCitasActivity extends AppCompatActivity {

    private static final int TAB_TODAS = 0;
    private static final int TAB_PROXIMAS = 1;
    private static final int TAB_HISTORIAL = 2;

    private TextView tabTodas;
    private TextView tabProximas;
    private TextView tabHistorial;
    private ClientAppointmentsAdapter appointmentsAdapter;
    private final List<ClientAppointment> allAppointments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_citas);

        tabTodas    = findViewById(R.id.tabTodas);
        tabProximas = findViewById(R.id.tabProximas);
        tabHistorial= findViewById(R.id.tabHistorial);

        allAppointments.addAll(ClientDataRepository.getAppointments());

        RecyclerView recyclerView = findViewById(R.id.recyclerViewCitas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        appointmentsAdapter = new ClientAppointmentsAdapter(new ClientAppointmentsAdapter.OnAppointmentActionListener() {
            @Override
            public void onPrimaryAction(ClientAppointment appointment) {
                Toast.makeText(MisCitasActivity.this, "Accion ejecutada: " + appointment.getProjectName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSecondaryAction(ClientAppointment appointment) {
                if (ClientAppointment.STATUS_COMPLETED.equals(appointment.getStatus())) {
                    Intent intent = new Intent(MisCitasActivity.this, AddCommentActivity.class);
                    intent.putExtra("proyecto", appointment.getProjectName());
                    startActivity(intent);
                }
            }
        });
        recyclerView.setAdapter(appointmentsAdapter);

        // Botón volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Tabs
        tabTodas.setOnClickListener(v -> seleccionarTab(TAB_TODAS));
        tabProximas.setOnClickListener(v -> seleccionarTab(TAB_PROXIMAS));
        tabHistorial.setOnClickListener(v -> seleccionarTab(TAB_HISTORIAL));
        seleccionarTab(TAB_TODAS);

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

    private void seleccionarTab(int pos) {
        int azul = getResources().getColor(android.R.color.holo_blue_dark, getTheme());
        int gris = getResources().getColor(android.R.color.darker_gray, getTheme());

        tabTodas.setTextColor(pos == 0 ? azul : gris);
        tabProximas.setTextColor(pos == 1 ? azul : gris);
        tabHistorial.setTextColor(pos == 2 ? azul : gris);

        tabTodas.setBackgroundResource(pos == 0 ? R.drawable.bg_tab_selected_bottom : 0);
        tabProximas.setBackgroundResource(pos == 1 ? R.drawable.bg_tab_selected_bottom : 0);
        tabHistorial.setBackgroundResource(pos == 2 ? R.drawable.bg_tab_selected_bottom : 0);

        List<ClientAppointment> filtered = filterByTab(pos);
        appointmentsAdapter.submitList(filtered);
    }

    private List<ClientAppointment> filterByTab(int tab) {
        List<ClientAppointment> filtered = new ArrayList<>();
        for (ClientAppointment appointment : allAppointments) {
            if (matchesTab(appointment, tab)) {
                filtered.add(appointment);
            }
        }
        return filtered;
    }

    private boolean matchesTab(ClientAppointment appointment, int tab) {
        if (tab == TAB_TODAS) {
            return true;
        }
        if (tab == TAB_PROXIMAS) {
            return ClientAppointment.STATUS_PENDING.equals(appointment.getStatus())
                    || ClientAppointment.STATUS_CONFIRMED.equals(appointment.getStatus());
        }
        return ClientAppointment.STATUS_COMPLETED.equals(appointment.getStatus())
                || ClientAppointment.STATUS_CANCELED.equals(appointment.getStatus())
                || ClientAppointment.STATUS_REVIEWED.equals(appointment.getStatus());
    }
}