package com.example.bitbusters.activities.access;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bitbusters.utils.ImmersiveMode;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;
import com.google.android.material.button.MaterialButton;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_forgot_password);

        MaterialButton backButton = findViewById(R.id.backButton);
        MaterialButton sendRecoveryButton = findViewById(R.id.sendRecoveryButton);
        EditText emailRecoveryInput = findViewById(R.id.emailRecoveryInput);

        backButton.setOnClickListener(v -> finish());
        if (sendRecoveryButton != null) {
            sendRecoveryButton.setOnClickListener(v -> startRecoveryFlow(emailRecoveryInput));
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void startRecoveryFlow(EditText emailRecoveryInput) {
        String email = emailRecoveryInput != null && emailRecoveryInput.getText() != null
                ? emailRecoveryInput.getText().toString().trim()
                : "";

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (emailRecoveryInput != null) {
                emailRecoveryInput.setError(getString(R.string.forgot_password_invalid_email));
            }
            return;
        }

        Toast.makeText(this, getString(R.string.forgot_password_code_sent, email), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, ForgotPasswordCodeActivity.class);
        intent.putExtra(ForgotPasswordCodeActivity.EXTRA_RECOVERY_EMAIL, email);
        startActivity(intent);
    }
}
