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
import com.example.bitbusters.utils.NotificationHelper;
import com.example.bitbusters.utils.PreferencesManager;

public class SuperadminControlCenterActivity extends AppCompatActivity {

    // Evita que las notificaciones se disparen en cada onResume dentro de la misma sesión
    private static boolean notificacionesEnviadas = false;

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
    }

    private void actualizarBadgeNotificaciones() {
        TextView badge = findViewById(R.id.badgeNotifCount);
        if (badge == null) return;
        int total = 3; // IDs fijas: sa_aprobacion, sa_usuario, sa_log_critico
        int descartadas = PreferencesManager.obtenerNotificacionesDescartadasSA(this).size();
        int pendientes = total - descartadas;
        if (pendientes > 0) {
            badge.setVisibility(View.VISIBLE);
            badge.setText(String.valueOf(pendientes));
        } else {
            badge.setVisibility(View.GONE);
        }
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
