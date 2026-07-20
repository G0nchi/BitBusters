package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.bitbusters.R;
import com.example.bitbusters.data.SeparacionesRepository;
import com.example.bitbusters.models.AdminSeparacion;
import com.example.bitbusters.utils.AdminPreferencesManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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
    private ChipGroup chipGroupPeriodoReportes;
    private TextView tvReporteVentas;
    private TextView tvReporteAprobadas;
    private TextView tvReportePendientes;
    private TextView tvReporteRechazadas;
    private View[] layoutVentasProyecto;
    private TextView[] tvVentaProyectoNombre;
    private TextView[] tvVentaProyectoMonto;
    private ProgressBar[] progressVentaProyecto;
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
        chipGroupPeriodoReportes = findViewById(R.id.chipGroupPeriodoReportes);
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
    }

    private void setupListeners() {
        if (chipGroupPeriodoReportes != null) {
            chipGroupPeriodoReportes.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds == null || checkedIds.isEmpty()) return;
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipReporteSemana) {
                    selectedOption = 1;
                } else if (checkedId == R.id.chipReporteSemestre) {
                    selectedOption = 2;
                } else if (checkedId == R.id.chipReporteAnio) {
                    selectedOption = 3;
                } else {
                    selectedOption = 0;
                }
                actualizarKpis(separacionesActuales);
            });
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
        double ventasPagadas = 0;

        if (separaciones != null) {
            for (AdminSeparacion separacion : separaciones) {
                if (!cumplePeriodo(separacion)) continue;
                String estado = separacion.getEstado() == null ? "" : separacion.getEstado();
                if ("Aprobada".equalsIgnoreCase(estado)) {
                    aprobadas++;
                    if (esPagoConfirmado(separacion)) {
                        ventasPagadas += parseMonto(separacion.getMonto());
                    }
                } else if ("Rechazada".equalsIgnoreCase(estado)) {
                    rechazadas++;
                } else {
                    pendientes++;
                }
            }
        }

        setText(tvReporteVentas, formatearSoles(ventasPagadas));
        setText(tvReporteAprobadas, String.valueOf(aprobadas));
        setText(tvReportePendientes, String.valueOf(pendientes));
        setText(tvReporteRechazadas, String.valueOf(rechazadas));
        actualizarVentasPorProyecto(separaciones);
    }

    private void actualizarVentasPorProyecto(List<AdminSeparacion> separaciones) {
        Map<String, Double> ventasPorProyecto = new LinkedHashMap<>();
        if (separaciones != null) {
            for (AdminSeparacion separacion : separaciones) {
                if (!esPagoConfirmado(separacion)) continue;
                if (!cumplePeriodo(separacion)) continue;
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
            mostrarFilaVentaProyecto(0, "Sin pagos registrados", 0, 0);
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

    private boolean cumplePeriodo(AdminSeparacion separacion) {
        if (separacion == null) return false;
        long ahora = System.currentTimeMillis();
        long fecha = fechaParaReporte(separacion);
        if (fecha <= 0) return false;
        long inicio = ahora - obtenerDuracionPeriodoMillis();
        return fecha >= inicio && fecha <= ahora;
    }

    private boolean esPagoConfirmado(AdminSeparacion separacion) {
        if (separacion == null) return false;
        String estadoPago = separacion.getEstadoPago();
        String estado = separacion.getEstado();
        return "Pagado".equalsIgnoreCase(estadoPago)
                || "Pagada".equalsIgnoreCase(estadoPago)
                || "pago_registrado".equalsIgnoreCase(estado);
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

    private void setText(TextView textView, String value) {
        if (textView != null) {
            textView.setText(value);
        }
    }
}
