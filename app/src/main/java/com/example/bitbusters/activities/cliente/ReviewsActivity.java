package com.example.bitbusters.activities.cliente;


import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.ClientReviewsAdapter;
import com.example.bitbusters.models.ClientReview;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReviewsActivity extends AppCompatActivity {

    public static final String EXTRA_PROYECTO = "proyecto";
    public static final String EXTRA_PROYECTO_ID = "proyecto_id";

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ClientReviewsAdapter adapter;
    private ProgressBar progressReviews;
    private View layoutReviewsError;
    private TextView tvReviewsError;
    private View layoutReviewsContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        String nombreProyecto = getIntent().getStringExtra(EXTRA_PROYECTO);
        String proyectoId = getIntent().getStringExtra(EXTRA_PROYECTO_ID);
        if (nombreProyecto != null) {
            ((TextView) findViewById(R.id.tvNombreProyecto)).setText(nombreProyecto);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        progressReviews = findViewById(R.id.progressReviews);
        layoutReviewsError = findViewById(R.id.layoutReviewsError);
        tvReviewsError = findViewById(R.id.tvReviewsError);
        layoutReviewsContent = findViewById(R.id.layoutReviewsContent);
        findViewById(R.id.btnRetryReviews).setOnClickListener(v -> cargarValoraciones(proyectoId, nombreProyecto));

        RecyclerView recyclerView = findViewById(R.id.recyclerViewReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClientReviewsAdapter();
        recyclerView.setAdapter(adapter);

        mostrarCargando();
        cargarValoraciones(proyectoId, nombreProyecto);
    }

    private void cargarValoraciones(String proyectoId, String nombreProyecto) {
        Query query;
        if (proyectoId != null) {
            query = firestore.collection("valoraciones").whereEqualTo("proyectoId", proyectoId);
        } else {
            query = firestore.collection("valoraciones").whereEqualTo("proyecto", nombreProyecto);
        }

        query.get()
                .addOnSuccessListener(snapshot -> {
                    mostrarContenido();
                    List<DocumentSnapshot> docs = new ArrayList<>(snapshot.getDocuments());
                    Collections.sort(docs, (a, b) -> {
                        Timestamp ta = a.getTimestamp("timestamp");
                        Timestamp tb = b.getTimestamp("timestamp");
                        if (ta == null && tb == null) return 0;
                        if (ta == null) return 1;
                        if (tb == null) return -1;
                        return tb.compareTo(ta);
                    });
                    resolverNombresYMostrar(docs);
                })
                .addOnFailureListener(e -> {
                    mostrarError("No se pudieron cargar las reseñas");
                    adapter.submitList(new ArrayList<>());
                });
    }

    private void resolverNombresYMostrar(List<DocumentSnapshot> docs) {
        if (docs.isEmpty()) {
            adapter.submitList(new ArrayList<>());
            return;
        }

        Set<String> uids = new LinkedHashSet<>();
        for (DocumentSnapshot doc : docs) {
            String uid = doc.getString("uidCliente");
            if (uid != null && !uid.isEmpty()) uids.add(uid);
        }

        if (uids.isEmpty()) {
            mostrarContenido();
            adapter.submitList(construirReviews(docs, new HashMap<>()));
            return;
        }

        firestore.collection("users")
                .whereIn(FieldPath.documentId(), new ArrayList<>(uids))
                .get()
                .addOnSuccessListener(usersSnapshot -> {
                    Map<String, String> nombresPorUid = new HashMap<>();
                    for (DocumentSnapshot userDoc : usersSnapshot.getDocuments()) {
                        nombresPorUid.put(userDoc.getId(), userDoc.getString("nombre"));
                    }
                    mostrarContenido();
                    adapter.submitList(construirReviews(docs, nombresPorUid));
                })
                .addOnFailureListener(e -> {
                    mostrarContenido();
                    adapter.submitList(construirReviews(docs, new HashMap<>()));
                });
    }

    private void mostrarCargando() {
        if (progressReviews != null) progressReviews.setVisibility(View.VISIBLE);
        if (layoutReviewsError != null) layoutReviewsError.setVisibility(View.GONE);
        if (layoutReviewsContent != null) layoutReviewsContent.setVisibility(View.GONE);
    }

    private void mostrarContenido() {
        if (progressReviews != null) progressReviews.setVisibility(View.GONE);
        if (layoutReviewsError != null) layoutReviewsError.setVisibility(View.GONE);
        if (layoutReviewsContent != null) layoutReviewsContent.setVisibility(View.VISIBLE);
    }

    private void mostrarError(String mensaje) {
        if (progressReviews != null) progressReviews.setVisibility(View.GONE);
        if (layoutReviewsContent != null) layoutReviewsContent.setVisibility(View.GONE);
        if (layoutReviewsError != null) layoutReviewsError.setVisibility(View.VISIBLE);
        if (tvReviewsError != null) tvReviewsError.setText(mensaje);
    }

    private List<ClientReview> construirReviews(List<DocumentSnapshot> docs, Map<String, String> nombresPorUid) {
        List<ClientReview> reviews = new ArrayList<>();
        for (DocumentSnapshot doc : docs) {
            String uid = doc.getString("uidCliente");
            String nombre = nombresPorUid.get(uid);
            Long calificacion = doc.getLong("calificacion");
            String comentario = doc.getString("comentario");
            reviews.add(new ClientReview(
                    nombre != null && !nombre.trim().isEmpty() ? nombre : "Cliente",
                    calificacion != null ? calificacion.intValue() : 0,
                    comentario != null ? comentario : "",
                    formatearTiempoRelativo(doc.getTimestamp("timestamp"))
            ));
        }
        return reviews;
    }

    private String formatearTiempoRelativo(Timestamp timestamp) {
        if (timestamp == null) return "";
        long ahora = System.currentTimeMillis();
        long cuando = timestamp.toDate().getTime();
        CharSequence texto = DateUtils.getRelativeTimeSpanString(
                cuando, ahora, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
        return texto.toString();
    }
}
