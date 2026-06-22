package com.example.bitbusters.activities.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;
import com.google.android.material.textfield.TextInputEditText;

public class AdminRegistrarAsesorActivity extends AppCompatActivity {

    private TextInputEditText etNombreAsesor;
    private TextInputEditText etCorreoAsesor;
    private TextInputEditText etTelefonoAsesor;
    private TextInputEditText etDniAsesor;
    private TextView tvInitials;
    private TextView tvPreviewNombre;
    private TextView tvPreviewCorreo;
    private TextView tvPreviewTelefono;
    private TextView tvPreviewDni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_registrar_asesor);
        initializeViews();
        setupNavigationListeners();
        setupPreviewUpdates();
    }

    private void initializeViews() {
        etNombreAsesor = findViewById(R.id.etNombreAsesor);
        etCorreoAsesor = findViewById(R.id.etCorreoAsesor);
        etTelefonoAsesor = findViewById(R.id.etTelefonoAsesor);
        etDniAsesor = findViewById(R.id.etDniAsesor);
        tvInitials = findViewById(R.id.tvInitials);
        tvPreviewNombre = findViewById(R.id.tvPreviewNombre);
        tvPreviewCorreo = findViewById(R.id.tvPreviewCorreo);
        tvPreviewTelefono = findViewById(R.id.tvPreviewTelefono);
        tvPreviewDni = findViewById(R.id.tvPreviewDni);
    }

    private void setupPreviewUpdates() {
        // Update preview when name changes
        if (etNombreAsesor != null) {
            etNombreAsesor.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updatePreview();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Update preview when email changes
        if (etCorreoAsesor != null) {
            etCorreoAsesor.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updatePreview();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (etTelefonoAsesor != null) {
            etTelefonoAsesor.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updatePreview();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (etDniAsesor != null) {
            etDniAsesor.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updatePreview();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void updatePreview() {
        String nombre = getText(etNombreAsesor);
        String correo = getText(etCorreoAsesor);
        String telefono = getText(etTelefonoAsesor);
        String dni = getText(etDniAsesor);

        // Update name in preview
        if (tvPreviewNombre != null) {
            tvPreviewNombre.setText(!nombre.isEmpty() ? nombre : "Nombre");
        }

        // Update email in preview
        if (tvPreviewCorreo != null) {
            tvPreviewCorreo.setText(!correo.isEmpty() ? correo : "correo@email.com");
        }
        if (tvPreviewTelefono != null) {
            tvPreviewTelefono.setText(!telefono.isEmpty() ? telefono : "Teléfono");
        }
        if (tvPreviewDni != null) {
            tvPreviewDni.setText(!dni.isEmpty() ? dni : "DNI");
        }

        // Update initials
        if (tvInitials != null) {
            String initials = getInitials(nombre);
            tvInitials.setText(initials);
        }
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return "--";
        }

        String[] parts = fullName.split(" ");
        StringBuilder initials = new StringBuilder();

        if (parts.length >= 2) {
            // First letter of first name + first letter of last name
            initials.append(parts[0].charAt(0));
            initials.append(parts[parts.length - 1].charAt(0));
        } else if (parts.length == 1 && !parts[0].isEmpty()) {
            // Only first letter if only one name
            initials.append(parts[0].charAt(0));
        }

        return initials.toString().toUpperCase();
    }

    private void setupNavigationListeners() {
        // Back button in header
        ImageButton btnBackRegister = findViewById(R.id.btnBackRegister);
        if (btnBackRegister != null) {
            btnBackRegister.setOnClickListener(v -> finish());
        }

        // Register button (save advisor)
        Button btnRegisterAdvisor = findViewById(R.id.btnRegisterAdvisor);
        if (btnRegisterAdvisor != null) {
            btnRegisterAdvisor.setOnClickListener(v -> {
                if (isFormValid()) {
                    Toast.makeText(this, "Asesor registrado en sesión", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }

        // Cancel button
        Button btnCancelRegister = findViewById(R.id.btnCancelRegister);
        if (btnCancelRegister != null) {
            btnCancelRegister.setOnClickListener(v -> finish());
        }
    }

    private boolean isFormValid() {
        String nombre = getText(etNombreAsesor);
        String correo = getText(etCorreoAsesor);
        String telefono = getText(etTelefonoAsesor);
        String dni = getText(etDniAsesor);

        if (nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty() || dni.isEmpty()) {
            Toast.makeText(this, "Completa Nombre, Correo, Teléfono y DNI", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String getText(TextInputEditText input) {
        return input != null && input.getText() != null ? input.getText().toString().trim() : "";
    }
}
