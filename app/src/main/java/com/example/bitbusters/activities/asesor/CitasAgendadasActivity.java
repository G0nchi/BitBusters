package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CitasAgendadasActivity extends AppCompatActivity {

    private RecyclerView rvCitas;
    private MaterialButton tabPendientes, tabConfirmadas, tabPasadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citas_agendadas);

        rvCitas = findViewById(R.id.rv_citas);
        rvCitas.setLayoutManager(new LinearLayoutManager(this));

        tabPendientes = findViewById(R.id.tab_pendientes);
        tabConfirmadas = findViewById(R.id.tab_confirmadas);
        tabPasadas = findViewById(R.id.tab_pasadas);

        tabPendientes.setOnClickListener(v -> switchTab(0));
        tabConfirmadas.setOnClickListener(v -> switchTab(1));
        tabPasadas.setOnClickListener(v -> switchTab(2));

        switchTab(0);
        setupBottomNav();
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void switchTab(int tab) {
        int activeColor = Color.parseColor("#252B5C");
        int inactiveColor = Color.TRANSPARENT;
        int activeText = Color.WHITE;
        int inactiveText = Color.parseColor("#AACDE0");

        tabPendientes.setBackgroundTintList(android.content.res.ColorStateList.valueOf(tab == 0 ? activeColor : inactiveColor));
        tabPendientes.setTextColor(tab == 0 ? activeText : inactiveText);
        tabConfirmadas.setBackgroundTintList(android.content.res.ColorStateList.valueOf(tab == 1 ? activeColor : inactiveColor));
        tabConfirmadas.setTextColor(tab == 1 ? activeText : inactiveText);
        tabPasadas.setBackgroundTintList(android.content.res.ColorStateList.valueOf(tab == 2 ? activeColor : inactiveColor));
        tabPasadas.setTextColor(tab == 2 ? activeText : inactiveText);

        rvCitas.setAdapter(new CitaAdapter(buildCitas(tab),
            new CitaAdapter.OnCitaActionListener() {
                @Override
                public void onLeftClick(int pos, CitaAdapter.Cita c) {
                    if ("Ver valoración".equals(c.btnLeft)) {
                        startActivity(new Intent(CitasAgendadasActivity.this, ValorarVisitaActivity.class));
                    }
                }
                @Override
                public void onRightClick(int pos, CitaAdapter.Cita c) {
                    if ("Confirmar".equals(c.btnRight)) {
                        showConfirmDialog();
                    } else if ("Valorar".equals(c.btnRight)) {
                        startActivity(new Intent(CitasAgendadasActivity.this, ValorarVisitaActivity.class));
                    }
                }
            }));
    }

    private List<CitaAdapter.Cita> buildCitas(int tab) {
        List<CitaAdapter.Cita> list = new ArrayList<>();
        int pendText  = Color.parseColor("#9A5700");
        int confText  = Color.parseColor("#186A3B");
        int pasadaText = Color.parseColor("#666666");
        int valoradaText = Color.parseColor("#1A5799");
        int canceladaText = Color.parseColor("#CC2222");

        if (tab == 0) {
            list.add(new CitaAdapter.Cita("CM", Color.parseColor("#4ECDC4"), "Carlos Mendoza",
                "Torres del Sol · Dpto 302", "Lun 7 Abr, 2025", "10:30 AM",
                "Pendiente", 0, pendText, "Reagendar", "Confirmar", false, false));
            list.add(new CitaAdapter.Cita("AL", Color.parseColor("#FF8C42"), "Ana López",
                "Torres del Sol · Dpto 501", "Mar 8 Abr, 2025", "3:00 PM",
                "Pendiente", 0, pendText, "Reagendar", "Confirmar", false, false));
            list.add(new CitaAdapter.Cita("RT", Color.parseColor("#FF6B9D"), "Rosa Torres",
                "Torres del Sol · Dpto 108", "Mié 9 Abr, 2025", "11:00 AM",
                "Confirmada", 0, confText, "Ver detalle", "Separar", false, false));
        } else if (tab == 1) {
            list.add(new CitaAdapter.Cita("RT", Color.parseColor("#FF6B9D"), "Rosa Torres",
                "Torres del Sol · Dpto 108", "Mié 9 Abr, 2025", "11:00 AM",
                "Confirmada", 0, confText, "Ver detalle", "Cancelar", false, false));
            list.add(new CitaAdapter.Cita("MP", Color.parseColor("#9B59B6"), "Marco Paredes",
                "Torres del Sol · Dpto 210", "Jue 10 Abr, 2025", "2:00 PM",
                "Confirmada", 0, confText, "Ver detalle", "Cancelar", false, false));
            list.add(new CitaAdapter.Cita("SV", Color.parseColor("#3498DB"), "Sandra Vega",
                "Torres del Sol · Dpto 415", "Vie 11 Abr, 2025", "4:30 PM",
                "Confirmada", 0, confText, "Ver detalle", "Cancelar", false, false));
        } else {
            list.add(new CitaAdapter.Cita("RT", Color.parseColor("#FF6B9D"), "Rosa Torres",
                "Torres del Sol · Dpto 108", "Mié 2 Abr, 2025", "11:00 AM",
                "Realizada", 0, pasadaText, "Ver detalle", "Valorar", true, false));
            list.add(new CitaAdapter.Cita("LV", Color.parseColor("#C8956C"), "Luis Vargas",
                "Torres del Sol · Dpto 204", "Mar 1 Abr, 2025", "2:00 PM",
                "Cancelada", 0, canceladaText, "Ver detalle", "Reagendar", false, false));
            list.add(new CitaAdapter.Cita("JC", Color.parseColor("#27AE60"), "Jorge Castro",
                "Torres del Sol · Dpto 601", "Lun 28 Mar, 2025", "4:00 PM",
                "Valorada", 0, valoradaText, "Ver valoración", "Valorar", false, true));
        }
        return list;
    }

    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.cita_dialog_title))
            .setMessage(getString(R.string.cita_dialog_msg))
            .setPositiveButton(getString(R.string.cita_dialog_ok), null)
            .show();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_citas);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, AsesorHomeActivity.class));
                finish();
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, MensajesActivity.class));
                finish();
            }
            return true;
        });
    }
}
