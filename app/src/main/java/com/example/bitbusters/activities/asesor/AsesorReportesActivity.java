package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.R;
import com.example.bitbusters.databinding.ActivityAsesorReportesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AsesorReportesActivity extends AppCompatActivity {

    private ActivityAsesorReportesBinding binding;
    private String uidAsesor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAsesorReportesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uidAsesor = (user != null) ? user.getUid() : "";

        binding.btnBack.setOnClickListener(v -> finish());
        setupBottomNav();
        cargarKPIs();
    }

    private void cargarKPIs() {
        if (uidAsesor.isEmpty()) {
            mostrarKPIs(0, 0, 0, 0.0);
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Citas confirmadas
        db.collection("citas")
            .whereEqualTo("uidAsesor", uidAsesor)
            .get()
            .addOnSuccessListener(citas -> {
                int totalCitas = citas.size();
                int confirmadas = 0;
                for (QueryDocumentSnapshot doc : citas) {
                    String estado = doc.getString("estado");
                    if ("Confirmada".equals(estado) || "Realizada".equals(estado)) confirmadas++;
                }
                final int citasConfirmadas = confirmadas;

                // Separaciones del asesor
                db.collection("separaciones")
                    .whereEqualTo("uidAsesor", uidAsesor)
                    .get()
                    .addOnSuccessListener(seps -> {
                        int totalSeps = seps.size();

                        // Valoraciones
                        db.collection("valoraciones")
                            .whereEqualTo("uidAsesor", uidAsesor)
                            .get()
                            .addOnSuccessListener(vals -> {
                                double promedio = 0.0;
                                if (!vals.isEmpty()) {
                                    double suma = 0;
                                    for (QueryDocumentSnapshot v : vals) {
                                        Long cal = v.getLong("calificacion");
                                        if (cal != null) suma += cal;
                                    }
                                    promedio = suma / vals.size();
                                }
                                mostrarKPIs(totalCitas, citasConfirmadas, totalSeps, promedio);
                            })
                            .addOnFailureListener(e -> mostrarKPIs(totalCitas, citasConfirmadas, totalSeps, 0.0));
                    })
                    .addOnFailureListener(e -> mostrarKPIs(totalCitas, 0, 0, 0.0));
            })
            .addOnFailureListener(e -> mostrarKPIs(0, 0, 0, 0.0));
    }

    private void mostrarKPIs(int totalCitas, int citasConfirmadas, int separaciones, double rating) {
        binding.tvTotalCitas.setText(String.valueOf(totalCitas));
        binding.tvCitasConfirmadas.setText(String.valueOf(citasConfirmadas));
        binding.tvSeparaciones.setText(String.valueOf(separaciones));

        if (rating > 0) {
            binding.tvRating.setText(String.format(java.util.Locale.getDefault(), "%.1f", rating));
        } else {
            binding.tvRating.setText("—");
        }

        int conversion = totalCitas > 0 ? (int) ((citasConfirmadas * 100.0) / totalCitas) : 0;
        binding.tvConversion.setText(conversion + "%");
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_reportes);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, AsesorHomeActivity.class)); finish();
            } else if (id == R.id.nav_citas) {
                startActivity(new Intent(this, CitasAgendadasActivity.class)); finish();
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, MensajesActivity.class)); finish();
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, AsesorPerfilActivity.class)); finish();
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
