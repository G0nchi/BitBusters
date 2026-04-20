package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;

public class AdminEditarInmobiliariaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_editar_inmobiliaria);
        setupNavigationListeners();
    }

    private void setupNavigationListeners() {
        // Back button in header
        ImageButton btnBackEdit = findViewById(R.id.btnBackEdit);
        if (btnBackEdit != null) {
            btnBackEdit.setOnClickListener(v -> finish());
        }

        // Save changes button
        Button btnSaveChanges = findViewById(R.id.btnSaveChanges);
        if (btnSaveChanges != null) {
            btnSaveChanges.setOnClickListener(v -> finish());
        }

        // Cancel button
        Button btnCancelEdit = findViewById(R.id.btnCancelEdit);
        if (btnCancelEdit != null) {
            btnCancelEdit.setOnClickListener(v -> finish());
        }

        // Add photo button - open gallery
        Button btnAddPhotoEdit = findViewById(R.id.btnAddPhotoEdit);
        if (btnAddPhotoEdit != null) {
            btnAddPhotoEdit.setOnClickListener(v -> {
                Intent pickImageIntent = new Intent(Intent.ACTION_PICK);
                pickImageIntent.setType("image/*");
                startActivity(pickImageIntent);
            });
        }

        // Add advisor button
        Button btnAddAdvisorEdit = findViewById(R.id.btnAddAdvisorEdit);
        if (btnAddAdvisorEdit != null) {
            btnAddAdvisorEdit.setOnClickListener(v -> {
                startActivity(new Intent(AdminEditarInmobiliariaActivity.this, AdminRegistrarAsesorActivity.class));
            });
        }
    }
}
