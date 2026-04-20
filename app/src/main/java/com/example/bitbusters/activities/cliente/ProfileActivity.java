package com.example.bitbusters.activities.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.bitbusters.R;
import com.example.bitbusters.activities.access.LoginActivity;
import com.example.bitbusters.utils.ImageUrls;

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