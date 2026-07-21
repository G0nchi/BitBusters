package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bitbusters.R;
import com.example.bitbusters.databinding.ActivityAsesorPerfilBinding;
import com.example.bitbusters.models.Proyecto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AsesorPerfilActivity extends AppCompatActivity {

    private static final String[] LABELS_CALIFICACION = {
        "Sin valoraciones", "Malo", "Regular", "Bueno", "Muy bueno", "Excelente"
    };

    private ActivityAsesorPerfilBinding binding;
    private SeparacionAdapter separacionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAsesorPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        cargarDatosAsesor();
        setupRecyclerView();
        setupBottomNav();
        cargarEstadisticas();
        cargarSeparaciones();
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

    /** Carga rating (valoraciones), total de citas y total de separaciones para el header. */
    private void cargarEstadisticas() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("valoraciones")
            .whereEqualTo("uidAsesor", uid)
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
                mostrarRating(promedio, vals.size());
            });

        db.collection("citas")
            .whereEqualTo("uidAsesor", uid)
            .get()
            .addOnSuccessListener(citas -> {
                TextView tvCitas = binding.getRoot().findViewById(R.id.tvStatCitas);
                if (tvCitas != null) tvCitas.setText(String.valueOf(citas.size()));
            });

        db.collection("separaciones")
            .whereEqualTo("uidAsesor", uid)
            .get()
            .addOnSuccessListener(seps -> {
                TextView tvSeps = binding.getRoot().findViewById(R.id.tvStatSeparaciones);
                if (tvSeps != null) tvSeps.setText(String.valueOf(seps.size()));

                double totalVentas = 0.0;
                for (QueryDocumentSnapshot doc : seps) {
                    if ("Pagado".equals(doc.getString("estadoPago"))) {
                        totalVentas += obtenerMontoSeguro(doc);
                    }
                }
                TextView tvVentas = binding.getRoot().findViewById(R.id.tvStatVentas);
                if (tvVentas != null) tvVentas.setText(formatMonto(totalVentas));
            });
    }

    private void mostrarRating(double promedio, int totalValoraciones) {
        TextView tvRating = binding.getRoot().findViewById(R.id.tvRatingPerfil);
        if (tvRating == null) return;
        if (totalValoraciones == 0) {
            tvRating.setText("— · Sin valoraciones");
            return;
        }
        int indice = Math.max(1, Math.min(5, (int) Math.round(promedio)));
        tvRating.setText(String.format(Locale.getDefault(), "%.1f · %s",
                promedio, LABELS_CALIFICACION[indice]));
    }

    /**
     * Lee el campo "monto" tolerando documentos legacy donde se guardó como String
     * en vez de número (doc.getDouble() lanza RuntimeException si el tipo no coincide).
     */
    private static double obtenerMontoSeguro(QueryDocumentSnapshot doc) {
        Object valor = doc.get("monto");
        if (valor instanceof Number) {
            return ((Number) valor).doubleValue();
        }
        if (valor instanceof String) {
            try {
                return Double.parseDouble(((String) valor).trim());
            } catch (NumberFormatException ignored) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private static String formatMonto(double monto) {
        if (monto >= 1_000_000) {
            return String.format(Locale.getDefault(), "S/ %.1fM", monto / 1_000_000);
        }
        if (monto >= 1_000) {
            return String.format(Locale.getDefault(), "S/ %,.0f", monto);
        }
        return String.format(Locale.getDefault(), "S/ %.0f", monto);
    }

    /** Carga las separaciones reales del asesor para el RecyclerView del perfil. */
    private void cargarSeparaciones() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Primero resolvemos las imágenes de los proyectos asignados, para no
        // depender de que la separación guarde su propia URL de imagen.
        db.collection("proyectos")
            .whereArrayContains("uidAsesores", uid)
            .get()
            .addOnSuccessListener(proyectosSnap -> {
                Map<String, String> imagenPorProyectoId = new HashMap<>();
                for (QueryDocumentSnapshot doc : proyectosSnap) {
                    Proyecto p = doc.toObject(Proyecto.class);
                    if (p == null) continue;
                    String url = p.getImageUrl();
                    if (url == null || url.isEmpty()) {
                        List<String> imgs = p.getImagenesUri();
                        url = (imgs != null && !imgs.isEmpty()) ? imgs.get(0) : "";
                    }
                    if (!url.isEmpty()) imagenPorProyectoId.put(doc.getId(), url);
                }
                cargarSeparacionesConImagenes(uid, imagenPorProyectoId);
            })
            .addOnFailureListener(e -> cargarSeparacionesConImagenes(uid, new HashMap<>()));
    }

    private void cargarSeparacionesConImagenes(String uid, Map<String, String> imagenPorProyectoId) {
        FirebaseFirestore.getInstance()
            .collection("separaciones")
            .whereEqualTo("uidAsesor", uid)
            .get()
            .addOnSuccessListener(snapshots -> {
                SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "PE"));
                fmt.setTimeZone(java.util.TimeZone.getTimeZone("America/Lima"));

                List<QueryDocumentSnapshot> docs = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshots) docs.add(doc);
                docs.sort((a, b) -> {
                    Date da = a.getDate("createdAt");
                    Date db2 = b.getDate("createdAt");
                    if (da == null && db2 == null) return 0;
                    if (da == null) return 1;
                    if (db2 == null) return -1;
                    return db2.compareTo(da);
                });

                List<SeparacionAdapter.Separacion> lista = new ArrayList<>();
                for (QueryDocumentSnapshot doc : docs) {
                    String proyecto = firstNonEmpty(doc.getString("proyecto"), doc.getString("proyectoNombre"));
                    String cliente  = firstNonEmpty(doc.getString("cliente"), doc.getString("nombreCliente"));
                    String estado   = doc.getString("estado");
                    String proyectoId = doc.getString("proyectoId");
                    double montoVal = obtenerMontoSeguro(doc);
                    Date createdAt  = doc.getDate("createdAt");

                    String monto = montoVal > 0
                            ? String.format(Locale.getDefault(), "S/ %,.0f", montoVal)
                            : "S/ 0";
                    String fecha = createdAt != null ? fmt.format(createdAt) : "";
                    String imageUrl = proyectoId != null ? imagenPorProyectoId.get(proyectoId) : null;

                    lista.add(new SeparacionAdapter.Separacion(
                            doc.getId().substring(0, Math.min(8, doc.getId().length())).toUpperCase(Locale.ROOT),
                            proyecto, cliente, monto,
                            estado != null ? estado : "Pendiente",
                            fecha, colorParaProyecto(proyecto), imageUrl));
                }

                separacionAdapter.setData(lista);
            });
    }

    private static String firstNonEmpty(String a, String b) {
        if (a != null && !a.isEmpty()) return a;
        return b != null ? b : "";
    }

    private static final int[] PALETA = {
        Color.parseColor("#B8C8D4"), Color.parseColor("#D4B896"), Color.parseColor("#A8C8A0")
    };

    private static int colorParaProyecto(String nombre) {
        if (nombre == null || nombre.isEmpty()) return PALETA[0];
        return PALETA[Math.abs(nombre.hashCode()) % PALETA.length];
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
        separacionAdapter = new SeparacionAdapter();
        binding.rvSeparaciones.setAdapter(separacionAdapter);
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
