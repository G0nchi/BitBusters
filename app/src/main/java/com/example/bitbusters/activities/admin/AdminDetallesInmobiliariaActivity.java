package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;

public class AdminDetallesInmobiliariaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_detalles_inmobiliaria);
        setupNavigationListeners();
    }

    private void setupNavigationListeners() {
        // Back button in header
        ImageButton btnBackImmob = findViewById(R.id.btnBackImmob);
        if (btnBackImmob != null) {
            btnBackImmob.setOnClickListener(v -> finish());
        }

        // Edit button in header (pencil icon)
        ImageButton btnEditImmobHeader = findViewById(R.id.btnEditImmobHeader);
        if (btnEditImmobHeader != null) {
            btnEditImmobHeader.setOnClickListener(v -> {
                startActivity(new Intent(AdminDetallesInmobiliariaActivity.this, AdminEditarInmobiliariaActivity.class));
            });
        }
    }
}
