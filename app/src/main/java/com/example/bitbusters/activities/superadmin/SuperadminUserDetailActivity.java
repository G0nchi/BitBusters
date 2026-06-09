package com.example.bitbusters.activities.superadmin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;
import com.example.bitbusters.utils.ImmersiveMode;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class SuperadminUserDetailActivity extends AppCompatActivity {

    private static final String STATUS_ACTIVE   = "active";
    private static final String STATUS_INACTIVE = "inactive";

    private String userId;
    private String currentStatus = STATUS_ACTIVE;

    private TextView userInitials;
    private TextView userName;
    private TextView userStatusPill;
    private TextView toggleStatusButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_superadmin_user_detail);

        userId = getIntent().getStringExtra("userId");

        userInitials     = findViewById(R.id.userInitials);
        userName         = findViewById(R.id.userName);
        userStatusPill   = findViewById(R.id.userStatusPill);
        toggleStatusButton = findViewById(R.id.toggleStatusButton);

        bindInsets();
        setupClicks();

        if (userId != null) {
            loadUser();
        }
    }

    private void loadUser() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    String nombre = doc.getString("nombre");
                    String status = doc.getString("status");
                    if (status == null) status = STATUS_ACTIVE;
                    currentStatus = status;

                    if (userName != null && nombre != null) {
                        userName.setText(nombre);
                    }
                    if (userInitials != null && nombre != null) {
                        userInitials.setText(computeInitials(nombre));
                    }
                    updateStatusUI();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error cargando usuario", Toast.LENGTH_SHORT).show());
    }

    private void toggleStatus() {
        String newStatus = STATUS_ACTIVE.equals(currentStatus) ? STATUS_INACTIVE : STATUS_ACTIVE;
        if (userId == null) return;

        toggleStatusButton.setEnabled(false);
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update("status", newStatus)
                .addOnSuccessListener(unused -> {
                    currentStatus = newStatus;
                    updateStatusUI();
                    toggleStatusButton.setEnabled(true);
                    int msg = STATUS_ACTIVE.equals(newStatus)
                            ? R.string.sa_enable_user
                            : R.string.sa_disable_user;
                    Toast.makeText(this, getString(msg) + " exitoso", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    toggleStatusButton.setEnabled(true);
                    Toast.makeText(this, "Error al actualizar estado", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateStatusUI() {
        if (userStatusPill == null || toggleStatusButton == null) return;

        if (STATUS_ACTIVE.equals(currentStatus)) {
            userStatusPill.setText(R.string.sa_status_active);
            userStatusPill.setBackgroundResource(R.drawable.sa_status_active_pill_bg);
            userStatusPill.setTextColor(ContextCompat.getColor(this, R.color.status_success));

            toggleStatusButton.setText(R.string.sa_disable_user);
            toggleStatusButton.setBackgroundResource(R.drawable.sa_disable_user_button_bg);
            toggleStatusButton.setTextColor(ContextCompat.getColor(this, R.color.status_error));
        } else {
            userStatusPill.setText(R.string.sa_status_inactive);
            userStatusPill.setBackgroundResource(R.drawable.sa_status_inactive_pill_bg);
            userStatusPill.setTextColor(ContextCompat.getColor(this, R.color.text_medium));

            toggleStatusButton.setText(R.string.sa_enable_user);
            toggleStatusButton.setBackgroundResource(R.drawable.sa_enable_user_button_bg);
            toggleStatusButton.setTextColor(ContextCompat.getColor(this, R.color.status_success));
        }
    }

    private void bindInsets() {
        View root = findViewById(R.id.main);
        if (root == null) return;
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupClicks() {
        View backButton = findViewById(R.id.backButton);
        if (backButton != null) backButton.setOnClickListener(v -> finish());
        if (toggleStatusButton != null) toggleStatusButton.setOnClickListener(v -> toggleStatus());
    }

    private String computeInitials(String nombre) {
        String[] parts = nombre.trim().split("\\s+");
        if (parts.length >= 2) {
            return (String.valueOf(parts[0].charAt(0)) + String.valueOf(parts[1].charAt(0))).toUpperCase(Locale.ROOT);
        }
        return nombre.length() >= 1 ? String.valueOf(nombre.charAt(0)).toUpperCase(Locale.ROOT) : "?";
    }
}
