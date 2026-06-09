package com.example.bitbusters.activities.access;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;
import com.example.bitbusters.utils.AuthHelper;
import com.example.bitbusters.utils.ImmersiveMode;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterPasswordActivity extends AppCompatActivity {

    private EditText passwordInput, repeatPasswordInput;
    private MaterialButton registerButton;
    private FirebaseAuth mAuth;
    private String email;
    private String fullName;
    private String registerButtonTextoOriginal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_register_password);

        mAuth = FirebaseAuth.getInstance();

        passwordInput       = findViewById(R.id.passwordInput);
        repeatPasswordInput = findViewById(R.id.repeatPasswordInput);
        registerButton      = findViewById(R.id.registerButton);

        email = getIntent().getStringExtra(RegisterAccountActivity.EXTRA_EMAIL);
        fullName = getIntent().getStringExtra(RegisterAccountActivity.EXTRA_FULL_NAME);

        MaterialButton backButton = findViewById(R.id.backButton);
        if (backButton != null) backButton.setOnClickListener(v -> finish());
        if (registerButton != null) registerButton.setOnClickListener(v -> attemptRegister());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });
    }

    private void attemptRegister() {
        if (!validatePasswords()) return;

        String email    = getIntent().getStringExtra("email");
        String password = passwordInput.getText().toString();

        if (email == null) {
            Toast.makeText(this, "Error: datos de registro incompletos", Toast.LENGTH_SHORT).show();
            return;
        }

        registerButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    saveUserToFirestore(uid, email);
                })
                .addOnFailureListener(e -> {
                    registerButton.setEnabled(true);
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("email address is already in use")) {
                        Toast.makeText(this, "Este correo ya está registrado", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error al crear cuenta: " + msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String uid, String email) {
        Bundle extras = getIntent().getExtras();

        Map<String, Object> user = new HashMap<>();
        user.put("nombre",           extras != null ? extras.getString("fullName", "") : "");
        user.put("email",            email);
        user.put("telefono",         extras != null ? extras.getString("phone", "") : "");
        user.put("direccion",        extras != null ? extras.getString("address", "") : "");
        user.put("tipoDoc",          extras != null ? extras.getString("docType", "") : "");
        user.put("numDoc",           extras != null ? extras.getString("docNumber", "") : "");
        user.put("fechaNacimiento",  extras != null ? extras.getString("birthDate", "") : "");
        user.put("role",   "cliente");
        user.put("status", "active");

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(unused -> {
                    mAuth.signOut();
                    Toast.makeText(this, "¡Cuenta creada! Inicia sesión.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Auth creada pero Firestore falló — eliminar la cuenta para no dejar estado inconsistente
                    if (mAuth.getCurrentUser() != null) {
                        mAuth.getCurrentUser().delete();
                    }
                    registerButton.setEnabled(true);
                    Toast.makeText(this, "Error guardando datos. Intenta de nuevo.", Toast.LENGTH_LONG).show();
                });
    }

    private boolean validatePasswords() {
        boolean isValid = true;
        String password       = passwordInput.getText().toString();
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
