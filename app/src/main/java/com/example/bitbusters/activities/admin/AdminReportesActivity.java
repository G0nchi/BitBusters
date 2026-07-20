package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.example.bitbusters.R;
import com.example.bitbusters.data.SeparacionesRepository;
import com.example.bitbusters.models.AdminSeparacion;
import com.example.bitbusters.utils.AdminPreferencesManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AdminReportesActivity extends AdminMainActivity {

    private final String[] tendenciaOptions = {"Este mes", "Esta semana", "Este semestre", "Este año"};
    private int selectedOption = 0;
    private TextView tvTendenciaTitle;
    private Chip chipEstesMes;
    private TextView tvReporteVentas;
    private TextView tvReporteAprobadas;
    private TextView tvReportePendientes;
    private TextView tvReporteRechazadas;
    private ListenerRegistration separacionesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reportes);
        setupHeaderListeners();
        setupBottomNavigation(R.id.nav_reportes);
        bindReporteViews();
        setupListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        iniciarListenerSeparaciones();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (separacionesListener != null) {
            separacionesListener.remove();
            separacionesListener = null;
        }
    }

    private void bindReporteViews() {
        tvReporteVentas = findViewById(R.id.tvReporteVentas);
        tvReporteAprobadas = findViewById(R.id.tvReporteAprobadas);
        tvReportePendientes = findViewById(R.id.tvReportePendientes);
        tvReporteRechazadas = findViewById(R.id.tvReporteRechazadas);
        tvTendenciaTitle = findViewById(R.id.tvTendenciaTitle);
    }

    private void setupListeners() {
        chipEstesMes = findViewById(R.id.chipEstesMes);
        if (chipEstesMes != null) {
            chipEstesMes.setOnClickListener(v -> showTendenciaDialog());
        }

        Chip chipVerReportes = findViewById(R.id.chipVerReportes);
        if (chipVerReportes != null) {
            chipVerReportes.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminDetallesDeReporteProyectoActivity.class);
                startActivity(intent);
            });
        }
    }

    private void iniciarListenerSeparaciones() {
        if (separacionesListener != null) {
            separacionesListener.remove();
        }
        separacionesListener = SeparacionesRepository.escucharDesdeFirestore(
                AdminPreferencesManager.obtenerInmobiliariaId(this),
                new SeparacionesRepository.SeparacionesListener() {
                    @Override
                    public void onSeparacionesActualizadas(List<AdminSeparacion> separaciones) {
                        actualizarKpis(separaciones);
                    }

                    @Override
                    public void onError(String mensaje) {
                        actualizarKpis(SeparacionesRepository.getLista());
                    }
                });
    }

    private void actualizarKpis(List<AdminSeparacion> separaciones) {
        int aprobadas = 0;
        int pendientes = 0;
        int rechazadas = 0;
        double ventasAprobadas = 0;

        if (separaciones != null) {
            for (AdminSeparacion separacion : separaciones) {
                String estado = separacion.getEstado() == null ? "" : separacion.getEstado();
                if ("Aprobada".equalsIgnoreCase(estado)) {
                    aprobadas++;
                    ventasAprobadas += parseMonto(separacion.getMonto());
                } else if ("Rechazada".equalsIgnoreCase(estado)) {
                    rechazadas++;
                } else {
                    pendientes++;
                }
            }
        }

        setText(tvReporteVentas, formatearSoles(ventasAprobadas));
        setText(tvReporteAprobadas, String.valueOf(aprobadas));
        setText(tvReportePendientes, String.valueOf(pendientes));
        setText(tvReporteRechazadas, String.valueOf(rechazadas));
    }

    private double parseMonto(String monto) {
        if (monto == null || monto.trim().isEmpty()) return 0;
        String limpio = monto.replace("S/", "")
                .replace("s/", "")
                .replace(",", "")
                .trim();
        try {
            return Double.parseDouble(limpio);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String formatearSoles(double monto) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        format.setMaximumFractionDigits(0);
        return "S/" + format.format(monto);
    }

    private void setText(TextView textView, String value) {
        if (textView != null) {
            textView.setText(value);
        }
    }

    private void showTendenciaDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Seleccionar período")
            .setSingleChoiceItems(tendenciaOptions, selectedOption, (dialog, which) -> {
                selectedOption = which;
                updateTendenciaTitle();
                updateChipText();
                dialog.dismiss();
            })
            .show();
    }

    private void updateTendenciaTitle() {
        if (tvTendenciaTitle != null) {
            tvTendenciaTitle.setText("Tendencia " + tendenciaOptions[selectedOption].toLowerCase());
        }
    }

    private void updateChipText() {
        if (chipEstesMes != null) {
            chipEstesMes.setText(tendenciaOptions[selectedOption]);
        }
    }
}
