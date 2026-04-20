package com.example.bitbusters.activities.access;

import android.content.Intent;
import android.os.Bundle;

import com.example.bitbusters.utils.ImmersiveMode;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;
import com.google.android.material.button.MaterialButton;

public class RegisterAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_register_account);

        MaterialButton backButton = findViewById(R.id.backButton);
        MaterialButton nextButton = findViewById(R.id.nextButton);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        if (nextButton != null) {
            nextButton.setOnClickListener(v -> startActivity(new Intent(this, RegisterOtpActivity.class)));
        }

        if (findViewById(R.id.main) != null) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }
}
