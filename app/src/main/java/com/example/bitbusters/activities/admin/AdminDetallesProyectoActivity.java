package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;

public class AdminDetallesProyectoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_detalles_proyecto);
        setupNavigationListeners();
    }

    private void setupNavigationListeners() {
        // Back button in header
        ImageButton backButton = findViewById(R.id.btnBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Edit project button
        Button btnEditProject = findViewById(R.id.btnEditProject);
        if (btnEditProject != null) {
            btnEditProject.setOnClickListener(v -> {
                startActivity(new Intent(AdminDetallesProyectoActivity.this, AdminEditarProyectoActivity.class));
            });
        }

        // View report details button
        Button btnViewReportDetails = findViewById(R.id.btnViewReportDetails);
        if (btnViewReportDetails != null) {
            btnViewReportDetails.setOnClickListener(v -> {
                startActivity(new Intent(AdminDetallesProyectoActivity.this, AdminDetallesDeReporteProyectoActivity.class));
            });
        }

        // Back button (secondary)
        Button btnBackProject = findViewById(R.id.btnBackProject);
        if (btnBackProject != null) {
            btnBackProject.setOnClickListener(v -> finish());
        }
    }
}
