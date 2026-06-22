package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bitbusters.R;
import com.example.bitbusters.databinding.ActivityAsesorPerfilBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AsesorPerfilActivity extends AppCompatActivity {

    private ActivityAsesorPerfilBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAsesorPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        cargarDatosAsesor();
        setupRecyclerView();
        setupBottomNav();
    }

    private void cargarDatosAsesor() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.getUid())
            .get()
            .addOnSuccessListener(doc -> {
                String nombre = doc.exists() ? doc.getString("nombre") : null;
                if (nombre == null || nombre.isEmpty()) nombre = "Asesor";

                TextView tvNombre   = binding.getRoot().findViewById(R.id.tvNombrePerfil);
                TextView tvInitials = binding.getRoot().findViewById(R.id.tvInitialsPerfil);

                if (tvNombre   != null) tvNombre.setText(nombre);
                if (tvInitials != null) tvInitials.setText(obtenerIniciales(nombre));
            });
    }

    private static String obtenerIniciales(String nombre) {
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length == 0) return "AS";
        if (partes.length == 1) return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        return (partes[0].substring(0, 1) + partes[1].substring(0, 1)).toUpperCase();
    }

    private void setupRecyclerView() {
        binding.rvSeparaciones.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSeparaciones.setNestedScrollingEnabled(false);
        binding.rvSeparaciones.setAdapter(new SeparacionAdapter());
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_perfil);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, AsesorHomeActivity.class));
                finish();
            } else if (id == R.id.nav_citas) {
                startActivity(new Intent(this, CitasAgendadasActivity.class));
                finish();
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, MensajesActivity.class));
                finish();
            } else if (id == R.id.nav_reportes) {
                startActivity(new Intent(this, AsesorReportesActivity.class));
                finish();
            }
            return true;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
