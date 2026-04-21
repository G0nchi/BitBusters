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

public class RegisterOtpActivity extends AppCompatActivity {

    private EditText otpDigit1, otpDigit2, otpDigit3, otpDigit4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        try {
            setContentView(R.layout.activity_register_otp);
        } catch (RuntimeException e) {
            Toast.makeText(this, R.string.generic_navigation_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        otpDigit1 = findViewById(R.id.otpDigit1);
        otpDigit2 = findViewById(R.id.otpDigit2);
        otpDigit3 = findViewById(R.id.otpDigit3);
        otpDigit4 = findViewById(R.id.otpDigit4);

        MaterialButton backButton = findViewById(R.id.backButton);
        MaterialButton verifyButton = findViewById(R.id.verifyButton);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        if (verifyButton != null) {
            verifyButton.setOnClickListener(v -> {
                if (validateOtp()) {
                    openIfAvailable(RegisterPasswordActivity.class);
                }
            });
        }

        if (findViewById(R.id.main) != null) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private boolean validateOtp() {
        if (otpDigit1.getText().toString().isEmpty() ||
            otpDigit2.getText().toString().isEmpty() ||
            otpDigit3.getText().toString().isEmpty() ||
            otpDigit4.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.validation_invalid_otp, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void openIfAvailable(Class<?> destination) {
        try {
            Intent intent = new Intent(this, destination);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.generic_navigation_error, Toast.LENGTH_SHORT).show();
        }
    }
}
