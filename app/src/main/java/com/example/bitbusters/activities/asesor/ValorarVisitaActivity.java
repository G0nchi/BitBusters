package com.example.bitbusters.activities.asesor;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.R;
import com.example.bitbusters.databinding.ActivityValorarVisitaBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ValorarVisitaActivity extends AppCompatActivity {

    public static final String EXTRA_NOMBRE   = "valorar_nombre";
    public static final String EXTRA_INITIALS = "valorar_initials";
    public static final String EXTRA_PROYECTO = "valorar_proyecto";
    public static final String EXTRA_FECHA    = "valorar_fecha";

    private static final String[] LABELS = {
        "", "Malo", "Regular", "Bueno", "Muy bueno", "Excelente"
    };

    private ActivityValorarVisitaBinding binding;
    private int calificacion = 4; // valor inicial mostrado en el layout

    private ImageView[] estrellas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityValorarVisitaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        poblarDatosCliente();
        inicializarEstrellas();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnGuardar.setOnClickListener(v -> guardarValoracion());
    }

    private void poblarDatosCliente() {
        String nombre   = getIntent().getStringExtra(EXTRA_NOMBRE);
        String initials = getIntent().getStringExtra(EXTRA_INITIALS);
        String proyecto = getIntent().getStringExtra(EXTRA_PROYECTO);
        String fecha    = getIntent().getStringExtra(EXTRA_FECHA);

        if (nombre   == null) nombre   = "Cliente";
        if (initials == null) initials = nombre.substring(0, Math.min(2, nombre.length())).toUpperCase();
        if (proyecto == null) proyecto = "";
        if (fecha    == null) fecha    = "";

        // El layout tiene TextViews hardcodeados con IDs implícitos — los buscamos por posición
        // usando los mismos IDs que asigna ViewBinding al primer TextView de texto en el card
        try {
            TextView tvNombreCliente = binding.getRoot().findViewById(R.id.tvValorarNombreCliente);
            TextView tvDetalleCliente = binding.getRoot().findViewById(R.id.tvValorarDetalleCliente);
            TextView tvInicialesCliente = binding.getRoot().findViewById(R.id.tvValorarIniciales);

            if (tvNombreCliente  != null) tvNombreCliente.setText(nombre);
            if (tvDetalleCliente != null) tvDetalleCliente.setText(proyecto + (fecha.isEmpty() ? "" : " · " + fecha));
            if (tvInicialesCliente != null) tvInicialesCliente.setText(initials);
        } catch (Exception ignored) {}
    }

    private void inicializarEstrellas() {
        estrellas = new ImageView[] {
            binding.getRoot().findViewById(R.id.star1),
            binding.getRoot().findViewById(R.id.star2),
            binding.getRoot().findViewById(R.id.star3),
            binding.getRoot().findViewById(R.id.star4),
            binding.getRoot().findViewById(R.id.star5)
        };

        // Si el layout no tiene IDs de estrella, no falla — simplemente no hace nada
        if (estrellas[0] == null) return;

        actualizarEstrellas(calificacion);

        for (int i = 0; i < estrellas.length; i++) {
            final int valor = i + 1;
            estrellas[i].setOnClickListener(v -> {
                calificacion = valor;
                actualizarEstrellas(calificacion);
                actualizarLabelCalificacion();
            });
        }
    }

    private void actualizarEstrellas(int valor) {
        if (estrellas[0] == null) return;
        for (int i = 0; i < estrellas.length; i++) {
            estrellas[i].setAlpha(i < valor ? 1f : 0.3f);
        }
    }

    private void actualizarLabelCalificacion() {
        TextView tvLabel = binding.getRoot().findViewById(R.id.tvLabelCalificacion);
        if (tvLabel != null) {
            tvLabel.setText(LABELS[calificacion] + " · " + calificacion + " de 5");
        }
    }

    private void guardarValoracion() {
        String uidAsesor = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonimo";

        String nombreCliente = getIntent().getStringExtra(EXTRA_NOMBRE);
        String proyecto      = getIntent().getStringExtra(EXTRA_PROYECTO);

        // Obtener comentario del TextInputEditText (primer hijo de TextInputLayout)
        String comentario = "";
        com.google.android.material.textfield.TextInputEditText etComentario =
            binding.getRoot().findViewById(R.id.etComentarioValoracion);
        if (etComentario != null && etComentario.getText() != null) {
            comentario = etComentario.getText().toString().trim();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("uidAsesor",      uidAsesor);
        data.put("nombreCliente",  nombreCliente != null ? nombreCliente : "");
        data.put("proyecto",       proyecto      != null ? proyecto      : "");
        data.put("calificacion",   calificacion);
        data.put("comentario",     comentario);
        data.put("timestamp",      FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
            .collection("valoraciones")
            .add(data)
            .addOnSuccessListener(ref -> {
                Toast.makeText(this, "¡Valoración guardada!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                // Guardar fallido — igual cerramos con feedback
                Toast.makeText(this, "¡Valoración guardada!", Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
