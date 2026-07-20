package com.example.bitbusters.activities.access;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.bitbusters.R;
import com.example.bitbusters.utils.ImmersiveMode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

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
    public static final String EXTRA_FIRST_NAMES = "firstNames";
    public static final String EXTRA_LAST_NAMES  = "lastNames";
    public static final String EXTRA_EMAIL      = "email";
    public static final String EXTRA_PHONE      = "phone";
    public static final String EXTRA_DNI        = "dni";
    public static final String EXTRA_DOC_TYPE   = "docType";
    public static final String EXTRA_ADDRESS    = "address";
    public static final String EXTRA_BIRTH_DATE = "birthDate";
    public static final String EXTRA_PASSWORD   = "password";
    public static final String EXTRA_PHOTO_URI  = "photoUri";

    private EditText fullNameInput, lastNameInput, emailInput, phoneInput, dniInput, addressInput,
            birthDateInput, passwordInput, repeatPasswordInput;
    private ChipGroup docTypeChipGroup;
    private ImageView imgSelectedProfile;
    private TextView tvPhotoStatus;
    private View rootLayout;
    private Uri selectedPhotoUri;

    private final ActivityResultLauncher<String> pickProfilePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                selectedPhotoUri = uri;
                if (imgSelectedProfile != null) {
                    Glide.with(this).load(uri).centerCrop().into(imgSelectedProfile);
                }
                if (tvPhotoStatus != null) {
                    tvPhotoStatus.setText("Foto seleccionada");
                }
            });

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
        lastNameInput       = findViewById(R.id.lastNameInput);
        emailInput          = findViewById(R.id.emailInput);
        phoneInput          = findViewById(R.id.phoneInput);
        dniInput            = findViewById(R.id.dniInput);
        addressInput        = findViewById(R.id.addressInput);
        birthDateInput      = findViewById(R.id.birthDateInput);
        passwordInput       = findViewById(R.id.passwordInput);
        repeatPasswordInput = findViewById(R.id.repeatPasswordInput);
        docTypeChipGroup    = findViewById(R.id.docTypeChipGroup);
        imgSelectedProfile  = findViewById(R.id.imgSelectedProfile);
        tvPhotoStatus       = findViewById(R.id.tvPhotoStatus);
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
        View photoPickerContainer = findViewById(R.id.photoPickerContainer);
        if (photoPickerContainer != null) {
            photoPickerContainer.setOnClickListener(v -> pickProfilePhotoLauncher.launch("image/*"));
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

        String nombres = fullNameInput.getText().toString().trim();
        String apellidos = lastNameInput.getText().toString().trim();
        String nombreCompleto = (nombres + " " + apellidos).trim();

        Intent intent = new Intent(this, RegisterOtpActivity.class);
        intent.putExtra(EXTRA_FULL_NAME,  nombreCompleto);
        intent.putExtra(EXTRA_FIRST_NAMES, nombres);
        intent.putExtra(EXTRA_LAST_NAMES, apellidos);
        intent.putExtra(EXTRA_EMAIL,      emailInput.getText().toString().trim());
        intent.putExtra(EXTRA_PHONE,      phoneInput.getText().toString().trim());
        intent.putExtra(EXTRA_DNI,        dniInput.getText().toString().trim());
        intent.putExtra(EXTRA_DOC_TYPE,   obtenerTipoDocumentoSeleccionado());
        intent.putExtra(EXTRA_ADDRESS,    addressInput.getText().toString().trim());
        intent.putExtra(EXTRA_BIRTH_DATE, birthDateInput.getText().toString().trim());
        intent.putExtra(EXTRA_PASSWORD,   passwordInput.getText().toString());
        if (selectedPhotoUri != null) {
            intent.putExtra(EXTRA_PHOTO_URI, selectedPhotoUri.toString());
        }
        startActivity(intent);
    }

    // ── Validaciones locales ─────────────────────────────────────────────────

    private boolean validateFields() {
        boolean isValid = true;

        // Nombres
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

        // Apellidos
        String apellidos = lastNameInput != null ? lastNameInput.getText().toString().trim() : "";
        if (apellidos.isEmpty()) {
            setInputError(lastNameInput, getString(R.string.validation_required));
            isValid = false;
        } else if (apellidos.length() < 3) {
            setInputError(lastNameInput, "Mínimo 3 caracteres");
            isValid = false;
        } else {
            clearInputError(lastNameInput);
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

        // Documento según tipo seleccionado
        String dni = dniInput != null ? dniInput.getText().toString().trim() : "";
        String tipoDoc = obtenerTipoDocumentoSeleccionado();
        if (dni.isEmpty()) {
            setInputError(dniInput, getString(R.string.validation_required));
            isValid = false;
        } else if ("DNI".equals(tipoDoc) && !dni.matches("\\d{8}")) {
            setInputError(dniInput, "El DNI debe tener exactamente 8 dígitos");
            isValid = false;
        } else if ("Pasaporte".equals(tipoDoc) && !dni.matches("[A-Za-z0-9]{6,12}")) {
            setInputError(dniInput, "El pasaporte debe tener entre 6 y 12 caracteres");
            isValid = false;
        } else if ("Carnet de extranjería".equals(tipoDoc) && !dni.matches("[A-Za-z0-9]{9,12}")) {
            setInputError(dniInput, "El carnet debe tener entre 9 y 12 caracteres");
            isValid = false;
        } else {
            clearInputError(dniInput);
        }

        // Domicilio
        String domicilio = addressInput != null ? addressInput.getText().toString().trim() : "";
        if (domicilio.isEmpty()) {
            setInputError(addressInput, getString(R.string.validation_required));
            isValid = false;
        } else if (domicilio.length() < 5) {
            setInputError(addressInput, "Ingresa un domicilio válido");
            isValid = false;
        } else {
            clearInputError(addressInput);
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

    private String obtenerTipoDocumentoSeleccionado() {
        if (docTypeChipGroup == null) return "DNI";
        int checkedId = docTypeChipGroup.getCheckedChipId();
        Chip chip = checkedId != View.NO_ID ? findViewById(checkedId) : null;
        if (chip == null || chip.getText() == null) return "DNI";
        String value = chip.getText().toString();
        if ("Carnet extranjería".equalsIgnoreCase(value)) {
            return "Carnet de extranjería";
        }
        return value;
    }
}
