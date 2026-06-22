package com.example.bitbusters.activities.access;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;
import com.example.bitbusters.activities.admin.AdminMainActivity;
import com.example.bitbusters.activities.asesor.AsesorHomeActivity;
import com.example.bitbusters.activities.cliente.HomeActivity;
import com.example.bitbusters.activities.superadmin.SuperadminControlCenterActivity;
import com.example.bitbusters.utils.AdminPreferencesManager;
import com.example.bitbusters.utils.AuthHelper;
import com.example.bitbusters.utils.PreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;
import com.example.bitbusters.utils.ImmersiveMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private static final String SUPERADMIN_USER = "superadmin";
    private static final String SUPERADMIN_PASSWORD = "superadmin";
    private static final String CLIENT_USER = "cliente";
    private static final String CLIENT_PASSWORD = "cliente";
    private static final String ADMIN_USER = "administrador";
    private static final String ADMIN_PASSWORD = "administrador";
    private FirebaseAuth mAuth;
    private boolean passwordVisible = false;

    /** Texto original del botón de login, para restaurarlo tras un intento de Firebase Auth. */
    private CharSequence loginButtonTextoOriginal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateByRole(currentUser.getUid());
            return;
        }

        setContentView(R.layout.activity_login);

        MaterialButton backButton = findViewById(R.id.backButton);
        MaterialButton loginButton = findViewById(R.id.loginButton);
        EditText emailInput = findViewById(R.id.emailInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        TextView registerLink = findViewById(R.id.registerLink);
        ImageView togglePassword = findViewById(R.id.togglePassword);

        if (backButton != null) backButton.setOnClickListener(v -> finish());
        if (forgotPassword != null) forgotPassword.setOnClickListener(v -> openIfAvailable(ForgotPasswordActivity.class));
        if (registerLink != null) registerLink.setOnClickListener(v -> openIfAvailable(RegisterAccountActivity.class));

        if (togglePassword != null && passwordInput != null) {
            togglePassword.setOnClickListener(v -> {
                passwordVisible = !passwordVisible;
                if (passwordVisible) {
                    passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    togglePassword.setImageResource(R.drawable.ic_visibility_off_20);
                    togglePassword.setContentDescription(getString(R.string.login_hide_password));
                } else {
                    passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    togglePassword.setImageResource(R.drawable.ic_visibility_20);
                    togglePassword.setContentDescription(getString(R.string.login_show_password));
                }
                passwordInput.setSelection(passwordInput.getText() != null ? passwordInput.getText().length() : 0);
            });
        }

        if (loginButton != null) {
            loginButtonTextoOriginal = loginButton.getText();
            loginButton.setOnClickListener(v -> validateLoginByRole(loginButton, emailInput, passwordInput));
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void signIn(EditText emailInput, EditText passwordInput) {
        String email = emailInput != null && emailInput.getText() != null
                ? emailInput.getText().toString().trim() : "";
        String password = passwordInput != null && passwordInput.getText() != null
                ? passwordInput.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.login_error_invalid_credentials, Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        navigateByRole(mAuth.getCurrentUser().getUid());
                    } else {
                        if (emailInput != null) emailInput.setError(getString(R.string.login_error_invalid_credentials));
                        Toast.makeText(this, R.string.login_error_invalid_credentials, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateByRole(String uid) {
        Log.d("LoginActivity", "Login exitoso, UID: " + uid);
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Log.e("LoginActivity", "Documento no existe en 'users' para UID: " + uid);
                        Toast.makeText(this, "Tu perfil no está configurado. Contacta al administrador.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        return;
                    }
                    String role   = doc.getString("role");
                    String nombre = doc.getString("nombre");
                    String status = doc.getString("status");
                    String fechaHora = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

                    if ("inactive".equals(status)) {
                        mAuth.signOut();
                        Toast.makeText(this, "Tu cuenta está desactivada. Contacta al soporte.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if ("pending".equals(status)) {
                        mAuth.signOut();
                        Toast.makeText(this, "Tu cuenta está pendiente de aprobación.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Intent intent;
                    switch (role != null ? role : "cliente") {
                        case "superadmin":
                            PreferencesManager.guardarNombreSuperadmin(this, nombre != null ? nombre : "Superadmin");
                            PreferencesManager.guardarUltimoAccesoSuperadmin(this, fechaHora);
                            intent = new Intent(this, SuperadminControlCenterActivity.class);
                            break;
                        case "admin":
                            AdminPreferencesManager.guardarNombre(this, nombre != null ? nombre : "Admin");
                            AdminPreferencesManager.guardarUltimoAcceso(this, fechaHora);
                            intent = new Intent(this, AdminMainActivity.class);
                            break;
                        case "asesor":
                            intent = new Intent(this, AsesorHomeActivity.class);
                            break;
                        default:
                            PreferencesManager.guardarNombre(this, nombre != null ? nombre : "Cliente");
                            PreferencesManager.guardarUltimoAcceso(this, fechaHora);
                            intent = new Intent(this, HomeActivity.class);
                            break;
                    }
                    // Guardar FCM token en Firestore para notificaciones push
                    FirebaseMessaging.getInstance().getToken()
                            .addOnSuccessListener(token ->
                                    FirebaseFirestore.getInstance()
                                            .collection("users").document(uid)
                                            .update("fcmToken", token));

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("LoginActivity", "Error al leer 'users': " + e.getMessage());
                    Toast.makeText(this, "Error al cargar tu perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                });
    }

    private void routeByRole(String role, String nombre, String uid) {
        String fechaHora = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        Intent intent;
        String rolNorm = role.toLowerCase().trim();
        Log.d("LoginActivity", "Redirigiendo rol normalizado: '" + rolNorm + "'");
        switch (rolNorm) {
            case "superadmin":
                PreferencesManager.guardarNombreSuperadmin(this, nombre != null ? nombre : "Superadmin");
                PreferencesManager.guardarUltimoAccesoSuperadmin(this, fechaHora);
                intent = new Intent(this, SuperadminControlCenterActivity.class);
                Log.d("LoginActivity", "Redirigiendo a SuperAdmin Home");
                break;
            case "admin":
                AdminPreferencesManager.guardarNombre(this, nombre != null ? nombre : "Admin");
                AdminPreferencesManager.guardarUltimoAcceso(this, fechaHora);
                intent = new Intent(this, AdminMainActivity.class);
                Log.d("LoginActivity", "Redirigiendo a Admin Home");
                break;
            case "asesor":
                intent = new Intent(this, AsesorHomeActivity.class);
                Log.d("LoginActivity", "Redirigiendo a Asesor Home");
                break;
            case "cliente":
                PreferencesManager.guardarNombre(this, nombre != null ? nombre : "Cliente");
                PreferencesManager.guardarUltimoAcceso(this, fechaHora);
                intent = new Intent(this, HomeActivity.class);
                Log.d("LoginActivity", "Redirigiendo a Cliente Home");
                break;
            default:
                Log.e("LoginActivity", "Rol no reconocido: '" + rolNorm + "'");
                Toast.makeText(this, "Rol no reconocido: " + role, Toast.LENGTH_LONG).show();
                mAuth.signOut();
                return;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openIfAvailable(Class<?> destination) {
        try {
            startActivity(new Intent(this, destination));
        } catch (Exception e) {
            Toast.makeText(this, R.string.generic_navigation_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void validateLoginByRole(MaterialButton loginButton, EditText emailInput, EditText passwordInput) {
        String user = emailInput != null && emailInput.getText() != null
                ? emailInput.getText().toString().trim()
                : "";
        String password = passwordInput != null && passwordInput.getText() != null
                ? passwordInput.getText().toString().trim()
                : "";

        // ── Roles con credenciales mock locales (sin backend real todavía) ──
        if (SUPERADMIN_USER.equals(user) && SUPERADMIN_PASSWORD.equals(password)) {
            PreferencesManager.guardarNombreSuperadmin(this, "Superadmin");
            String fechaHoraSA = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(new Date());
            PreferencesManager.guardarUltimoAccesoSuperadmin(this, fechaHoraSA);
            startActivity(new Intent(this, SuperadminControlCenterActivity.class));
            finish();
            return;
        }

        if (CLIENT_USER.equals(user) && CLIENT_PASSWORD.equals(password)) {
            // Guardar nombre y fecha/hora del login en SharedPreferences
            PreferencesManager.guardarNombre(this, "Jonathan");
            String fechaHora = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(new Date());
            PreferencesManager.guardarUltimoAcceso(this, fechaHora);
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        if (ADMIN_USER.equals(user) && ADMIN_PASSWORD.equals(password)) {
            // Guardar datos del admin en SharedPreferences separadas (Lab 5)
            AdminPreferencesManager.guardarNombre(this, "Juan García");
            AdminPreferencesManager.guardarInmobiliaria(this, "Inmobiliaria BitBuilders");
            String fechaHoraAdmin = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(new Date());
            AdminPreferencesManager.guardarUltimoAcceso(this, fechaHoraAdmin);
            startActivity(new Intent(this, AdminMainActivity.class));
            finish();
            return;
        }

        // ── Rol Asesor: autenticación real con Firebase Authentication (Clase 10 — BaaS) ──
        // Si el correo tiene formato válido, se intenta contra Firebase; cualquier
        // otra entrada (que no calzó con los roles mock de arriba) se rechaza de
        // inmediato sin gastar una llamada de red.
        if (!user.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(user).matches() && !password.isEmpty()) {
            iniciarSesionAsesorConFirebase(loginButton, emailInput, passwordInput, user, password);
            return;
        }

        mostrarErrorCredenciales(emailInput, passwordInput);
    }

    /**
     * Intenta autenticar al Asesor contra Firebase Authentication
     * (FirebaseAuth.signInWithEmailAndPassword) usando {@link AuthHelper}.
     *
     * Requiere que el proyecto tenga `app/google-services.json` y el plugin de
     * Google Services activo (ver FIREBASE_SETUP.md); de lo contrario Firebase
     * devuelve un error de configuración que se muestra igualmente al usuario.
     */
    private void iniciarSesionAsesorConFirebase(MaterialButton loginButton, EditText emailInput,
                                                 EditText passwordInput, String email, String password) {
        if (loginButton != null) {
            loginButton.setEnabled(false);
            loginButton.setText(getString(R.string.login_validating));
        }

        AuthHelper.iniciarSesion(email, password, new AuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser usuario) {
                restaurarBotonLogin(loginButton);
                navigateByRole(usuario.getUid());
            }

            @Override
            public void onError(String mensaje) {
                restaurarBotonLogin(loginButton);
                Toast.makeText(LoginActivity.this, mensaje, Toast.LENGTH_LONG).show();
                mostrarErrorCredenciales(emailInput, passwordInput);
            }
        });
    }

    private void restaurarBotonLogin(MaterialButton loginButton) {
        if (loginButton != null) {
            loginButton.setEnabled(true);
            loginButton.setText(loginButtonTextoOriginal != null ? loginButtonTextoOriginal : getString(R.string.login_button));
        }
    }

    private void mostrarErrorCredenciales(EditText emailInput, EditText passwordInput) {
        if (emailInput != null) {
            emailInput.setError(getString(R.string.login_error_invalid_credentials));
        }
        if (passwordInput != null) {
            passwordInput.setError(getString(R.string.login_error_invalid_credentials));
        }
        Toast.makeText(this, R.string.login_error_invalid_credentials, Toast.LENGTH_SHORT).show();
    }
}
