package com.example.bitbusters.activities.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.AdminHistorialSeparacionAdapter;
import com.example.bitbusters.data.AdminProyectosRepository;
import com.example.bitbusters.data.SeparacionesRepository;
import com.example.bitbusters.models.AdminProyecto;
import com.example.bitbusters.models.AdminHistorialSeparacion;
import com.example.bitbusters.models.AdminSeparacion;
import com.example.bitbusters.utils.AdminPreferencesManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AdminDetallesDeReporteProyectoActivity extends AppCompatActivity {

    private List<String> proyectos = new ArrayList<>();
    private String selectedPeriodo = "Mensual";
    private String selectedProyecto = "";

    private TextView tvResumenMonto, tvResumenSeparaciones, tvResumenAsesores, tvHistorialReporteEmpty;
    private ChipGroup chipGroupProyectosReporte, chipGroupPeriodoProyectoReporte;
    private RecyclerView rvHistorial;
    private AdminHistorialSeparacionAdapter adapter;
    private View[] layoutAsesorReporte;
    private TextView[] tvAsesorReporteNombre;
    private TextView[] tvAsesorReporteStats;
    private ProgressBar[] progressAsesorReporte;
    private ListenerRegistration separacionesListener;
    private ListenerRegistration proyectosListener;
    private List<AdminSeparacion> separacionesActuales = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_detalles_reporte_proyecto);
        setupListeners();
        setupRecyclerView();
        actualizarProyectos();
        updateResumen();
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
        if (proyectosListener != null) {
            proyectosListener.remove();
            proyectosListener = null;
        }
    }

    private void setupRecyclerView() {
        rvHistorial = findViewById(R.id.rvHistorialSeparaciones);
        if (rvHistorial != null) {
            rvHistorial.setLayoutManager(new LinearLayoutManager(this));
            adapter = new AdminHistorialSeparacionAdapter(new ArrayList<>());
            rvHistorial.setAdapter(adapter);
        }
    }

    private void setupListeners() {
        ImageButton backButton = findViewById(R.id.btnBackReporte);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        tvResumenMonto = findViewById(R.id.tvResumenMonto);
        tvResumenSeparaciones = findViewById(R.id.tvResumenSeparaciones);
        tvResumenAsesores = findViewById(R.id.tvResumenAsesores);
        tvHistorialReporteEmpty = findViewById(R.id.tvHistorialReporteEmpty);
        chipGroupProyectosReporte = findViewById(R.id.chipGroupProyectosReporte);
        chipGroupPeriodoProyectoReporte = findViewById(R.id.chipGroupPeriodoProyectoReporte);
        layoutAsesorReporte = new View[] {
                findViewById(R.id.layoutAsesorReporte1),
                findViewById(R.id.layoutAsesorReporte2)
        };
        tvAsesorReporteNombre = new TextView[] {
                findViewById(R.id.tvAsesorReporteNombre1),
                findViewById(R.id.tvAsesorReporteNombre2)
        };
        tvAsesorReporteStats = new TextView[] {
                findViewById(R.id.tvAsesorReporteStats1),
                findViewById(R.id.tvAsesorReporteStats2)
        };
        progressAsesorReporte = new ProgressBar[] {
                findViewById(R.id.progressAsesorReporte1),
                findViewById(R.id.progressAsesorReporte2)
        };

        if (chipGroupPeriodoProyectoReporte != null) {
            chipGroupPeriodoProyectoReporte.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds == null || checkedIds.isEmpty()) return;
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipPeriodoDiarioReporte) {
                    setPeriodo("Diario");
                } else if (checkedId == R.id.chipPeriodoAnualReporte) {
                    setPeriodo("Anual");
                } else {
                    setPeriodo("Mensual");
                }
            });
        }
    }

    private void setPeriodo(String periodo) {
        selectedPeriodo = periodo;
        updateResumen();
    }

    private void updateResumen() {
        double monto = 0;
        int totalSeparaciones = 0;
        Set<String> asesores = new HashSet<>();
        List<AdminHistorialSeparacion> historial = new ArrayList<>();

        List<AdminSeparacion> filtradas = separacionesFiltradas();
        for (AdminSeparacion separacion : filtradas) {
            double montoSeparacion = parseMonto(separacion.getMonto());
            monto += montoSeparacion;
            totalSeparaciones++;

            String asesorKey = !separacion.getUidAsesor().isEmpty()
                    ? separacion.getUidAsesor()
                    : separacion.getAsesorNombre();
            if (asesorKey != null && !asesorKey.trim().isEmpty()) {
                asesores.add(asesorKey.trim());
            }

            historial.add(new AdminHistorialSeparacion(
                    separacion.getId(),
                    selectedPeriodo,
                    formatearSoles(montoSeparacion),
                    1,
                    asesorKey == null || asesorKey.trim().isEmpty() ? 0 : 1,
                    separacion.getFecha(),
                    separacion.getNombreProyecto()
            ));
        }

        if (tvResumenMonto != null) {
            tvResumenMonto.setText(formatearSoles(monto));
        }
        if (tvResumenSeparaciones != null) {
            tvResumenSeparaciones.setText(String.valueOf(totalSeparaciones));
        }
        if (tvResumenAsesores != null) {
            tvResumenAsesores.setText(String.valueOf(asesores.size()));
        }
        if (adapter != null) {
            adapter.setData(historial);
        }
        if (rvHistorial != null) {
            rvHistorial.setVisibility(historial.isEmpty() ? View.GONE : View.VISIBLE);
        }
        if (tvHistorialReporteEmpty != null) {
            tvHistorialReporteEmpty.setVisibility(historial.isEmpty() ? View.VISIBLE : View.GONE);
        }
        actualizarAsesores(filtradas);
    }

    private void actualizarAsesores(List<AdminSeparacion> separaciones) {
        Map<String, AsesorResumen> resumenPorAsesor = new LinkedHashMap<>();
        if (separaciones != null) {
            for (AdminSeparacion separacion : separaciones) {
                if (separacion == null) continue;
                String key = !separacion.getUidAsesor().isEmpty()
                        ? separacion.getUidAsesor()
                        : separacion.getAsesorNombre();
                if (key == null || key.trim().isEmpty()) {
                    key = "asesor_no_registrado";
                }
                String nombre = !separacion.getAsesorNombre().isEmpty()
                        ? separacion.getAsesorNombre()
                        : ("asesor_no_registrado".equals(key) ? "Asesor no registrado" : key);
                AsesorResumen resumen = resumenPorAsesor.get(key);
                if (resumen == null) {
                    resumen = new AsesorResumen(nombre);
                    resumenPorAsesor.put(key, resumen);
                }
                resumen.separaciones++;
                resumen.monto += parseMonto(separacion.getMonto());
            }
        }

        List<AsesorResumen> asesoresOrdenados = new ArrayList<>(resumenPorAsesor.values());
        Collections.sort(asesoresOrdenados, (a, b) -> Double.compare(b.monto, a.monto));

        if (asesoresOrdenados.isEmpty()) {
            mostrarFilaAsesor(0, "Sin asesores con pagos", "0 pagos · S/0", 0);
            ocultarFilaAsesor(1);
            return;
        }

        double max = asesoresOrdenados.get(0).monto;
        for (int i = 0; i < 2; i++) {
            if (i >= asesoresOrdenados.size()) {
                ocultarFilaAsesor(i);
                continue;
            }
            AsesorResumen asesor = asesoresOrdenados.get(i);
            int progreso = max > 0 ? (int) Math.max(5, Math.round((asesor.monto / max) * 100)) : 0;
            mostrarFilaAsesor(
                    i,
                    asesor.nombre,
                    asesor.separaciones + " pagos · " + formatearSolesCompacto(asesor.monto),
                    progreso
            );
        }
    }

    private void mostrarFilaAsesor(int index, String nombre, String stats, int progreso) {
        if (layoutAsesorReporte != null && layoutAsesorReporte[index] != null) {
            layoutAsesorReporte[index].setVisibility(View.VISIBLE);
        }
        if (progressAsesorReporte != null && progressAsesorReporte[index] != null) {
            progressAsesorReporte[index].setVisibility(View.VISIBLE);
            progressAsesorReporte[index].setProgress(progreso);
        }
        if (tvAsesorReporteNombre != null && tvAsesorReporteNombre[index] != null) {
            tvAsesorReporteNombre[index].setText(nombre);
        }
        if (tvAsesorReporteStats != null && tvAsesorReporteStats[index] != null) {
            tvAsesorReporteStats[index].setText(stats);
        }
    }

    private void ocultarFilaAsesor(int index) {
        if (layoutAsesorReporte != null && layoutAsesorReporte[index] != null) {
            layoutAsesorReporte[index].setVisibility(View.GONE);
        }
        if (progressAsesorReporte != null && progressAsesorReporte[index] != null) {
            progressAsesorReporte[index].setVisibility(View.GONE);
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
                        separacionesActuales = separaciones != null
                                ? new ArrayList<>(separaciones)
                                : new ArrayList<>();
                        actualizarProyectos();
                        updateResumen();
                    }

                    @Override
                    public void onError(String mensaje) {
                        separacionesActuales = new ArrayList<>(SeparacionesRepository.getLista());
                        actualizarProyectos();
                        updateResumen();
                    }
                });

        if (proyectosListener != null) {
            proyectosListener.remove();
        }
        proyectosListener = AdminProyectosRepository.escucharPorAdministrador(
                null,
                AdminPreferencesManager.obtenerInmobiliariaId(this),
                new AdminProyectosRepository.ProyectosListener() {
                    @Override
                    public void onProyectosActualizados(List<AdminProyecto> proyectos) {
                        actualizarProyectos();
                        updateResumen();
                    }

                    @Override
                    public void onError(String mensaje) {
                        actualizarProyectos();
                        updateResumen();
                    }
                });
    }

    private void actualizarProyectos() {
        LinkedHashSet<String> nombres = new LinkedHashSet<>();
        for (AdminProyecto proyecto : AdminProyectosRepository.getTodos()) {
            if (proyecto == null) continue;
            String nombre = proyecto.getNombre();
            if (nombre != null && !nombre.trim().isEmpty()) {
                nombres.add(nombre.trim());
            }
        }
        for (AdminSeparacion separacion : separacionesActuales) {
            if (separacion == null) continue;
            String nombre = separacion.getNombreProyecto();
            if (nombre != null && !nombre.trim().isEmpty()) {
                nombres.add(nombre.trim());
            }
        }

        proyectos.clear();
        proyectos.addAll(nombres);
        Collections.sort(proyectos);

        if (proyectos.isEmpty()) {
            selectedProyecto = "";
            renderizarChipsProyecto();
            return;
        }

        if (selectedProyecto == null || selectedProyecto.trim().isEmpty()
                || !proyectos.contains(selectedProyecto)) {
            selectedProyecto = proyectos.get(0);
        }
        renderizarChipsProyecto();
    }

    private void renderizarChipsProyecto() {
        if (chipGroupProyectosReporte == null) return;
        chipGroupProyectosReporte.removeAllViews();

        if (proyectos.isEmpty()) {
            Chip chip = crearChip("Sin proyectos");
            chip.setEnabled(false);
            chip.setChecked(true);
            chipGroupProyectosReporte.addView(chip);
            return;
        }

        for (String proyecto : proyectos) {
            Chip chip = crearChip(proyecto);
            chip.setChecked(proyecto.equals(selectedProyecto));
            chip.setOnClickListener(v -> {
                selectedProyecto = ((Chip) v).getText().toString();
                updateResumen();
            });
            chipGroupProyectosReporte.addView(chip);
        }
    }

    private Chip crearChip(String texto) {
        Chip chip = new Chip(this, null, com.google.android.material.R.style.Widget_Material3_Chip_Filter);
        chip.setText(texto);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(false);
        chip.setEnsureMinTouchTargetSize(true);
        return chip;
    }

    private List<AdminSeparacion> separacionesFiltradas() {
        List<AdminSeparacion> resultado = new ArrayList<>();
        if (selectedProyecto == null || selectedProyecto.trim().isEmpty()) {
            return resultado;
        }

        long ahora = System.currentTimeMillis();
        long inicio = ahora - obtenerDuracionPeriodoMillis();
        for (AdminSeparacion separacion : separacionesActuales) {
            if (separacion == null) continue;
            if (!esPagoConfirmado(separacion)) continue;
            if (!selectedProyecto.equals(separacion.getNombreProyecto())) continue;
            long fecha = fechaParaReporte(separacion);
            if (fecha <= 0 || fecha < inicio || fecha > ahora) continue;
            resultado.add(separacion);
        }
        return resultado;
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
        if ("Diario".equals(selectedPeriodo)) return dia;
        if ("Anual".equals(selectedPeriodo)) return 365L * dia;
        return 30L * dia;
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

    private String formatearSolesCompacto(double monto) {
        if (monto >= 1000) {
            double miles = monto / 1000.0;
            if (Math.abs(miles - Math.round(miles)) < 0.05) {
                return "S/" + Math.round(miles) + "k";
            }
            return "S/" + String.format(Locale.US, "%.1fk", miles);
        }
        return formatearSoles(monto);
    }

    private static class AsesorResumen {
        final String nombre;
        int separaciones;
        double monto;

        AsesorResumen(String nombre) {
            this.nombre = nombre;
        }
    }
}
