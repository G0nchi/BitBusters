package com.example.bitbusters.activities.superadmin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.bitbusters.utils.ImmersiveMode;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;

public class SuperadminApprovalEvaluationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_superadmin_approval_evaluation);

        bindInsets();
        setupClicks();
    }

    private void bindInsets() {
        View root = findViewById(R.id.main);
        if (root == null) {
            return;
        }
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupClicks() {
        View backButton = findViewById(R.id.backButton);
        View rejectButton = findViewById(R.id.rejectButton);
        View approveButton = findViewById(R.id.approveButton);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        if (rejectButton != null) {
            rejectButton.setOnClickListener(v ->
                    Toast.makeText(this, getString(R.string.sa_reject), Toast.LENGTH_SHORT).show());
        }
        if (approveButton != null) {
            approveButton.setOnClickListener(v ->
                    Toast.makeText(this, getString(R.string.sa_approve), Toast.LENGTH_SHORT).show());
        }
    }
}
