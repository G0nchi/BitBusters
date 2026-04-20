package com.example.bitbusters.activities.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;

public class AdminDetallesSeparacionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_detalles_separacion);
        setupNavigationListeners();
    }

    private void setupNavigationListeners() {
        // Back button in header
        ImageButton backButton = findViewById(R.id.btnBackSeparation);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Back button (main)
        Button btnBack = findViewById(R.id.btnBackSeparationMain);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
}
