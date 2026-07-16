package com.example.bitbusters.activities.cliente;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddCommentActivity extends AppCompatActivity {

    private ImageButton star1, star2, star3, star4, star5;
    private TextView tvRating;
    private EditText etComentario;
    private TextView btnGuardar;
    private int calificacionSeleccionada = 0;
    private boolean guardando = false;

    private ImageButton[] estrellas;

    private FirebaseFirestore firestore;
    private String citaId;
    private String proyectoNombre;
    private String proyectoId;
    private String uidAsesor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);

        firestore = FirebaseFirestore.getInstance();

        proyectoNombre = getIntent().getStringExtra("proyecto");
        citaId = getIntent().getStringExtra("citaId");
        proyectoId = getIntent().getStringExtra("proyectoId");
        uidAsesor = getIntent().getStringExtra("uidAsesor");

        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);
        star5 = findViewById(R.id.star5);
        tvRating = findViewById(R.id.tvRating);
        etComentario = findViewById(R.id.etComentario);
        btnGuardar = findViewById(R.id.btnGuardar);
        estrellas = new ImageButton[]{star1, star2, star3, star4, star5};

        // Botón volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Lógica de estrellas interactivas
        for (int i = 0; i < estrellas.length; i++) {
            final int posicion = i + 1;
            estrellas[i].setOnClickListener(v -> seleccionarEstrellas(posicion));
        }

        // Botón Guardar
        btnGuardar.setOnClickListener(v -> guardarComentario());
    }

    private void seleccionarEstrellas(int cantidad) {
        calificacionSeleccionada = cantidad;
        tvRating.setText(String.valueOf((float) cantidad));

        for (int i = 0; i < estrellas.length; i++) {
            if (i < cantidad) {
                estrellas[i].setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                estrellas[i].setImageResource(android.R.drawable.btn_star_big_off);
            }
        }
    }

    private void guardarComentario() {
        if (guardando) return;

        String comentario = etComentario.getText().toString().trim();

        if (calificacionSeleccionada == 0) {
            Toast.makeText(this, "Por favor selecciona una calificación", Toast.LENGTH_SHORT).show();
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Debes iniciar sesión para valorar", Toast.LENGTH_SHORT).show();
            return;
        }

        String uidCliente = FirebaseAuth.getInstance().getCurrentUser().getUid().trim();

        Map<String, Object> data = new HashMap<>();
        data.put("uidCliente", uidCliente);
        data.put("uidAsesor", uidAsesor != null ? uidAsesor : "");
        data.put("proyecto", proyectoNombre != null ? proyectoNombre : "");
        data.put("proyectoId", proyectoId != null ? proyectoId : "");
        data.put("citaId", citaId != null ? citaId : "");
        data.put("calificacion", calificacionSeleccionada);
        data.put("comentario", comentario);
        data.put("timestamp", FieldValue.serverTimestamp());

        mostrarGuardando(true);

        firestore.collection("valoraciones")
                .add(data)
                .addOnSuccessListener(ref -> marcarCitaValorada())
                .addOnFailureListener(e -> {
                    mostrarGuardando(false);
                    Snackbar.make(btnGuardar, "No se pudo guardar tu valoración. Intenta de nuevo.", Snackbar.LENGTH_LONG)
                            .setAction("Reintentar", v -> guardarComentario())
                            .show();
                });
    }

    private void marcarCitaValorada() {
        if (citaId == null || citaId.isEmpty()) {
            finalizarConExito();
            return;
        }
        // Campo booleano independiente de "estado" — no altera el ciclo de vida de la cita (ver PC-06).
        firestore.collection("citas").document(citaId)
                .update("valoradaCliente", true)
                .addOnSuccessListener(unused -> finalizarConExito())
                .addOnFailureListener(e -> finalizarConExito());
    }

    private void finalizarConExito() {
        Toast.makeText(this, "¡Comentario guardado!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void mostrarGuardando(boolean guardandoAhora) {
        guardando = guardandoAhora;
        btnGuardar.setEnabled(!guardandoAhora);
        btnGuardar.setAlpha(guardandoAhora ? 0.5f : 1f);
        btnGuardar.setText(guardandoAhora ? "Guardando..." : "Guardar");
    }
}
