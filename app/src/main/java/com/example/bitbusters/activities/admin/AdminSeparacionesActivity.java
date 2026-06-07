package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.AdminSeparacionAdapter;
import com.example.bitbusters.data.SeparacionesRepository;
import com.example.bitbusters.models.AdminSeparacion;
import com.example.bitbusters.utils.AdminPreferencesManager;
import com.example.bitbusters.utils.AdminStorageManager;
import com.example.bitbusters.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Pantalla de lista de separaciones del Administrador.
 * Extiende AdminMainActivity para heredar header y bottom nav.
 *
 * Correcciones Lab 5:
 *  - Usa SeparacionesRepository como fuente única de datos (no AdminDataRepository directo)
 *  - Botón "Simular" crea separación nueva y lanza notificación (Corrección 2)
 *  - onNewIntent/onResume: al recibir "separacion_id" hace scroll al item y lo resalta (Corrección 3)
 */
public class AdminSeparacionesActivity extends AdminMainActivity {

    // Clave del Intent extra para scroll/resalte post-notificación
    public static final String EXTRA_SEPARACION_ID = "separacion_id";

    private Button btnPendientes, btnAprobadas, btnRechazadas;
    private AutoCompleteTextView actvProyectoFilter, actvFechaFilter;
    private RecyclerView rvSeparaciones;
    private AdminSeparacionAdapter adapter;
    private String currentEstadoFilter = null;

    // Proyectos ficticios para la simulación (Corrección 2)
    private static final String[] PROYECTOS_DEMO = {
        "Edificio Los Álamos",
        "Mirador de Surco",
        "Alto San Felipe",
        "Residencial Verde",
        "Torres Unidas"
    };

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
    protected void onResume() {
        super.onResume();
        procesarIntentSeparacionId();
    }

    // ── Inicialización ───────────────────────────────────────────────────────

    private void initializeViews() {
        btnPendientes = findViewById(R.id.btnPendientes);
        btnAprobadas  = findViewById(R.id.btnAprobadas);
        btnRechazadas = findViewById(R.id.btnRechazadas);

        actvProyectoFilter = findViewById(R.id.actvProyectoFilter);
        actvFechaFilter    = findViewById(R.id.actvFechaFilter);
        rvSeparaciones     = findViewById(R.id.rvSeparaciones);

        setupDropdowns();
    }

    private void setupDropdowns() {
        String[] proyectos = {
            "Todos los proyectos",
            "Edificio Los Álamos",
            "Mirador de Surco",
            "Alto San Felipe",
            "Residencial Verde"
        };
        ArrayAdapter<String> adapterProyectos = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, proyectos);
        actvProyectoFilter.setAdapter(adapterProyectos);
        actvProyectoFilter.setOnClickListener(v -> actvProyectoFilter.showDropDown());

