package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.AdminSeparacionAdapter;
import com.example.bitbusters.data.AdminProyectosRepository;
import com.example.bitbusters.data.SeparacionesRepository;
import com.example.bitbusters.models.AdminProyecto;
import com.example.bitbusters.models.AdminSeparacion;
import com.example.bitbusters.utils.AdminPreferencesManager;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Pantalla de lista de separaciones del Administrador.
 * Extiende AdminMainActivity para heredar header y bottom nav.
 *
 * Correcciones Lab 5:
 *  - Usa SeparacionesRepository como fuente única de datos (no AdminDataRepository directo)
 *  - onNewIntent/onResume: al recibir "separacion_id" hace scroll al item y lo resalta (Corrección 3)
 */
public class AdminSeparacionesActivity extends AdminMainActivity {

    // Clave del Intent extra para scroll/resalte post-notificación
    public static final String EXTRA_SEPARACION_ID = "separacion_id";

    private ChipGroup chipGroupEstadosSeparacion, chipGroupProyectoFilter, chipGroupFechaFilter;
    private Chip chipEstadoTodos, chipEstadoPendientes, chipEstadoAprobadas, chipEstadoRechazadas;
    private TextView tvSeparacionesEmpty;
    private RecyclerView rvSeparaciones;
    private AdminSeparacionAdapter adapter;
    private String currentEstadoFilter = "Pendiente";
    private String currentProyectoFilter = "Todos los proyectos";
    private String currentFechaFilter = "Todo el tiempo";
    private ListenerRegistration separacionesListener;
    private ListenerRegistration proyectosListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_separaciones);
        setupHeaderListeners();
        setupBottomNavigation(R.id.nav_separaciones);
        setupNotificationsButton();
        initializeViews();
        setupListeners();
        setupRecyclerView();
    }

    /**
     * onNewIntent se llama cuando la Activity ya está viva y se abre de nuevo
     * (p.e. al tocar la notificación con FLAG_ACTIVITY_CLEAR_TOP).
     * Actualiza el Intent para que onResume() lo procese.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Actualiza el intent actual para que onResume lo lea
    }

    /**
     * Se llama cada vez que la Activity vuelve al frente.
     * Verifica si hay un "separacion_id" en el Intent para hacer scroll y resaltar.
     */
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

    @Override
    protected void onResume() {
        super.onResume();
        renderSeparaciones();
        procesarIntentSeparacionId();
    }

    // ── Inicialización ───────────────────────────────────────────────────────

    private void initializeViews() {
        chipGroupEstadosSeparacion = findViewById(R.id.chipGroupEstadosSeparacion);
        chipGroupProyectoFilter = findViewById(R.id.chipGroupProyectoFilter);
        chipGroupFechaFilter = findViewById(R.id.chipGroupFechaFilter);
        chipEstadoTodos = findViewById(R.id.chipEstadoTodos);
        chipEstadoPendientes = findViewById(R.id.chipEstadoPendientes);
        chipEstadoAprobadas = findViewById(R.id.chipEstadoAprobadas);
        chipEstadoRechazadas = findViewById(R.id.chipEstadoRechazadas);

        rvSeparaciones     = findViewById(R.id.rvSeparaciones);
        tvSeparacionesEmpty = findViewById(R.id.tvSeparacionesEmpty);

        setupChips();
    }

    private void setupChips() {
        if (chipGroupEstadosSeparacion != null) {
            chipGroupEstadosSeparacion.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds == null || checkedIds.isEmpty()) return;
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipEstadoTodos) {
                    currentEstadoFilter = null;
                } else if (checkedId == R.id.chipEstadoAprobadas) {
                    currentEstadoFilter = "Aprobada";
                } else if (checkedId == R.id.chipEstadoRechazadas) {
                    currentEstadoFilter = "Rechazada";
                } else {
                    currentEstadoFilter = "Pendiente";
                }
                renderSeparaciones();
            });
        }

        if (chipGroupFechaFilter != null) {
            chipGroupFechaFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds == null || checkedIds.isEmpty()) return;
                Chip selected = group.findViewById(checkedIds.get(0));
                currentFechaFilter = selected != null ? selected.getText().toString() : "Todo el tiempo";
                renderSeparaciones();
            });
        }

        actualizarOpcionesProyecto();
    }

    private void setupRecyclerView() {
        if (rvSeparaciones == null) return;

        // ── Corrección 2: usar SeparacionesRepository (fuente única de verdad) ──
        List<AdminSeparacion> listaViva = SeparacionesRepository.getLista();

        rvSeparaciones.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminSeparacionAdapter(listaViva, separacion -> {
            // Al hacer click en un ítem → abrir detalle pasando el ID
            Intent intent = new Intent(AdminSeparacionesActivity.this,
                    AdminDetallesSeparacionActivity.class);
            intent.putExtra(EXTRA_SEPARACION_ID, separacion.getId());
            // También pasar los extras legacy por si algo más los usa
            intent.putExtra("nombreProyecto", separacion.getNombreProyecto());
            intent.putExtra("precio",         separacion.getMonto());
            intent.putExtra("fecha",          separacion.getFecha());
            intent.putExtra("asesor",         separacion.getCliente());
            startActivity(intent);
        });
        rvSeparaciones.setAdapter(adapter);
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
                        actualizarOpcionesProyecto();
                        renderSeparaciones();
                    }

                    @Override
                    public void onError(String mensaje) {
                        actualizarOpcionesProyecto();
                        renderSeparaciones();
                    }
                });

        if (proyectosListener != null) {
            proyectosListener.remove();
        }
        String inmobiliariaId = AdminPreferencesManager.obtenerInmobiliariaId(this);
        proyectosListener = AdminProyectosRepository.escucharPorAdministrador(
                null,
                inmobiliariaId,
                new AdminProyectosRepository.ProyectosListener() {
                    @Override
                    public void onProyectosActualizados(List<AdminProyecto> proyectos) {
                        actualizarOpcionesProyecto();
                        renderSeparaciones();
                    }

                    @Override
                    public void onError(String mensaje) {
                        actualizarOpcionesProyecto();
                        renderSeparaciones();
                    }
                });
    }

    private void setupListeners() {
    }

    private void setupNotificationsButton() {
        ImageButton btnNotifications = findViewById(R.id.btnNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v ->
                    startActivity(new Intent(this, AdminNotificacionesActivity.class))
            );
        }
    }

    // ── Filtros ────────────────────────────────────────────────────────────

    private void filtrarPorEstado(String estado) {
        currentEstadoFilter = estado;
        seleccionarChipEstado(estado);
        renderSeparaciones();
    }

    private void renderSeparaciones() {
        if (adapter == null) return;
        List<AdminSeparacion> filtradas = new ArrayList<>();
        for (AdminSeparacion sep : SeparacionesRepository.getLista()) {
            if (cumpleFiltroEstado(sep)
                    && cumpleFiltroProyecto(sep)
                    && cumpleFiltroFecha(sep)) {
                filtradas.add(sep);
            }
        }
        adapter.setData(filtradas);
        actualizarEmptyState(filtradas.isEmpty());
        actualizarContadoresTabs();
        seleccionarChipEstado(currentEstadoFilter);
    }

    private boolean cumpleFiltroEstado(AdminSeparacion separacion) {
        if (currentEstadoFilter == null || currentEstadoFilter.trim().isEmpty()) return true;
        return separacion != null && currentEstadoFilter.equals(separacion.getEstado());
    }

    private boolean cumpleFiltroProyecto(AdminSeparacion separacion) {
        if (currentProyectoFilter == null
                || currentProyectoFilter.trim().isEmpty()
                || "Todos los proyectos".equals(currentProyectoFilter)) {
            return true;
        }
        return separacion != null && currentProyectoFilter.equals(separacion.getNombreProyecto());
    }

    private boolean cumpleFiltroFecha(AdminSeparacion separacion) {
        if (currentFechaFilter == null
                || currentFechaFilter.trim().isEmpty()
                || "Todo el tiempo".equals(currentFechaFilter)) {
            return true;
        }
        long fecha = fechaParaFiltro(separacion);
        if (fecha <= 0) return false;
        long ahora = System.currentTimeMillis();
        long inicio = ahora - obtenerDuracionFiltroMillis();
        return fecha >= inicio && fecha <= ahora;
    }

    private long fechaParaFiltro(AdminSeparacion separacion) {
        if (separacion == null) return 0L;
        if (separacion.getFechaActualizacionMillis() > 0) {
            return separacion.getFechaActualizacionMillis();
        }
        return separacion.getFechaRegistroMillis();
    }

    private long obtenerDuracionFiltroMillis() {
        long dia = 24L * 60L * 60L * 1000L;
        if ("Esta semana".equals(currentFechaFilter)) return 7L * dia;
        if ("Este bimestre".equals(currentFechaFilter)) return 60L * dia;
        if ("Este año".equals(currentFechaFilter)) return 365L * dia;
        return 30L * dia;
    }

    private void actualizarOpcionesProyecto() {
        if (chipGroupProyectoFilter == null) return;

        LinkedHashSet<String> nombres = new LinkedHashSet<>();
        for (AdminProyecto proyecto : AdminProyectosRepository.getTodos()) {
            if (proyecto == null) continue;
            String nombre = proyecto.getNombre();
            if (nombre != null && !nombre.trim().isEmpty()) {
                nombres.add(nombre.trim());
            }
        }
        for (AdminSeparacion sep : SeparacionesRepository.getLista()) {
            if (sep == null) continue;
            String proyecto = sep.getNombreProyecto();
            if (proyecto != null && !proyecto.trim().isEmpty()) {
                nombres.add(proyecto.trim());
            }
        }

        List<String> opciones = new ArrayList<>(nombres);
        Collections.sort(opciones);
        opciones.add(0, "Todos los proyectos");

        if (!opciones.contains(currentProyectoFilter)) {
            currentProyectoFilter = "Todos los proyectos";
        }

        chipGroupProyectoFilter.removeAllViews();
        for (String opcion : opciones) {
            Chip chip = crearChipFiltro(opcion);
            chip.setChecked(opcion.equals(currentProyectoFilter));
            chip.setOnClickListener(v -> {
                currentProyectoFilter = ((Chip) v).getText().toString();
                renderSeparaciones();
            });
            chipGroupProyectoFilter.addView(chip);
        }
    }

    private void actualizarContadoresTabs() {
        int total = 0;
        int pendientes = 0;
        int aprobadas = 0;
        int rechazadas = 0;

        for (AdminSeparacion sep : SeparacionesRepository.getLista()) {
            if (!cumpleFiltroProyecto(sep) || !cumpleFiltroFecha(sep)) continue;
            total++;
            if ("Aprobada".equalsIgnoreCase(sep.getEstado())) {
                aprobadas++;
            } else if ("Rechazada".equalsIgnoreCase(sep.getEstado())) {
                rechazadas++;
            } else {
                pendientes++;
            }
        }

        if (chipEstadoTodos != null) chipEstadoTodos.setText("Todos (" + total + ")");
        if (chipEstadoPendientes != null) chipEstadoPendientes.setText("Pendientes (" + pendientes + ")");
        if (chipEstadoAprobadas != null) chipEstadoAprobadas.setText("Aprobadas (" + aprobadas + ")");
        if (chipEstadoRechazadas != null) chipEstadoRechazadas.setText("Rechazadas (" + rechazadas + ")");
    }

    private void seleccionarChipEstado(String estadoSeleccionado) {
        if (chipGroupEstadosSeparacion == null) return;
        int chipId;
        if ("Aprobada".equals(estadoSeleccionado)) {
            chipId = R.id.chipEstadoAprobadas;
        } else if ("Rechazada".equals(estadoSeleccionado)) {
            chipId = R.id.chipEstadoRechazadas;
        } else if ("Pendiente".equals(estadoSeleccionado)) {
            chipId = R.id.chipEstadoPendientes;
        } else {
            chipId = R.id.chipEstadoTodos;
        }
        if (chipGroupEstadosSeparacion.getCheckedChipId() != chipId) {
            chipGroupEstadosSeparacion.check(chipId);
        }
    }

    private Chip crearChipFiltro(String texto) {
        Chip chip = new Chip(this, null, com.google.android.material.R.style.Widget_Material3_Chip_Filter);
        chip.setText(texto);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(false);
        chip.setEnsureMinTouchTargetSize(true);
        return chip;
    }

    private void actualizarEmptyState(boolean listaVacia) {
        if (tvSeparacionesEmpty == null || rvSeparaciones == null) return;

        tvSeparacionesEmpty.setText(obtenerMensajeVacio());
        tvSeparacionesEmpty.setVisibility(listaVacia ? View.VISIBLE : View.GONE);
        rvSeparaciones.setVisibility(listaVacia ? View.GONE : View.VISIBLE);
    }

    private String obtenerMensajeVacio() {
        if ("Pendiente".equals(currentEstadoFilter)) return "No hay separaciones pendientes";
        if ("Aprobada".equals(currentEstadoFilter)) return "No hay separaciones aprobadas";
        if ("Rechazada".equals(currentEstadoFilter)) return "No hay separaciones rechazadas";
        if (!"Todos los proyectos".equals(currentProyectoFilter)) {
            return "No hay separaciones para " + currentProyectoFilter;
        }
        return "No hay separaciones para los filtros seleccionados";
    }

    // ── Scroll / Resalte tras notificación (Corrección 3) ───────────────────

    /**
     * Si el Intent contiene "separacion_id", limpia el filtro activo,
     * muestra toda la lista y hace scroll hasta esa separación.
     * Remueve el extra para que no se repita al próximo onResume().
     */
    private void procesarIntentSeparacionId() {
        if (getIntent() == null) return;
        String separacionId = getIntent().getStringExtra(EXTRA_SEPARACION_ID);
        if (separacionId == null || separacionId.isEmpty()) return;

        // Limpiar filtro y mostrar toda la lista (para que el ítem sea visible)
        currentEstadoFilter = null;
        if (adapter != null) {
            adapter.setData(new ArrayList<>(SeparacionesRepository.getLista()));
        }
        actualizarEmptyState(SeparacionesRepository.getLista().isEmpty());
        seleccionarChipEstado(currentEstadoFilter);
        actualizarContadoresTabs();

        // Hacer scroll hasta la posición del ítem resaltado
        int posicion = SeparacionesRepository.getPosicion(separacionId);
        if (posicion >= 0 && rvSeparaciones != null) {
            rvSeparaciones.smoothScrollToPosition(posicion);
        }

        // Remover el extra para evitar re-scroll en el próximo onResume
        getIntent().removeExtra(EXTRA_SEPARACION_ID);
    }
}
