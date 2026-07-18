package com.example.bitbusters.activities.cliente;


import android.os.Bundle;
import android.text.format.DateUtils;
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

        RecyclerView recyclerView = findViewById(R.id.recyclerViewReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClientReviewsAdapter();
        recyclerView.setAdapter(adapter);

        cargarValoraciones(proyectoId, nombreProyecto);
    }

    private void cargarValoraciones(String proyectoId, String nombreProyecto) {
        Query query = (proyectoId != null && !proyectoId.isEmpty())
                ? firestore.collection("valoraciones").whereEqualTo("proyectoId", proyectoId)
                : firestore.collection("valoraciones").whereEqualTo("proyecto", nombreProyecto);

        query.get()
                .addOnSuccessListener(snapshot -> {
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
                .addOnFailureListener(e -> adapter.submitList(new ArrayList<>()));
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
                    adapter.submitList(construirReviews(docs, nombresPorUid));
                })
                .addOnFailureListener(e -> adapter.submitList(construirReviews(docs, new HashMap<>())));
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
