package com.example.bitbusters.activities.access;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;
import com.example.bitbusters.activities.cliente.HomeActivity;
import com.example.bitbusters.models.Usuario;
import com.example.bitbusters.repository.AuthRepository;
import com.example.bitbusters.utils.ImmersiveMode;
import com.example.bitbusters.utils.PreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Pantalla de registro de cliente.
 * Recoge: nombre, email, teléfono, DNI, contraseña y confirmación de contraseña.
 * Valida todos los campos localmente, verifica unicidad de DNI en Firestore,
 * crea la cuenta en Firebase Auth y guarda el perfil en Firestore.
 */
public class RegisterAccountActivity extends AppCompatActivity {

    private EditText fullNameInput;
    private EditText emailInput;
    private EditText phoneInput;
    private EditText dniInput;
    private EditText passwordInput;
    private EditText repeatPasswordInput;
    private MaterialButton registerButton;
    private ProgressBar progressBar;
    private View rootLayout;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_register_account);

        authRepository = new AuthRepository();

        // Campos del formulario
        fullNameInput      = findViewById(R.id.fullNameInput);
        emailInput         = findViewById(R.id.emailInput);
        phoneInput         = findViewById(R.id.phoneInput);
        dniInput           = findViewById(R.id.dniInput);
        passwordInput      = findViewById(R.id.passwordInput);
        repeatPasswordInput = findViewById(R.id.repeatPasswordInput);

        // Controles de UI
        registerButton = findViewById(R.id.registerButton);
        progressBar    = findViewById(R.id.progressBar);
        rootLayout     = findViewById(R.id.main);

        // Botón atrás
        MaterialButton backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Link "¿Ya tienes cuenta? Inicia sesión"
        TextView loginLink = findViewById(R.id.loginLink);
        if (loginLink != null) {
            loginLink.setOnClickListener(v -> finish());
        }

        // Botón Registrarme
        if (registerButton != null) {
            registerButton.setOnClickListener(v -> attemptRegister());
        }

        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    // ── Flujo de registro ────────────────────────────────────────────────────

    private void attemptRegister() {
        if (!validateFields()) return;

        String nombre   = fullNameInput.getText().toString().trim();
        String email    = emailInput.getText().toString().trim();
        String telefono = phoneInput.getText().toString().trim();
        String dni      = dniInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        setLoading(true);

        authRepository.registrarCliente(nombre, email, password, telefono, dni,
                new AuthRepository.AuthCallback() {
                    @Override
                    public void onSuccess(Usuario usuario) {
                        // Guardar nombre en PreferencesManager para uso local
                        String fechaHora = new SimpleDateFormat(
                                "dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
                        PreferencesManager.guardarNombre(
                                RegisterAccountActivity.this, usuario.getNombre());
                        PreferencesManager.guardarUltimoAcceso(
                                RegisterAccountActivity.this, fechaHora);

                        Toast.makeText(RegisterAccountActivity.this,
                                "¡Cuenta creada! Bienvenido, " + usuario.getNombre(),
                                Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(
                                RegisterAccountActivity.this, HomeActivity.class);
                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(String mensaje) {
                        setLoading(false);
                        if (rootLayout != null) {
                            Snackbar.make(rootLayout, mensaje, Snackbar.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RegisterAccountActivity.this,
                                    mensaje, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // ── Validaciones locales ─────────────────────────────────────────────────

    private boolean validateFields() {
        boolean isValid = true;

        // Nombre (mínimo 3 caracteres)
        String nombre = fullNameInput != null
                ? fullNameInput.getText().toString().trim() : "";
        if (nombre.isEmpty()) {
            setInputError(fullNameInput, getString(R.string.validation_required));
            isValid = false;
        } else if (nombre.length() < 3) {
            setInputError(fullNameInput, "Mínimo 3 caracteres");
            isValid = false;
        } else {
            clearInputError(fullNameInput);
        }

        // Email
        String email = emailInput != null
                ? emailInput.getText().toString().trim() : "";
        if (email.isEmpty()) {
            setInputError(emailInput, getString(R.string.validation_required));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setInputError(emailInput, getString(R.string.validation_invalid_email));
            isValid = false;
        } else {
            clearInputError(emailInput);
        }

        // Teléfono (9 dígitos, empieza con 9)
        String telefono = phoneInput != null
                ? phoneInput.getText().toString().trim() : "";
        if (telefono.isEmpty()) {
            setInputError(phoneInput, getString(R.string.validation_required));
            isValid = false;
        } else if (telefono.length() != 9 || !telefono.startsWith("9")) {
            setInputError(phoneInput, "Debe tener 9 dígitos y empezar con 9");
            isValid = false;
        } else {
            clearInputError(phoneInput);
        }

        // DNI (exactamente 8 dígitos numéricos)
        String dni = dniInput != null
                ? dniInput.getText().toString().trim() : "";
        if (dni.isEmpty()) {
            setInputError(dniInput, getString(R.string.validation_required));
            isValid = false;
        } else if (!dni.matches("\\d{8}")) {
            setInputError(dniInput, "El DNI debe tener exactamente 8 dígitos");
            isValid = false;
        } else {
            clearInputError(dniInput);
        }

        // Contraseña (mínimo 6 caracteres)
        String password = passwordInput != null
                ? passwordInput.getText().toString() : "";
        if (password.isEmpty()) {
            setInputError(passwordInput, getString(R.string.validation_required));
            isValid = false;
        } else if (password.length() < 6) {
            setInputError(passwordInput, getString(R.string.validation_password_short));
            isValid = false;
        } else {
            clearInputError(passwordInput);
        }

        // Confirmar contraseña
        String repeatPassword = repeatPasswordInput != null
                ? repeatPasswordInput.getText().toString() : "";
        if (repeatPassword.isEmpty()) {
            setInputError(repeatPasswordInput, getString(R.string.validation_required));
            isValid = false;
        } else if (!password.equals(repeatPassword)) {
            setInputError(repeatPasswordInput,
                    getString(R.string.validation_password_mismatch));
            isValid = false;
        } else {
            clearInputError(repeatPasswordInput);
        }

        return isValid;
    }

    private void setInputError(EditText input, String error) {
        if (input != null) input.setError(error);
    }

    private void clearInputError(EditText input) {
        if (input != null) input.setError(null);
    }

    // ── Estado de carga ──────────────────────────────────────────────────────

    private void setLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (registerButton != null) {
            registerButton.setEnabled(!loading);
        }
    }
}
