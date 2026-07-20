package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.R;
import com.example.bitbusters.data.AdminProyectosRepository;
import com.example.bitbusters.data.FirestoreAsesoresRepository;
import com.example.bitbusters.data.SeparacionesRepository;
import com.example.bitbusters.models.AdminAsesorInmobiliaria;
import com.example.bitbusters.models.AdminProyecto;
import com.example.bitbusters.models.AdminSeparacion;
import com.example.bitbusters.utils.AdminPreferencesManager;
import com.example.bitbusters.utils.NotificationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Pantalla principal del Administrador de Inmobiliaria.
 * Actúa como clase base para las Activities hijas del admin (via herencia).
 *
 * Responsabilidades:
 *  - Inicializar canal de notificaciones admin y solicitar permiso (Lab 5)
 *  - Cargar y mostrar datos del admin desde AdminPreferencesManager
 *  - Proveer setupBottomNavigation() y setupHeaderListeners() a las subclases
 */
public class AdminMainActivity extends AppCompatActivity {

    private ListenerRegistration dashboardProyectosListener;
    private ListenerRegistration dashboardSeparacionesListener;
    private final FirestoreAsesoresRepository dashboardAsesoresRepository = new FirestoreAsesoresRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        // ── Lab 5: Crear canal admin y solicitar permiso de notificaciones ──
        NotificationHelper.crearCanalAdmin(this);
        NotificationHelper.solicitarPermiso(this);

        // Configurar navegación y listeners de la pantalla principal
        setupBottomNavigation(R.id.nav_dashboard);
        setupQuickActionListeners();
        setupHeaderListeners();

        // ── Lab 5 (Parte 1): Leer prefs del admin y mostrar en la cabecera ──
        cargarDatosAdmin();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (esDashboardActivo()) {
            iniciarDashboardRealtime();
            cargarAsesoresDashboard();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        detenerDashboardRealtime();
    }

    /**
     * Lee los datos guardados en AdminPreferencesManager y los refleja
     * en los elementos de la cabecera de AdminMainActivity.
     *
     * Actualiza:
     *  - tvGreeting      → "Hola, [nombre del admin]"
     *  - tvRealEstateName → nombre de la inmobiliaria
     *  - tvAvatar         → iniciales del nombre (máximo 2 letras)
     */
    private void cargarDatosAdmin() {
        String nombre       = AdminPreferencesManager.obtenerNombre(this);
        String inmobiliaria = AdminPreferencesManager.obtenerInmobiliaria(this);

        // Saludo personalizado con el nombre guardado
        TextView tvGreeting = findViewById(R.id.tvGreeting);
        if (tvGreeting != null) {
            tvGreeting.setText("Hola, " + nombre);
        }

        // Nombre de la inmobiliaria en el header
        TextView tvRealEstateName = findViewById(R.id.tvRealEstateName);
        if (tvRealEstateName != null) {
            tvRealEstateName.setText(inmobiliaria);
        }

        // Iniciales del admin en el avatar circular
        TextView tvAvatar = findViewById(R.id.tvAvatar);
        if (tvAvatar != null) {
            tvAvatar.setText(obtenerIniciales(nombre));
        }
    }

