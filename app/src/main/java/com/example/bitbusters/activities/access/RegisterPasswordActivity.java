package com.example.bitbusters.activities.access;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bitbusters.utils.AuthHelper;
import com.example.bitbusters.utils.ImmersiveMode;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterPasswordActivity extends AppCompatActivity {

    private EditText passwordInput, repeatPasswordInput;
    private CharSequence registerButtonTextoOriginal;
    /** Correo y nombre recolectados en RegisterAccountActivity y propagados vía RegisterOtpActivity. */
    private String email, fullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_register_password);

        passwordInput = findViewById(R.id.passwordInput);
        repeatPasswordInput = findViewById(R.id.repeatPasswordInput);

        email = getIntent().getStringExtra(RegisterAccountActivity.EXTRA_EMAIL);
        fullName = getIntent().getStringExtra(RegisterAccountActivity.EXTRA_FULL_NAME);

        MaterialButton backButton = findViewById(R.id.backButton);
        MaterialButton registerButton = findViewById(R.id.registerButton);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        if (registerButton != null) {
            registerButtonTextoOriginal = registerButton.getText();
            registerButton.setOnClickListener(v -> {
                if (validatePasswords()) {
                    crearCuentaEnFirebase(registerButton, passwordInput.getText().toString());
                }
            });
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean validatePasswords() {
        boolean isValid = true;
        String password = passwordInput.getText().toString();
        String repeatPassword = repeatPasswordInput.getText().toString();

        if (password.isEmpty()) {
            passwordInput.setError(getString(R.string.validation_required));
            isValid = false;
        } else if (password.length() < 6) {
            passwordInput.setError(getString(R.string.validation_password_short));
            isValid = false;
        }

        if (repeatPassword.isEmpty()) {
            repeatPasswordInput.setError(getString(R.string.validation_required));
            isValid = false;
        } else if (!password.equals(repeatPassword)) {
            repeatPasswordInput.setError(getString(R.string.validation_password_mismatch));
            isValid = false;
        }

        return isValid;
    }

    /**
     * Crea la cuenta del Asesor en Firebase Authentication
     * (FirebaseAuth.createUserWithEmailAndPassword) con el correo recolectado
     * en RegisterAccountActivity, fija el displayName con el nombre completo y,
     * si todo sale bien, regresa a LoginActivity para que inicie sesión (Clase 10 — BaaS).
     */
    private void crearCuentaEnFirebase(MaterialButton registerButton, String password) {
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, R.string.generic_navigation_error, Toast.LENGTH_SHORT).show();
            return;
        }

        registerButton.setEnabled(false);
        registerButton.setText(getString(R.string.register_creating_account));

        AuthHelper.registrarUsuario(email, password, new AuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser usuario) {
                if (usuario != null && fullName != null && !fullName.isEmpty()) {
                    usuario.updateProfile(new UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build());
                }
                Toast.makeText(RegisterPasswordActivity.this, R.string.register_success, Toast.LENGTH_LONG).show();
                irALogin();
            }

            @Override
            public void onError(String mensaje) {
                restaurarBotonRegistro(registerButton);
                Toast.makeText(RegisterPasswordActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void restaurarBotonRegistro(MaterialButton registerButton) {
        registerButton.setEnabled(true);
        registerButton.setText(registerButtonTextoOriginal != null ? registerButtonTextoOriginal : getString(R.string.register_submit));
    }

    private void irALogin() {
        try {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, R.string.generic_navigation_error, Toast.LENGTH_SHORT).show();
        }
    }
}
