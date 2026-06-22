package com.example.bitbusters.activities.access;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;
import com.example.bitbusters.utils.ImmersiveMode;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Primer paso del registro de cliente: recoge los datos personales y, tras
 * validarlos, navega a {@link RegisterOtpActivity} para verificar el correo
 * con un código. La cuenta NO se crea aquí: se crea al verificar el OTP.
 */
public class RegisterAccountActivity extends AppCompatActivity {

    // Claves de extras propagadas a RegisterOtpActivity (y de ahí a la creación de cuenta).
    public static final String EXTRA_FULL_NAME  = "fullName";
    public static final String EXTRA_EMAIL      = "email";
    public static final String EXTRA_PHONE      = "phone";
    public static final String EXTRA_DNI        = "dni";
    public static final String EXTRA_BIRTH_DATE = "birthDate";
    public static final String EXTRA_PASSWORD   = "password";

    private EditText fullNameInput, emailInput, phoneInput, dniInput,
            birthDateInput, passwordInput, repeatPasswordInput;
    private View rootLayout;

    /** Fecha elegida en el calendario; arranca en la fecha actual. */
    private final Calendar fechaSeleccionada = Calendar.getInstance();
    private final SimpleDateFormat formatoFecha =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_register_account);

        // Campos del formulario
        fullNameInput       = findViewById(R.id.fullNameInput);
        emailInput          = findViewById(R.id.emailInput);
        phoneInput          = findViewById(R.id.phoneInput);
        dniInput            = findViewById(R.id.dniInput);
        birthDateInput      = findViewById(R.id.birthDateInput);
        passwordInput       = findViewById(R.id.passwordInput);
        repeatPasswordInput = findViewById(R.id.repeatPasswordInput);
        rootLayout          = findViewById(R.id.main);

        MaterialButton backButton     = findViewById(R.id.backButton);
        MaterialButton registerButton = findViewById(R.id.registerButton);
        TextView loginLink            = findViewById(R.id.loginLink);

        if (backButton != null) backButton.setOnClickListener(v -> finish());
        if (loginLink != null)  loginLink.setOnClickListener(v -> finish());

        // La fecha de nacimiento se elige con un calendario (no se escribe a mano).
        if (birthDateInput != null) {
            birthDateInput.setOnClickListener(v -> mostrarCalendarioFechaNacimiento());
        }

        // Botón Registrarme → valida y pasa a la verificación por código (OTP).
        if (registerButton != null) {
            registerButton.setOnClickListener(v -> irAVerificacionOtp());
        }

        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    // ── Calendario de fecha de nacimiento ────────────────────────────────────

    private void mostrarCalendarioFechaNacimiento() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    fechaSeleccionada.set(year, month, day);
                    birthDateInput.setText(formatoFecha.format(fechaSeleccionada.getTime()));
                    clearInputError(birthDateInput);
                },
                fechaSeleccionada.get(Calendar.YEAR),
                fechaSeleccionada.get(Calendar.MONTH),
                fechaSeleccionada.get(Calendar.DAY_OF_MONTH));
        // No tiene sentido nacer en el futuro.
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    // ── Navegación al paso de verificación (OTP) ─────────────────────────────

    private void irAVerificacionOtp() {
        if (!validateFields()) return;

        Intent intent = new Intent(this, RegisterOtpActivity.class);
        intent.putExtra(EXTRA_FULL_NAME,  fullNameInput.getText().toString().trim());
        intent.putExtra(EXTRA_EMAIL,      emailInput.getText().toString().trim());
        intent.putExtra(EXTRA_PHONE,      phoneInput.getText().toString().trim());
        intent.putExtra(EXTRA_DNI,        dniInput.getText().toString().trim());
        intent.putExtra(EXTRA_BIRTH_DATE, birthDateInput.getText().toString().trim());
        intent.putExtra(EXTRA_PASSWORD,   passwordInput.getText().toString());
        startActivity(intent);
    }

    // ── Validaciones locales ─────────────────────────────────────────────────

    private boolean validateFields() {
        boolean isValid = true;

        // Nombre (mínimo 3 caracteres)
        String nombre = fullNameInput != null ? fullNameInput.getText().toString().trim() : "";
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
        String email = emailInput != null ? emailInput.getText().toString().trim() : "";
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
        String telefono = phoneInput != null ? phoneInput.getText().toString().trim() : "";
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
        String dni = dniInput != null ? dniInput.getText().toString().trim() : "";
        if (dni.isEmpty()) {
            setInputError(dniInput, getString(R.string.validation_required));
            isValid = false;
        } else if (!dni.matches("\\d{8}")) {
            setInputError(dniInput, "El DNI debe tener exactamente 8 dígitos");
            isValid = false;
        } else {
            clearInputError(dniInput);
        }

        // Fecha de nacimiento (obligatoria; se elige desde el calendario)
        String fecha = birthDateInput != null ? birthDateInput.getText().toString().trim() : "";
        if (fecha.isEmpty()) {
            setInputError(birthDateInput, getString(R.string.validation_required));
            isValid = false;
        } else {
            clearInputError(birthDateInput);
        }

        // Contraseña (mínimo 6 caracteres)
        String password = passwordInput != null ? passwordInput.getText().toString() : "";
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
            setInputError(repeatPasswordInput, getString(R.string.validation_password_mismatch));
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
}
