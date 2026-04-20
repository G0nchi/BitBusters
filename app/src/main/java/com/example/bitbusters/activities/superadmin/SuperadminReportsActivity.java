package com.example.bitbusters.activities.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.bitbusters.utils.ImmersiveMode;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;

public class SuperadminReportsActivity extends AppCompatActivity {

    private TextView reportsPeriodText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_superadmin_reports);

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
        reportsPeriodText = findViewById(R.id.reportsPeriodText);

        View reportsPeriodChip = findViewById(R.id.reportsPeriodChip);
        View navHome = findViewById(R.id.navHome);
        View navUsers = findViewById(R.id.navUsers);
        View navApprovals = findViewById(R.id.navApprovals);
        View navLogs = findViewById(R.id.navLogs);

        if (reportsPeriodChip != null) {
            reportsPeriodChip.setOnClickListener(this::showPeriodMenu);
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> openAndFinish(SuperadminControlCenterActivity.class));
        }
        if (navUsers != null) {
            navUsers.setOnClickListener(v -> openAndFinish(SuperadminUsersActivity.class));
        }
        if (navApprovals != null) {
            navApprovals.setOnClickListener(v -> openAndFinish(SuperadminApprovalsActivity.class));
        }
        if (navLogs != null) {
            navLogs.setOnClickListener(v -> openAndFinish(SuperadminLogsActivity.class));
        }
    }

    private void showPeriodMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, 0, 0, getString(R.string.sa_reports_period_month));
        menu.getMenu().add(0, 1, 1, getString(R.string.sa_reports_period_quarter));
        menu.getMenu().add(0, 2, 2, getString(R.string.sa_reports_period_year));

        menu.setOnMenuItemClickListener(item -> {
            if (reportsPeriodText != null) {
                reportsPeriodText.setText(item.getTitle());
            }
            return true;
        });
        menu.show();
    }

    private void open(Class<?> destination) {
        startActivity(new Intent(this, destination));
    }

    private void openAndFinish(Class<?> destination) {
        open(destination);
        finish();
    }
}