    /**
     * Genera las iniciales (máximo 2 caracteres) del nombre completo del admin.
     * Ejemplo: "Juan García" → "JG" | "Administrador" → "AD"
     */
    private String obtenerIniciales(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) return "AD";
        String[] partes = nombre.trim().split("\\s+");
        StringBuilder iniciales = new StringBuilder();
        for (int i = 0; i < Math.min(2, partes.length); i++) {
            if (!partes[i].isEmpty()) {
                iniciales.append(partes[i].charAt(0));
            }
        }
        return iniciales.toString().toUpperCase();
    }

    /**
     * Configura los listeners del header (botón de notificaciones, avatar y nombre de inmobiliaria).
     * Método protected para que las Activities hijas puedan reutilizarlo con su propio layout.
     */
    protected void setupHeaderListeners() {
        cargarDatosAdmin();

        // Botón de notificaciones → abre AdminNotificacionesActivity
        ImageButton btnNotifications = findViewById(R.id.btnNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v ->
                    startActivity(new Intent(AdminMainActivity.this, AdminNotificacionesActivity.class))
            );
        }

        // Avatar del admin → abre AdminPerfilActivity
        TextView tvAvatar = findViewById(R.id.tvAvatar);
        if (tvAvatar != null) {
            tvAvatar.setOnClickListener(v ->
                    startActivity(new Intent(AdminMainActivity.this, AdminPerfilActivity.class))
            );
        }

        // Nombre de la inmobiliaria → abre AdminDetallesInmobiliariaActivity
        TextView tvRealEstateName = findViewById(R.id.tvRealEstateName);
        if (tvRealEstateName != null) {
            tvRealEstateName.setOnClickListener(v ->
                    startActivity(new Intent(AdminMainActivity.this, AdminDetallesInmobiliariaActivity.class))
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosAdmin();
    }

    private boolean esDashboardActivo() {
        return findViewById(R.id.tvDashboardProyectosActivos) != null;
    }

    private void iniciarDashboardRealtime() {
        detenerDashboardRealtime();
        String inmobiliariaId = AdminPreferencesManager.obtenerInmobiliariaId(this);

        dashboardProyectosListener = AdminProyectosRepository.escucharPorAdministrador(
                null,
                inmobiliariaId,
                new AdminProyectosRepository.ProyectosListener() {
                    @Override
                    public void onProyectosActualizados(List<AdminProyecto> proyectos) {
                        actualizarProyectosDashboard(proyectos);
                    }

                    @Override
                    public void onError(String mensaje) {
                        actualizarProyectosDashboard(AdminProyectosRepository.getTodos());
                    }
                });

        dashboardSeparacionesListener = SeparacionesRepository.escucharDesdeFirestore(
                inmobiliariaId,
                new SeparacionesRepository.SeparacionesListener() {
                    @Override
                    public void onSeparacionesActualizadas(List<AdminSeparacion> separaciones) {
                        actualizarSeparacionesDashboard(separaciones);
                    }

                    @Override
                    public void onError(String mensaje) {
                        actualizarSeparacionesDashboard(SeparacionesRepository.getLista());
                    }
                });
    }

    private void detenerDashboardRealtime() {
        if (dashboardProyectosListener != null) {
            dashboardProyectosListener.remove();
            dashboardProyectosListener = null;
        }
        if (dashboardSeparacionesListener != null) {
            dashboardSeparacionesListener.remove();
            dashboardSeparacionesListener = null;
        }
    }

    private void cargarAsesoresDashboard() {
        dashboardAsesoresRepository.obtenerAsesoresRegistrados(this, new FirestoreAsesoresRepository.AsesoresCallback() {
            @Override
            public void onSuccess(List<AdminAsesorInmobiliaria> asesores) {
                int activos = 0;
                if (asesores != null) {
                    for (AdminAsesorInmobiliaria asesor : asesores) {
                        if (asesor != null && "Activo".equalsIgnoreCase(asesor.getEstado())) {
                            activos++;
                        }
                    }
                }
                setDashboardText(R.id.tvDashboardAsesoresActivos, String.valueOf(activos));
            }

            @Override
            public void onError(String mensaje) {
                setDashboardText(R.id.tvDashboardAsesoresActivos, "0");
            }
        });
    }

    private void actualizarProyectosDashboard(List<AdminProyecto> proyectos) {
        int activos = 0;
        if (proyectos != null) {
            for (AdminProyecto proyecto : proyectos) {
                if (proyecto == null) continue;
                boolean activo = proyecto.getActivo() == null || Boolean.TRUE.equals(proyecto.getActivo());
                boolean visible = proyecto.getVisible() == null || Boolean.TRUE.equals(proyecto.getVisible());
                if (activo && visible) activos++;
            }
        }
        setDashboardText(R.id.tvDashboardProyectosActivos, String.valueOf(activos));
    }

    private void actualizarSeparacionesDashboard(List<AdminSeparacion> separaciones) {
        int pendientes = 0;
        double ventasMes = 0;
        long ahora = System.currentTimeMillis();
        long inicioMes = ahora - 30L * 24L * 60L * 60L * 1000L;

        List<AdminSeparacion> recientes = separaciones != null
                ? new ArrayList<>(separaciones)
                : new ArrayList<>();

        for (AdminSeparacion separacion : recientes) {
            if (separacion == null) continue;
            String estado = separacion.getEstado() == null ? "" : separacion.getEstado();
            if ("Pendiente".equalsIgnoreCase(estado)) pendientes++;
            long fecha = fechaDashboard(separacion);
            if (esPagoConfirmado(separacion)
                    && fecha >= inicioMes
                    && fecha <= ahora) {
                ventasMes += parseMonto(separacion.getMonto());
            }
        }

        setDashboardText(R.id.tvDashboardSeparacionesPendientes, String.valueOf(pendientes));
        setDashboardText(R.id.tvDashboardVentasMes, formatearSoles(ventasMes));
        actualizarActividadReciente(recientes);
    }

    private void actualizarActividadReciente(List<AdminSeparacion> separaciones) {
        Collections.sort(separaciones, (a, b) -> Long.compare(fechaDashboard(b), fechaDashboard(a)));

        int[] titulos = {
                R.id.tvActividadTitulo1,
                R.id.tvActividadTitulo2,
                R.id.tvActividadTitulo3
        };
        int[] tiempos = {
                R.id.tvActividadTiempo1,
                R.id.tvActividadTiempo2,
                R.id.tvActividadTiempo3
        };

        if (separaciones.isEmpty()) {
            setDashboardText(titulos[0], "No hay actividad reciente");
            setDashboardText(tiempos[0], "");
            setDashboardText(titulos[1], "");
            setDashboardText(tiempos[1], "");
            setDashboardText(titulos[2], "");
            setDashboardText(tiempos[2], "");
            return;
        }

        for (int i = 0; i < titulos.length; i++) {
            if (i >= separaciones.size()) {
                setDashboardText(titulos[i], "");
                setDashboardText(tiempos[i], "");
                continue;
            }
            AdminSeparacion separacion = separaciones.get(i);
            setDashboardText(titulos[i], textoActividad(separacion));
            setDashboardText(tiempos[i], tiempoRelativo(fechaDashboard(separacion)));
        }
    }

    private String textoActividad(AdminSeparacion separacion) {
        String proyecto = separacion.getNombreProyecto();
        if (proyecto == null || proyecto.trim().isEmpty()) proyecto = "proyecto";
        String estado = separacion.getEstado() == null ? "" : separacion.getEstado();
        if (esPagoConfirmado(separacion)) return "Pago registrado · " + proyecto;
        if ("Aprobada".equalsIgnoreCase(estado)) return "Separación aprobada · " + proyecto;
        if ("Rechazada".equalsIgnoreCase(estado)) return "Separación rechazada · " + proyecto;
        return "Separación pendiente · " + proyecto;
    }

    private long fechaDashboard(AdminSeparacion separacion) {
        if (separacion == null) return 0L;
        if (separacion.getFechaActualizacionMillis() > 0) return separacion.getFechaActualizacionMillis();
        return separacion.getFechaRegistroMillis();
    }

    private boolean esPagoConfirmado(AdminSeparacion separacion) {
        if (separacion == null) return false;
        String estadoPago = separacion.getEstadoPago();
        String estado = separacion.getEstado();
        return "Pagado".equalsIgnoreCase(estadoPago)
                || "Pagada".equalsIgnoreCase(estadoPago)
                || "pago_registrado".equalsIgnoreCase(estado);
    }

    private String tiempoRelativo(long fecha) {
        if (fecha <= 0) return "";
        long diff = Math.max(0, System.currentTimeMillis() - fecha);
        long minuto = 60L * 1000L;
        long hora = 60L * minuto;
        long dia = 24L * hora;
        if (diff < hora) {
            long minutos = Math.max(1, diff / minuto);
            return "Hace " + minutos + " min";
        }
        if (diff < dia) {
            long horas = Math.max(1, diff / hora);
            return "Hace " + horas + " h";
        }
        long dias = Math.max(1, diff / dia);
        return "Hace " + dias + " d";
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

    private void setDashboardText(int id, String value) {
        TextView textView = findViewById(id);
        if (textView != null) textView.setText(value);
    }

    /** Configura las tarjetas de acciones rápidas del dashboard principal. */
    private void setupQuickActionListeners() {
        // Crear proyecto → AdminCrearProyectoActivity
        MaterialCardView cardCreateProject = findViewById(R.id.cardCreateProject);
        if (cardCreateProject != null) {
            cardCreateProject.setOnClickListener(v ->
                    startActivity(new Intent(AdminMainActivity.this, AdminCrearProyectoActivity.class))
            );
        }

        // Ver separaciones → AdminSeparacionesActivity
        MaterialCardView cardViewSeparations = findViewById(R.id.cardViewSeparations);
        if (cardViewSeparations != null) {
            cardViewSeparations.setOnClickListener(v ->
                    startActivity(new Intent(AdminMainActivity.this, AdminSeparacionesActivity.class))
            );
        }

        // Ver reportes → AdminReportesActivity
        MaterialCardView cardViewReports = findViewById(R.id.cardViewReports);
        if (cardViewReports != null) {
            cardViewReports.setOnClickListener(v ->
                    startActivity(new Intent(AdminMainActivity.this, AdminReportesActivity.class))
            );
        }

        // Asignar asesor → AdminProyectosActivity (lista de proyectos para asignar)
        MaterialCardView cardAssignAdvisor = findViewById(R.id.cardAssignAdvisor);
        if (cardAssignAdvisor != null) {
            cardAssignAdvisor.setOnClickListener(v ->
                    startActivity(new Intent(AdminMainActivity.this, AdminProyectosActivity.class))
            );
        }
    }

    /**
     * Configura la barra de navegación inferior.
     * Se llama también desde las Activities hijas pasando su ítem seleccionado.
     *
     * @param selectedItemId ID del ítem del menú que debe quedar resaltado.
     */
    protected void setupBottomNavigation(int selectedItemId) {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(selectedItemId);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                if (!(this instanceof AdminMainActivity) || selectedItemId != R.id.nav_dashboard) {
                    startActivity(new Intent(this, AdminMainActivity.class));
                    finish();
                }
            } else if (id == R.id.nav_proyectos) {
                startActivity(new Intent(this, AdminProyectosActivity.class));
                finish();
            } else if (id == R.id.nav_separaciones) {
                startActivity(new Intent(this, AdminSeparacionesActivity.class));
                finish();
            } else if (id == R.id.nav_reportes) {
                startActivity(new Intent(this, AdminReportesActivity.class));
                finish();
            }
            return true;
        });
    }
}
