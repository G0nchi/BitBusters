package com.example.bitbusters.activities.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.bitbusters.R;
import com.example.bitbusters.activities.access.LoginActivity;
import com.example.bitbusters.utils.ImageUrls;
import com.example.bitbusters.utils.PreferencesManager;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Cargar avatar del usuario
        ImageView imgUserAvatar = findViewById(R.id.imgUserAvatar);
        if (imgUserAvatar != null) {
            Glide.with(this)
                    .load(ImageUrls.AVATAR_JONATHAN)
                    .centerCrop()
                    .into(imgUserAvatar);
        }

        // Mostrar nombre desde SharedPreferences (guardado al iniciar sesión)
        TextView tvNombre = findViewById(R.id.tvUserName);
        if (tvNombre != null) {
            tvNombre.setText(PreferencesManager.obtenerNombre(this));
        }

        // Mostrar último acceso si fue registrado al hacer login
        TextView tvEmail = findViewById(R.id.tvUserEmail);
        String ultimoAcceso = PreferencesManager.obtenerUltimoAcceso(this);
        if (tvEmail != null && !ultimoAcceso.isEmpty()) {
            tvEmail.setText("Último acceso: " + ultimoAcceso);
        }

        // Botón volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Botón Editar (placeholder)
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            // TODO: Implementar edición de perfil
        });

        // Botón Cerrar Sesión
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}