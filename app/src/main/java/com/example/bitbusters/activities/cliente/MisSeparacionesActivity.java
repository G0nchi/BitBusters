package com.example.bitbusters.activities.cliente;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.ClienteSeparacionesAdapter;
import com.example.bitbusters.models.ClienteSeparacion;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MisSeparacionesActivity extends AppCompatActivity {

    private ProgressBar progress;
    private TextView tvEmpty;
    private RecyclerView rvSeparaciones;
    private ClienteSeparacionesAdapter adapter;
    private ListenerRegistration listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_separaciones);

        progress = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvSeparaciones = findViewById(R.id.rvSeparaciones);
        adapter = new ClienteSeparacionesAdapter();
        rvSeparaciones.setLayoutManager(new LinearLayoutManager(this));
        rvSeparaciones.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    protected void onStart() {
        super.onStart();
        escucharSeparaciones();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }

    private void escucharSeparaciones() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";
        if (uid.isEmpty()) {
            Toast.makeText(this, "Debes iniciar sesión para ver tus separaciones.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mostrarCargando();
        listener = FirebaseFirestore.getInstance()
                .collection("separaciones")
                .whereEqualTo("uidCliente", uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        mostrarVacio("No se pudieron cargar tus separaciones: " + error.getMessage());
                        return;
                    }
                    List<ClienteSeparacion> data = new ArrayList<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            data.add(mapSeparacion(doc));
                        }
                    }
                    Collections.sort(data, (a, b) -> Long.compare(b.getOrdenMillis(), a.getOrdenMillis()));
                    adapter.setData(data);
                    if (data.isEmpty()) {
                        mostrarVacio("Aún no tienes separaciones registradas.");
                    } else {
                        mostrarContenido();
                    }
                });
    }

    private ClienteSeparacion mapSeparacion(DocumentSnapshot doc) {
        ClienteSeparacion item = new ClienteSeparacion();
        item.setId(doc.getId());
        item.setProyectoNombre(firstNonEmpty(doc.getString("proyectoNombre"), doc.getString("proyecto"), "Proyecto"));
        item.setAsesorNombre(firstNonEmpty(doc.getString("asesorNombre"), doc.getString("asesor"), doc.getString("uidAsesor")));
        item.setMonto(formatearMonto(doc));
        item.setEstado(firstNonEmpty(doc.getString("estado"), "Pendiente", "Pendiente"));
        item.setEstadoPago(firstNonEmpty(doc.getString("estadoPago"), doc.getString("pagoEstado"), "Pendiente"));
        item.setMetodoPago(firstNonEmpty(doc.getString("metodoPago"), "Tarjeta", "Tarjeta"));
        item.setOrdenMillis(fechaOrden(doc));
        item.setFecha(formatearFecha(item.getOrdenMillis()));
        return item;
    }

    private String formatearMonto(DocumentSnapshot doc) {
        Object monto = doc.get("monto");
        if (monto instanceof Number) {
            return String.format(Locale.getDefault(), "S/ %.2f", ((Number) monto).doubleValue());
        }
        String text = monto != null ? String.valueOf(monto) : "";
        if (text.trim().isEmpty()) return "S/ 0";
        return text.startsWith("S/") ? text : "S/ " + text;
    }

    private long fechaOrden(DocumentSnapshot doc) {
        Timestamp timestamp = firstTimestamp(doc, "pagoRegistradoEn", "fechaPago", "updatedAt", "createdAt", "timestamp");
        return timestamp != null ? timestamp.toDate().getTime() : 0L;
    }

    private Timestamp firstTimestamp(DocumentSnapshot doc, String... fields) {
        for (String field : fields) {
            Timestamp value = doc.getTimestamp(field);
            if (value != null) return value;
        }
        return null;
    }

    private String formatearFecha(long millis) {
        if (millis <= 0L) return "Sin fecha";
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(millis));
    }

    private void mostrarCargando() {
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvSeparaciones.setVisibility(View.GONE);
    }

    private void mostrarContenido() {
        progress.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        rvSeparaciones.setVisibility(View.VISIBLE);
    }

    private void mostrarVacio(String mensaje) {
        progress.setVisibility(View.GONE);
        rvSeparaciones.setVisibility(View.GONE);
        tvEmpty.setText(mensaje);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    private String firstNonEmpty(String primary, String secondary, String fallback) {
        if (primary != null && !primary.trim().isEmpty()) return primary;
        if (secondary != null && !secondary.trim().isEmpty()) return secondary;
        return fallback != null ? fallback : "";
    }
}
