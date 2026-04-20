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
        ImageButton btnBackProject = findViewById(R.id.btnBackProject);
        if (btnBackProject != null) {
            btnBackProject.setOnClickListener(v -> finish());
        }

        // Edit project button
        ImageButton btnEditProject = findViewById(R.id.btnEditProject);
        if (btnEditProject != null) {
            btnEditProject.setOnClickListener(v -> {
                startActivity(new Intent(AdminDetallesProyectoActivity.this, AdminEditarProyectoActivity.class));
            });
        }
    }
}
