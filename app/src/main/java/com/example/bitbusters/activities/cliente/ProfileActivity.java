package com.example.bitbusters.activities.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.bitbusters.R;
import com.example.bitbusters.activities.access.LoginActivity;
import com.example.bitbusters.utils.ImageUrls;
import com.example.bitbusters.utils.PreferencesManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;

    private ProgressBar progressProfile;
    private View layoutProfileError;
    private TextView tvProfileError;
    private View scrollProfileContent;

    private ImageView imgUserAvatar;
    private TextView tvNombre;
    private TextView tvEmail;
    private TextView tvLastAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firestore = FirebaseFirestore.getInstance();

        progressProfile = findViewById(R.id.progressProfile);
        layoutProfileError = findViewById(R.id.layoutProfileError);
        tvProfileError = findViewById(R.id.tvProfileError);
        scrollProfileContent = findViewById(R.id.scrollProfileContent);

        imgUserAvatar = findViewById(R.id.imgUserAvatar);
        tvNombre = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvUserEmail);
        tvLastAccess = findViewById(R.id.tvLastAccess);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Botón Editar (placeholder, ver BAJO-03)
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            // TODO: Implementar edición de perfil
        });

        findViewById(R.id.btnRetryProfile).setOnClickListener(v -> cargarPerfil());

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        cargarPerfil();
    }

    private void cargarPerfil() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        mostrarCargando();

        String uid = currentUser.getUid().trim();
        firestore.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        mostrarError("No se encontró tu perfil. Contacta al administrador.");
                        return;
                    }
                    String nombre = doc.getString("nombre");
                    String email = doc.getString("email");
                    String fotoUrl = doc.getString("fotoUrl");
                    mostrarPerfil(nombre, email, fotoUrl);
                })
                .addOnFailureListener(e -> mostrarError("Error al cargar tu perfil: " + e.getMessage()));
    }

    private void mostrarCargando() {
        progressProfile.setVisibility(View.VISIBLE);
        layoutProfileError.setVisibility(View.GONE);
        scrollProfileContent.setVisibility(View.GONE);
    }

    private void mostrarError(String mensaje) {
        progressProfile.setVisibility(View.GONE);
        scrollProfileContent.setVisibility(View.GONE);
        layoutProfileError.setVisibility(View.VISIBLE);
        tvProfileError.setText(mensaje);
    }

    private void mostrarPerfil(String nombre, String email, String fotoUrl) {
        progressProfile.setVisibility(View.GONE);
        layoutProfileError.setVisibility(View.GONE);
        scrollProfileContent.setVisibility(View.VISIBLE);

        tvNombre.setText(nombre != null && !nombre.trim().isEmpty() ? nombre : "Usuario");
        tvEmail.setText(email != null ? email : "");

        String ultimoAcceso = PreferencesManager.obtenerUltimoAcceso(this);
        tvLastAccess.setText(!ultimoAcceso.isEmpty() ? "Último acceso: " + ultimoAcceso : "");

        Glide.with(this)
                .load(fotoUrl != null && !fotoUrl.trim().isEmpty() ? fotoUrl : ImageUrls.AVATAR_JONATHAN)
                .centerCrop()
                .into(imgUserAvatar);
    }
}
