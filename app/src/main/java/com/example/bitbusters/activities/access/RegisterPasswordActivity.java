package com.example.bitbusters.activities.access;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bitbusters.utils.ImmersiveMode;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;
import com.google.android.material.button.MaterialButton;

public class RegisterPasswordActivity extends AppCompatActivity {

    private EditText passwordInput, repeatPasswordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_register_password);

        passwordInput = findViewById(R.id.passwordInput);
        repeatPasswordInput = findViewById(R.id.repeatPasswordInput);

        MaterialButton backButton = findViewById(R.id.backButton);
        MaterialButton registerButton = findViewById(R.id.registerButton);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        if (registerButton != null) {
            registerButton.setOnClickListener(v -> {
                if (validatePasswords()) {
                    try {
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(this, R.string.generic_navigation_error, Toast.LENGTH_SHORT).show();
                    }
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
}
