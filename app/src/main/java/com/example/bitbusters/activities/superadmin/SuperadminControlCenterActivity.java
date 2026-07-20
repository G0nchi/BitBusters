package com.example.bitbusters.activities.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.bitbusters.utils.ImmersiveMode;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.TextView;

import com.example.bitbusters.R;
import com.example.bitbusters.activities.access.LoginActivity;
import com.example.bitbusters.data.SuperadminMetricsRepository;
import com.example.bitbusters.data.SuperadminMetricsSnapshot;
import com.example.bitbusters.utils.MoneyParser;
import com.example.bitbusters.utils.NotificationHelper;
import com.example.bitbusters.utils.PeriodoUtils;
import com.example.bitbusters.utils.PreferencesManager;

import java.util.Locale;

public class SuperadminControlCenterActivity extends AppCompatActivity {

    // Evita que las notificaciones se disparen en cada onResume dentro de la misma sesión
    private static boolean notificacionesEnviadas = false;

    private final SuperadminMetricsRepository metricsRepository = new SuperadminMetricsRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_superadmin_control_center);

        NotificationHelper.crearCanal(this);
        NotificationHelper.solicitarPermiso(this);

        bindInsets();
        setupClicks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!notificacionesEnviadas) {
            notificacionesEnviadas = true;
            NotificationHelper.notificarNuevaAprobacion(this);
            NotificationHelper.notificarNuevoUsuario(this);
            NotificationHelper.notificarLogCritico(this);
        }
        actualizarBadgeNotificaciones();
        cargarMetricasReales();
    }

    private void cargarMetricasReales() {
        metricsRepository.loadSnapshot(this::bindMetrics);
    }

    private void bindMetrics(SuperadminMetricsSnapshot snapshot) {
        if (isFinishing() || isDestroyed()) return;

        bindEmpresas(snapshot);
        bindAsesores(snapshot);
        bindIngresosMes(snapshot);
        bindReservasMes(snapshot);

        setText(R.id.quickUsersValue, String.valueOf(snapshot.totalUsuarios()));
        setText(R.id.quickApprovalsValue, String.valueOf(snapshot.aprobacionesPendientes()));
        setText(R.id.quickLogsValue, String.valueOf(snapshot.totalLogs()));
        setText(R.id.quickReportsValue, String.valueOf(snapshot.reservasEnRango(null, null)));
    }

    private void bindEmpresas(SuperadminMetricsSnapshot snapshot) {
        setText(R.id.dashboardEmpresasValue, String.valueOf(snapshot.empresasDistintas()));
    }

    private void bindAsesores(SuperadminMetricsSnapshot snapshot) {
        setText(R.id.dashboardAsesoresValue, String.valueOf(snapshot.totalAsesores()));
        int nuevosHoy = snapshot.asesoresRegistradosDesde(PeriodoUtils.inicioDeHoy());
        TextView badge = findViewById(R.id.dashboardAsesoresBadge);
        if (badge != null) {
            if (nuevosHoy > 0) {
                badge.setText(getString(R.string.sa_dashboard_delta_hoy, nuevosHoy));
                badge.setVisibility(View.VISIBLE);
            } else {
                badge.setVisibility(View.GONE);
            }
        }
    }

    private void bindIngresosMes(SuperadminMetricsSnapshot snapshot) {
        Double ingresosMes = snapshot.ingresosEnRango(PeriodoUtils.inicioDeMes(0), PeriodoUtils.ahora());
        setText(R.id.dashboardIngresosValue, ingresosMes != null ? MoneyParser.formatCompacto(ingresosMes) : "S/ 0");

        Double ingresosMesAnterior = snapshot.ingresosEnRango(PeriodoUtils.inicioDeMes(-1), PeriodoUtils.inicioDeMes(0));
        TextView badge = findViewById(R.id.dashboardIngresosBadge);
        if (badge != null) {
            if (ingresosMes != null && ingresosMesAnterior != null && ingresosMesAnterior > 0) {
                double variacion = ((ingresosMes - ingresosMesAnterior) / ingresosMesAnterior) * 100.0;
                badge.setText(getString(R.string.sa_dashboard_delta_mes_pasado, formatVariacion(variacion)));
                badge.setVisibility(View.VISIBLE);
            } else {
                badge.setVisibility(View.GONE);
            }
        }
    }

    private void bindReservasMes(SuperadminMetricsSnapshot snapshot) {
        int reservasMes = snapshot.reservasEnRango(PeriodoUtils.inicioDeMes(0), PeriodoUtils.ahora());
        int reservasMesAnterior = snapshot.reservasEnRango(PeriodoUtils.inicioDeMes(-1), PeriodoUtils.inicioDeMes(0));
        setText(R.id.dashboardReservasMesValue, String.valueOf(reservasMes));

        TextView badge = findViewById(R.id.dashboardReservasMesBadge);
        if (badge != null) {
            int delta = reservasMes - reservasMesAnterior;
            if (delta != 0) {
                badge.setText(getString(R.string.sa_dashboard_delta_mes_pasado,
                        (delta > 0 ? "+" + delta : String.valueOf(delta))));
                badge.setVisibility(View.VISIBLE);
            } else {
                badge.setVisibility(View.GONE);
            }
        }
    }

    private String formatVariacion(double variacionPorcentual) {
        String signo = variacionPorcentual >= 0 ? "+" : "";
        return String.format(Locale.getDefault(), "%s%.0f%%", signo, variacionPorcentual);
    }

    private void setText(int viewId, String value) {
        TextView view = findViewById(viewId);
        if (view != null) view.setText(value);
    }

    private void actualizarBadgeNotificaciones() {
        TextView badge = findViewById(R.id.badgeNotifCount);
        if (badge == null) return;

        SuperadminNotificationsActivity.loadActiveNotificationIds(this, ids -> {
            if (isFinishing() || isDestroyed()) return;

            java.util.Set<String> descartadas = PreferencesManager.obtenerNotificacionesDescartadasSA(this);
            java.util.Set<String> leidas = PreferencesManager.obtenerNotificacionesLeidasSA(this);
            int pendientes = 0;
            for (String id : ids) {
                if (!descartadas.contains(id) && !leidas.contains(id)) pendientes++;
            }

            if (pendientes > 0) {
                badge.setVisibility(View.VISIBLE);
                badge.setText(String.valueOf(pendientes));
            } else {
                badge.setVisibility(View.GONE);
            }
        });
    }

    private void bindInsets() {
        View root = findViewById(R.id.main);
        if (root == null) {
            return;
        }
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }

    private void setupClicks() {
        View quickUsers = findViewById(R.id.quickUsersItem);
        View quickApprovals = findViewById(R.id.quickApprovalsItem);
        View quickLogs = findViewById(R.id.quickLogsItem);
        View quickReports = findViewById(R.id.quickReportsItem);
        View profileBadge = findViewById(R.id.saProfileBadge);
        View navUsers = findViewById(R.id.navUsers);
        View navApprovals = findViewById(R.id.navApprovals);
        View navReports = findViewById(R.id.navReports);
        View navLogs = findViewById(R.id.navLogs);

        if (quickUsers != null) {
            quickUsers.setOnClickListener(v -> open(SuperadminUsersActivity.class));
        }
        if (quickApprovals != null) {
            quickApprovals.setOnClickListener(v -> open(SuperadminApprovalsActivity.class));
        }
        if (quickLogs != null) {
            quickLogs.setOnClickListener(v -> open(SuperadminLogsActivity.class));
        }
        if (quickReports != null) {
            quickReports.setOnClickListener(v -> open(SuperadminReportsActivity.class));
        }
        if (profileBadge != null) {
            profileBadge.setOnClickListener(this::showProfileMenu);
        }
        View btnNotifications = findViewById(R.id.btnNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> open(SuperadminNotificationsActivity.class));
        }
        if (navUsers != null) {
            navUsers.setOnClickListener(v -> open(SuperadminUsersActivity.class));
        }
        if (navApprovals != null) {
            navApprovals.setOnClickListener(v -> open(SuperadminApprovalsActivity.class));
        }
        if (navReports != null) {
            navReports.setOnClickListener(v -> open(SuperadminReportsActivity.class));
        }
        if (navLogs != null) {
            navLogs.setOnClickListener(v -> open(SuperadminLogsActivity.class));
        }
    }

    private void open(Class<?> destination) {
        startActivity(new Intent(this, destination));
    }

    private void showProfileMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.sa_profile_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(this::onProfileMenuItemClick);
        popupMenu.show();
    }

    private boolean onProfileMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.actionLogout) {
            logout();
            return true;
        }
        return false;
    }

    private void logout() {
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
