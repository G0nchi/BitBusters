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

public class ProjectTypeSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_project_type_selection);

        MaterialButton backButton = findViewById(R.id.backButton);
        MaterialButton skipButton = findViewById(R.id.skipButton);
        MaterialButton nextButton = findViewById(R.id.nextButton);

        backButton.setOnClickListener(v -> finish());

        skipButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProjectTypeSelectionActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        nextButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProjectTypeSelectionActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
