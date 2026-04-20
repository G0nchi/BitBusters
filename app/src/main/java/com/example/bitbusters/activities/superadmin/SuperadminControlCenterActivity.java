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

import com.example.bitbusters.R;
import com.example.bitbusters.activities.access.LoginActivity;

public class SuperadminControlCenterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_superadmin_control_center);

        bindInsets();
        setupClicks();
    }

    private void bindInsets() {
        View root = findViewById(R.id.main);
        if (root == null) {
            return;
        }
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
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
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
