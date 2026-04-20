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
import com.example.bitbusters.utils.ImmersiveMode;
import com.google.android.material.button.MaterialButton;

public class ResetPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_reset_password);

        MaterialButton backButton = findViewById(R.id.backButton);
        MaterialButton updatePasswordButton = findViewById(R.id.updatePasswordButton);
        EditText newPasswordInput = findViewById(R.id.newPasswordInput);
        EditText repeatPasswordInput = findViewById(R.id.repeatPasswordInput);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        if (updatePasswordButton != null) {
            updatePasswordButton.setOnClickListener(v -> updatePassword(newPasswordInput, repeatPasswordInput));
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void updatePassword(EditText newPasswordInput, EditText repeatPasswordInput) {
        String newPassword = newPasswordInput != null && newPasswordInput.getText() != null
                ? newPasswordInput.getText().toString().trim()
                : "";
        String repeatPassword = repeatPasswordInput != null && repeatPasswordInput.getText() != null
                ? repeatPasswordInput.getText().toString().trim()
                : "";

        if (newPassword.length() < 6) {
            if (newPasswordInput != null) {
                newPasswordInput.setError(getString(R.string.reset_password_short));
            }
            return;
        }

        if (!newPassword.equals(repeatPassword)) {
            if (repeatPasswordInput != null) {
                repeatPasswordInput.setError(getString(R.string.reset_password_mismatch));
            }
            return;
        }

        Toast.makeText(this, R.string.reset_password_success, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAffinity();
    }
}