        String[] fechas = {"Este mes", "Esta semana", "Este año", "Este bimestre"};
        ArrayAdapter<String> adapterFechas = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, fechas);
        actvFechaFilter.setAdapter(adapterFechas);
        actvFechaFilter.setOnClickListener(v -> actvFechaFilter.showDropDown());
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

    private void setupListeners() {
        if (btnPendientes != null)
            btnPendientes.setOnClickListener(v -> filtrarPorEstado("Pendiente"));
        if (btnAprobadas != null)
            btnAprobadas.setOnClickListener(v -> filtrarPorEstado("Aprobada"));
        if (btnRechazadas != null)
            btnRechazadas.setOnClickListener(v -> filtrarPorEstado("Rechazada"));

        // ── Corrección 2: Botón de simulación de separación ─────────────────
        Button btnSimularSeparacion = findViewById(R.id.btnSimularSeparacion);
        if (btnSimularSeparacion != null) {
            btnSimularSeparacion.setOnClickListener(v -> simularNuevaSeparacion());
        }
    }

    private void setupNotificationsButton() {
        ImageButton btnNotifications = findViewById(R.id.btnNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v ->
                    startActivity(new Intent(this, AdminNotificacionesActivity.class))
            );
        }
    }

    // ── Simulación de nueva separación (Corrección 2) ────────────────────────

    /**
     * Crea una separación ficticia, la agrega al repositorio,
     * refresca el RecyclerView y lanza la notificación correspondiente.
     * El Intent de la notificación incluye el ID para que al tocarla
     * se haga scroll hasta esa separación.
     */
    private void simularNuevaSeparacion() {
        // Generar datos ficticios
        String proyectoAleatorio = PROYECTOS_DEMO[new Random().nextInt(PROYECTOS_DEMO.length)];
        String nuevoId = "SIM_" + System.currentTimeMillis();
        String timestamp = new SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault())
                .format(new Date());

        AdminSeparacion nueva = new AdminSeparacion(
                nuevoId,
                proyectoAleatorio,
                "S/ 5,000",
                timestamp,
                "Cliente Demo",
                "Pendiente"
        );

        // Agregar al repositorio compartido
        SeparacionesRepository.agregar(nueva);

        // Actualizar contadores en SharedPreferences e Internal Storage
        AdminPreferencesManager.incrementarSeparacionesPendientes(this);
        AdminStorageManager.actualizarContador(
                this, AdminStorageManager.CAMPO_SEPARACIONES_PENDIENTES);

        // Limpiar filtro activo y mostrar toda la lista con la nueva separación al inicio
        currentEstadoFilter = null;
        adapter.setData(new ArrayList<>(SeparacionesRepository.getLista()));

        // ── Corrección 2: notificación con separacion_id que abre el DETALLE ──
        // Al tocarla → AdminDetallesSeparacionActivity carga el item por ID para que el admin apruebe
        Intent destinoNotif = new Intent(this, AdminDetallesSeparacionActivity.class);
        destinoNotif.putExtra(EXTRA_SEPARACION_ID, nuevoId);
        NotificationHelper.lanzarNotificacionAdmin(
                this,
                "Nueva Separación",
                "Un asesor ha registrado una nueva separación. Revisa y aprueba el monto",
                NotificationHelper.NOTIF_ADMIN_NUEVA_SEPARACION,
                destinoNotif
        );
    }

    // ── Filtros ────────────────────────────────────────────────────────────

    private void filtrarPorEstado(String estado) {
        currentEstadoFilter = estado;
        seleccionarTab(estado);
        List<AdminSeparacion> filtradas = new ArrayList<>();
        for (AdminSeparacion sep : SeparacionesRepository.getLista()) {
            if (sep.getEstado().equals(estado)) {
                filtradas.add(sep);
            }
        }
        adapter.setData(filtradas);
    }

    private void seleccionarTab(String estadoSeleccionado) {
        // Resetear todos los botones al estilo inactivo
        btnPendientes.setBackground(getDrawable(R.drawable.button_outline_state_bg));
        btnAprobadas.setBackground(getDrawable(R.drawable.button_outline_state_bg));
        btnRechazadas.setBackground(getDrawable(R.drawable.button_outline_state_bg));
        btnPendientes.setTextColor(getColor(R.color.neutral_medium));
        btnAprobadas.setTextColor(getColor(R.color.neutral_medium));
        btnRechazadas.setTextColor(getColor(R.color.neutral_medium));

        // Resaltar el botón seleccionado
        Button seleccionado = null;
        if ("Pendiente".equals(estadoSeleccionado))  seleccionado = btnPendientes;
        else if ("Aprobada".equals(estadoSeleccionado))  seleccionado = btnAprobadas;
        else if ("Rechazada".equals(estadoSeleccionado)) seleccionado = btnRechazadas;

        if (seleccionado != null) {
            seleccionado.setBackground(getDrawable(R.color.brand_deep_blue));
            seleccionado.setTextColor(getColor(android.R.color.white));
        }
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

        // Hacer scroll hasta la posición del ítem resaltado
        int posicion = SeparacionesRepository.getPosicion(separacionId);
        if (posicion >= 0 && rvSeparaciones != null) {
            rvSeparaciones.smoothScrollToPosition(posicion);
        }

        // Remover el extra para evitar re-scroll en el próximo onResume
        getIntent().removeExtra(EXTRA_SEPARACION_ID);
    }
}
