package com.example.bitbusters.activities.admin;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilidad one-shot de migración. NO enlazar en flujos normales de la app.
 *
 * Lanzar con adb:
 *   adb shell am start -n com.example.bitbusters/.activities.admin.AdminSeedActivity
 *
 * Qué hace:
 *   1. Lee usuarios con role="asesor" de la colección "users".
 *   2. Si no hay ninguno, crea un asesor de prueba (uid="asesor_demo_001").
 *   3. Lee proyectos cuyo campo uidAsesores esté ausente o vacío.
 *   4. Asigna asesores en round-robin (todos los proyectos quedan con al menos uno).
 *   5. Escribe con WriteBatch (máx 499 ops por batch).
 *   6. Reporta en Logcat y en pantalla cuántos documentos migró.
 */
public class AdminSeedActivity extends AppCompatActivity {

    private static final String TAG = "AdminSeed";

    private TextView tvLog;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final StringBuilder logBuffer = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_seed);

        tvLog = findViewById(R.id.tvLog);
        Button btnEjecutar = findViewById(R.id.btnEjecutarMigracion);
        btnEjecutar.setOnClickListener(v -> {
            btnEjecutar.setEnabled(false);
            logBuffer.setLength(0);
            appendLog("=== Migración uidAsesores iniciada ===");
            ejecutarMigracion(btnEjecutar);
        });
    }

    // ── Paso 1: obtener asesores ───────────────────────────────────────────────

    private void ejecutarMigracion(Button btn) {
        db.collection("users")
            .whereEqualTo("role", "asesor")
            .get()
            .addOnSuccessListener(snap -> {
                List<String> uids = new ArrayList<>();
                for (DocumentSnapshot doc : snap.getDocuments()) uids.add(doc.getId());
                appendLog("Asesores con role=asesor encontrados: " + uids.size());

                if (uids.isEmpty()) {
                    appendLog("No hay asesores. Creando asesor de prueba...");
                    crearAsesorDemoYMigrar(btn);
                } else {
                    migrarProyectos(uids, btn);
                }
            })
            .addOnFailureListener(e -> {
                appendLog("ERROR leyendo users: " + e.getMessage());
                Log.e(TAG, "Error users query", e);
                btn.setEnabled(true);
            });
    }

    // ── Paso 2 (opcional): crear asesor demo si no hay ninguno ────────────────

    private void crearAsesorDemoYMigrar(Button btn) {
        String demoUid = "asesor_demo_001";
        Map<String, Object> data = new HashMap<>();
        data.put("uid",       demoUid);
        data.put("nombre",    "Asesor Demo");
        data.put("email",     "asesor.demo@bitbusters.dev");
        data.put("role",      "asesor");
        data.put("activo",    true);
        data.put("creadoEn",  FieldValue.serverTimestamp());

        db.collection("users").document(demoUid).set(data)
            .addOnSuccessListener(v -> {
                appendLog("Asesor demo creado: users/" + demoUid);
                migrarProyectos(Arrays.asList(demoUid), btn);
            })
            .addOnFailureListener(e -> {
                appendLog("ERROR creando asesor demo: " + e.getMessage());
                Log.e(TAG, "Error demo asesor", e);
                btn.setEnabled(true);
            });
    }

    // ── Paso 3: leer proyectos sin uidAsesores y asignar en round-robin ───────

    @SuppressWarnings("unchecked")
    private void migrarProyectos(List<String> uidAsesores, Button btn) {
        appendLog("Cargando proyectos...");
        db.collection("proyectos").get()
            .addOnSuccessListener(snap -> {
                List<DocumentSnapshot> sinAsesor = new ArrayList<>();
                for (DocumentSnapshot doc : snap.getDocuments()) {
                    List<String> ua = (List<String>) doc.get("uidAsesores");
                    if (ua == null || ua.isEmpty()) sinAsesor.add(doc);
                }
                appendLog("Total proyectos: " + snap.size()
                        + " | Sin uidAsesores: " + sinAsesor.size());

                if (sinAsesor.isEmpty()) {
                    appendLog("Nada que migrar: todos los proyectos ya tienen asesor.");
                    btn.setEnabled(true);
                    return;
                }

                // Estrategia: round-robin sobre la lista de asesores disponibles
                List<WriteBatch> batches = new ArrayList<>();
                WriteBatch batch = db.batch();
                int ops = 0;
                int rrIdx = 0;

                for (DocumentSnapshot doc : sinAsesor) {
                    String uid = uidAsesores.get(rrIdx % uidAsesores.size());
                    rrIdx++;
                    batch.update(doc.getReference(), "uidAsesores", Arrays.asList(uid));
                    appendLog("  proyectos/" + doc.getId() + "  ← [" + uid + "]");
                    ops++;
                    if (ops == 499) {
                        batches.add(batch);
                        batch = db.batch();
                        ops = 0;
                    }
                }
                if (ops > 0) batches.add(batch);

                commitBatches(batches, 0, sinAsesor.size(), btn);
            })
            .addOnFailureListener(e -> {
                appendLog("ERROR leyendo proyectos: " + e.getMessage());
                Log.e(TAG, "Error proyectos query", e);
                btn.setEnabled(true);
            });
    }

    // ── Paso 4: commit encadenado de batches ──────────────────────────────────

    private void commitBatches(List<WriteBatch> batches, int idx, int total, Button btn) {
        if (idx >= batches.size()) {
            String msg = "=== Migración completa: " + total + " proyectos actualizados ===";
            appendLog(msg);
            Log.i(TAG, msg);
            btn.setEnabled(true);
            return;
        }
        batches.get(idx).commit()
            .addOnSuccessListener(v -> {
                appendLog("Batch " + (idx + 1) + "/" + batches.size() + " OK");
                commitBatches(batches, idx + 1, total, btn);
            })
            .addOnFailureListener(e -> {
                appendLog("ERROR en batch " + (idx + 1) + ": " + e.getMessage());
                Log.e(TAG, "Batch " + idx + " error", e);
                btn.setEnabled(true);
            });
    }

    // ── Utilitario ─────────────────────────────────────────────────────────────

    private void appendLog(String line) {
        Log.d(TAG, line);
        logBuffer.append(line).append("\n");
        runOnUiThread(() -> tvLog.setText(logBuffer.toString()));
    }
}
