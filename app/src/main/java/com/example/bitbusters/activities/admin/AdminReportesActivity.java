package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.bitbusters.R;
import com.example.bitbusters.data.SeparacionesRepository;
import com.example.bitbusters.models.AdminSeparacion;
import com.example.bitbusters.utils.AdminPreferencesManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminReportesActivity extends AdminMainActivity {

    private final String[] tendenciaOptions = {"Este mes", "Esta semana", "Este semestre", "Este año"};
    private int selectedOption = 0;
    private TextView tvTendenciaTitle;
    private Chip chipEstesMes;
    private TextView tvReporteVentas;
    private TextView tvReporteAprobadas;
    private TextView tvReportePendientes;
    private TextView tvReporteRechazadas;
    private View[] layoutVentasProyecto;
    private TextView[] tvVentaProyectoNombre;
    private TextView[] tvVentaProyectoMonto;
    private ProgressBar[] progressVentaProyecto;
    private View[] barrasTendencia;
    private List<AdminSeparacion> separacionesActuales = new ArrayList<>();
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
        layoutVentasProyecto = new View[] {
                findViewById(R.id.layoutVentaProyecto1),
                findViewById(R.id.layoutVentaProyecto2),
                findViewById(R.id.layoutVentaProyecto3),
                findViewById(R.id.layoutVentaProyecto4)
        };
        tvVentaProyectoNombre = new TextView[] {
                findViewById(R.id.tvVentaProyectoNombre1),
                findViewById(R.id.tvVentaProyectoNombre2),
                findViewById(R.id.tvVentaProyectoNombre3),
                findViewById(R.id.tvVentaProyectoNombre4)
        };
        tvVentaProyectoMonto = new TextView[] {
                findViewById(R.id.tvVentaProyectoMonto1),
                findViewById(R.id.tvVentaProyectoMonto2),
                findViewById(R.id.tvVentaProyectoMonto3),
                findViewById(R.id.tvVentaProyectoMonto4)
        };
        progressVentaProyecto = new ProgressBar[] {
                findViewById(R.id.progressVentaProyecto1),
                findViewById(R.id.progressVentaProyecto2),
                findViewById(R.id.progressVentaProyecto3),
                findViewById(R.id.progressVentaProyecto4)
        };
        barrasTendencia = new View[] {
                findViewById(R.id.barTendencia1),
                findViewById(R.id.barTendencia2),
                findViewById(R.id.barTendencia3),
                findViewById(R.id.barTendencia4),
                findViewById(R.id.barTendencia5),
                findViewById(R.id.barTendencia6),
                findViewById(R.id.barTendencia7)
        };
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
        separacionesActuales = separaciones != null
                ? new ArrayList<>(separaciones)
                : new ArrayList<>();
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
        actualizarVentasPorProyecto(separaciones);
        actualizarTendencia(separaciones);
    }

    private void actualizarVentasPorProyecto(List<AdminSeparacion> separaciones) {
        Map<String, Double> ventasPorProyecto = new LinkedHashMap<>();
        if (separaciones != null) {
            for (AdminSeparacion separacion : separaciones) {
                if (!"Aprobada".equalsIgnoreCase(separacion.getEstado())) continue;
                String proyecto = separacion.getNombreProyecto();
                if (proyecto == null || proyecto.trim().isEmpty()) {
                    proyecto = "Proyecto sin nombre";
                }
                double monto = parseMonto(separacion.getMonto());
                ventasPorProyecto.put(
                        proyecto,
                        ventasPorProyecto.containsKey(proyecto)
                                ? ventasPorProyecto.get(proyecto) + monto
                                : monto
                );
            }
        }

        List<Map.Entry<String, Double>> ordenadas = new ArrayList<>(ventasPorProyecto.entrySet());
        Collections.sort(ordenadas, (a, b) -> Double.compare(b.getValue(), a.getValue()));

        if (ordenadas.isEmpty()) {
            mostrarFilaVentaProyecto(0, "Sin ventas aprobadas", 0, 0);
            for (int i = 1; i < 4; i++) ocultarFilaVentaProyecto(i);
            return;
        }

        double max = ordenadas.get(0).getValue();
        for (int i = 0; i < 4; i++) {
            if (i >= ordenadas.size()) {
                ocultarFilaVentaProyecto(i);
                continue;
            }
            Map.Entry<String, Double> item = ordenadas.get(i);
            int progreso = max > 0 ? (int) Math.max(5, Math.round((item.getValue() / max) * 100)) : 0;
            mostrarFilaVentaProyecto(i, item.getKey(), item.getValue(), progreso);
        }
    }

    private void mostrarFilaVentaProyecto(int index, String proyecto, double monto, int progreso) {
        if (layoutVentasProyecto != null && layoutVentasProyecto[index] != null) {
            layoutVentasProyecto[index].setVisibility(View.VISIBLE);
        }
        if (progressVentaProyecto != null && progressVentaProyecto[index] != null) {
            progressVentaProyecto[index].setVisibility(View.VISIBLE);
            progressVentaProyecto[index].setProgress(progreso);
        }
        setText(tvVentaProyectoNombre[index], proyecto);
        setText(tvVentaProyectoMonto[index], formatearSoles(monto));
    }

    private void ocultarFilaVentaProyecto(int index) {
        if (layoutVentasProyecto != null && layoutVentasProyecto[index] != null) {
            layoutVentasProyecto[index].setVisibility(View.GONE);
        }
        if (progressVentaProyecto != null && progressVentaProyecto[index] != null) {
            progressVentaProyecto[index].setVisibility(View.GONE);
        }
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

    private void actualizarTendencia(List<AdminSeparacion> separaciones) {
        if (barrasTendencia == null) return;

        double[] montos = new double[7];
        long ahora = System.currentTimeMillis();
        long duracion = obtenerDuracionPeriodoMillis();
        long inicio = ahora - duracion;
        long bucket = Math.max(1L, duracion / montos.length);

        if (separaciones != null) {
            for (AdminSeparacion separacion : separaciones) {
                if (separacion == null || !"Aprobada".equalsIgnoreCase(separacion.getEstado())) {
                    continue;
                }
                long fecha = fechaParaReporte(separacion);
                if (fecha <= 0 || fecha < inicio || fecha > ahora) {
                    continue;
                }
                int index = (int) Math.min(montos.length - 1, Math.max(0, (fecha - inicio) / bucket));
                montos[index] += parseMonto(separacion.getMonto());
            }
        }

        double max = 0;
        for (double monto : montos) {
            if (monto > max) max = monto;
        }

        for (int i = 0; i < barrasTendencia.length; i++) {
            View barra = barrasTendencia[i];
            if (barra == null) continue;
            ViewGroup.LayoutParams params = barra.getLayoutParams();
            int alturaMin = dpToPx(12);
            int alturaMax = dpToPx(90);
            params.height = max > 0
                    ? alturaMin + (int) Math.round((montos[i] / max) * (alturaMax - alturaMin))
                    : alturaMin;
            barra.setLayoutParams(params);
            barra.setAlpha(max > 0 ? 1f : 0.45f);
        }

        actualizarTituloTendencia(max > 0);
    }

    private long fechaParaReporte(AdminSeparacion separacion) {
        if (separacion.getFechaActualizacionMillis() > 0) {
            return separacion.getFechaActualizacionMillis();
        }
        return separacion.getFechaRegistroMillis();
    }

    private long obtenerDuracionPeriodoMillis() {
        long dia = 24L * 60L * 60L * 1000L;
        switch (selectedOption) {
            case 1: return 7L * dia;    // Esta semana
            case 2: return 182L * dia;  // Este semestre
            case 3: return 365L * dia;  // Este año
            case 0:
            default: return 30L * dia;  // Este mes
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
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
        actualizarTituloTendencia(true);
        actualizarTendencia(separacionesActuales);
    }

    private void updateChipText() {
        if (chipEstesMes != null) {
            chipEstesMes.setText(tendenciaOptions[selectedOption]);
        }
    }

    private void actualizarTituloTendencia(boolean conDatos) {
        if (tvTendenciaTitle == null) return;
        String base = "Tendencia " + tendenciaOptions[selectedOption].toLowerCase();
        tvTendenciaTitle.setText(conDatos ? base : base + " · sin ventas aprobadas");
    }
}
