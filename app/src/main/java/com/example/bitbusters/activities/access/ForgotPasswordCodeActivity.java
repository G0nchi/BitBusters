package com.example.bitbusters.activities.access;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;
import com.example.bitbusters.utils.ImmersiveMode;
import com.google.android.material.button.MaterialButton;

public class ForgotPasswordCodeActivity extends AppCompatActivity {

    public static final String EXTRA_RECOVERY_EMAIL = "extra_recovery_email";
    private static final String DEMO_CODE = "1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_forgot_password_code);

        MaterialButton backButton = findViewById(R.id.backButton);
        MaterialButton verifyCodeButton = findViewById(R.id.verifyCodeButton);
        TextView resendCodeText = findViewById(R.id.resendCodeText);
        TextView codeSubtitle = findViewById(R.id.codeSubtitle);
        EditText codeInput = findViewById(R.id.codeInput);

        String email = getIntent() != null ? getIntent().getStringExtra(EXTRA_RECOVERY_EMAIL) : "";
        if (email == null) {
            email = "";
        }

        if (codeSubtitle != null) {
            codeSubtitle.setText(getString(R.string.forgot_password_code_subtitle, email));
        }

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        if (resendCodeText != null) {
            String finalEmail = email;
            resendCodeText.setOnClickListener(v -> Toast.makeText(
                    this,
                    getString(R.string.forgot_password_code_sent, finalEmail),
                    Toast.LENGTH_SHORT
            ).show());
        }

        if (verifyCodeButton != null) {
            String finalEmail = email;
            verifyCodeButton.setOnClickListener(v -> verifyCode(codeInput, finalEmail));
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void verifyCode(EditText codeInput, String email) {
        String code = codeInput != null && codeInput.getText() != null
                ? codeInput.getText().toString().trim()
                : "";

        if (!DEMO_CODE.equals(code)) {
            if (codeInput != null) {
                codeInput.setError(getString(R.string.forgot_password_code_invalid));
            }
            return;
        }

        Intent intent = new Intent(this, ResetPasswordActivity.class);
        intent.putExtra(EXTRA_RECOVERY_EMAIL, email);
        startActivity(intent);
    }
}
