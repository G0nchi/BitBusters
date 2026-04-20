package com.example.bitbusters.activities.access;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bitbusters.utils.ImmersiveMode;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;
import com.example.bitbusters.activities.admin.AdminMainActivity;
import com.example.bitbusters.activities.asesor.AsesorHomeActivity;
import com.example.bitbusters.activities.cliente.HomeActivity;
import com.example.bitbusters.activities.superadmin.SuperadminControlCenterActivity;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private static final String SUPERADMIN_USER = "superadmin";
    private static final String SUPERADMIN_PASSWORD = "superadmin";
    private static final String CLIENT_USER = "cliente";
    private static final String CLIENT_PASSWORD = "cliente";
    private static final String ADMIN_USER = "administrador";
    private static final String ADMIN_PASSWORD = "administrador";
    private static final String ASESOR_USER = "asesor";
    private static final String ASESOR_PASSWORD = "asesor";
    private boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_login);

        MaterialButton backButton = findViewById(R.id.backButton);
        MaterialButton loginButton = findViewById(R.id.loginButton);
        EditText emailInput = findViewById(R.id.emailInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        TextView registerLink = findViewById(R.id.registerLink);
        TextView showPassword = findViewById(R.id.showPassword);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        if (forgotPassword != null) {
            forgotPassword.setOnClickListener(v -> openIfAvailable(ForgotPasswordActivity.class));
        }
        if (registerLink != null) {
            registerLink.setOnClickListener(v -> openIfAvailable(RegisterAccountActivity.class));
        }

        if (showPassword != null && passwordInput != null) {
            showPassword.setOnClickListener(v -> {
                passwordVisible = !passwordVisible;
                if (passwordVisible) {
                    passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    showPassword.setText(R.string.login_hide_password);
                } else {
                    passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    showPassword.setText(R.string.login_show_password);
                }
                passwordInput.setSelection(passwordInput.getText() != null ? passwordInput.getText().length() : 0);
            });
        }

        if (loginButton != null) {
            loginButton.setOnClickListener(v -> validateLoginByRole(emailInput, passwordInput));
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void openIfAvailable(Class<?> destination) {
        try {
            Intent intent = new Intent(this, destination);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.generic_navigation_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void validateLoginByRole(EditText emailInput, EditText passwordInput) {
        String user = emailInput != null && emailInput.getText() != null
                ? emailInput.getText().toString().trim()
                : "";
        String password = passwordInput != null && passwordInput.getText() != null
                ? passwordInput.getText().toString().trim()
                : "";

        if (SUPERADMIN_USER.equals(user) && SUPERADMIN_PASSWORD.equals(password)) {
            startActivity(new Intent(this, SuperadminControlCenterActivity.class));
            finish();
            return;
        }

        if (CLIENT_USER.equals(user) && CLIENT_PASSWORD.equals(password)) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        if (ADMIN_USER.equals(user) && ADMIN_PASSWORD.equals(password)) {
            startActivity(new Intent(this, AdminMainActivity.class));
            finish();
            return;
        }

        if (ASESOR_USER.equals(user) && ASESOR_PASSWORD.equals(password)) {
            startActivity(new Intent(this, AsesorHomeActivity.class));
            finish();
            return;
        }

        if (emailInput != null) {
            emailInput.setError(getString(R.string.login_error_invalid_credentials));
        }
        if (passwordInput != null) {
            passwordInput.setError(getString(R.string.login_error_invalid_credentials));
        }
        Toast.makeText(this, R.string.login_error_invalid_credentials, Toast.LENGTH_SHORT).show();
    }
}
