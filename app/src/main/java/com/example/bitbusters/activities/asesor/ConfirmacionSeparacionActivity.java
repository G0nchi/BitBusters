package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.databinding.ActivityConfirmacionSeparacionBinding;
import com.example.bitbusters.utils.AsesorNotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrato de {@code separaciones} y notificaciones alineado con
 * INTEGRACION_FINAL_ADMIN_CLIENTE_ASESOR_SUPERADMIN.md (rama noha/branch-3,
 * pendiente de merge) — no con la convención lowercase de {@code citas}.
 */
public class ConfirmacionSeparacionActivity extends AppCompatActivity {

    private static final String TAG = "Separacion";

    public static final String EXTRA_CLIENTE     = "extra_cliente";
    public static final String EXTRA_PROYECTO    = "extra_proyecto";
    public static final String EXTRA_MONTO       = "extra_monto";
    public static final String EXTRA_FECHA       = "extra_fecha";
    public static final String EXTRA_HORA        = "extra_hora";
    public static final String EXTRA_METODO_PAGO = "extra_metodo_pago";
    public static final String EXTRA_CITA_ID     = "extra_cita_id";
    public static final String EXTRA_UID_CLIENTE = "extra_uid_cliente";
    public static final String EXTRA_PROYECTO_ID = "extra_proyecto_id";

    private ActivityConfirmacionSeparacionBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfirmacionSeparacionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String cliente     = getIntent().getStringExtra(EXTRA_CLIENTE);
        String proyecto    = getIntent().getStringExtra(EXTRA_PROYECTO);
        String monto       = getIntent().getStringExtra(EXTRA_MONTO);
        String fecha       = getIntent().getStringExtra(EXTRA_FECHA);
        String hora        = getIntent().getStringExtra(EXTRA_HORA);
        String metodoPago  = getIntent().getStringExtra(EXTRA_METODO_PAGO);
        String citaId       = getIntent().getStringExtra(EXTRA_CITA_ID);
        String uidCliente   = getIntent().getStringExtra(EXTRA_UID_CLIENTE);
        String proyectoId   = getIntent().getStringExtra(EXTRA_PROYECTO_ID);

        String clienteDisplay  = (cliente  != null && !cliente.isEmpty())  ? cliente  : "el cliente";
        String proyectoDisplay = (proyecto != null && !proyecto.isEmpty()) ? proyecto : "el proyecto";

        // Notificación local del sistema
        AsesorNotificationHelper.showNuevaSeparacion(this, clienteDisplay, proyectoDisplay);

        // Guardar en Firestore
        guardarSeparacionEnFirestore(cliente, proyecto, monto, fecha, hora, metodoPago,
                citaId, uidCliente, proyectoId);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(this, AsesorHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }

    private void guardarSeparacionEnFirestore(String cliente, String proyecto,
                                               String montoStr, String fecha,
                                               String hora, String metodoPago,
                                               String citaId, String uidCliente, String proyectoId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uidAsesor  = (user != null) ? user.getUid() : "anonimo";

        double monto;
        try {
            monto = montoStr != null ? Double.parseDouble(montoStr.trim()) : 0.0;
        } catch (NumberFormatException e) {
            monto = 0.0;
        }

        String clienteNombre  = cliente  != null ? cliente  : "";
        String proyectoNombre = proyecto != null ? proyecto : "";
        String uidClienteVal  = uidCliente != null ? uidCliente : "";
        double montoFinal = monto;

        Map<String, Object> data = new HashMap<>();
        // Cliente — campos + alias que exige el contrato del equipo
        data.put("uidCliente",     uidClienteVal);
        data.put("clienteUid",     uidClienteVal);
        data.put("cliente",        clienteNombre);
        data.put("nombreCliente",  clienteNombre);
        // Asesor
        data.put("uidAsesor",      uidAsesor);
        // Proyecto
        data.put("proyectoId",     proyectoId != null ? proyectoId : "");
        data.put("proyecto",       proyectoNombre);
        data.put("proyectoNombre", proyectoNombre);
        // Otros datos de la cita/pago
        data.put("citaId",         citaId     != null ? citaId     : "");
        data.put("monto",          monto);
        data.put("fecha",          fecha      != null ? fecha      : "");
        data.put("hora",           hora       != null ? hora       : "");
        data.put("metodoPago",     metodoPago != null ? metodoPago : "Tarjeta");
        // Estados oficiales (mayúscula) — ver INTEGRACION_FINAL_ADMIN_CLIENTE_ASESOR_SUPERADMIN.md § 2-3
        data.put("estado",         "Pendiente");
        data.put("estadoPago",     "No habilitado");
        data.put("createdAt",      FieldValue.serverTimestamp());
        data.put("updatedAt",      FieldValue.serverTimestamp());

        if (proyectoId == null || proyectoId.isEmpty()) {
            db.collection("separaciones").add(data)
                .addOnSuccessListener(ref -> Log.d(TAG, "Guardada con ID: " + ref.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Error al guardar: " + e.getMessage()));
            return;
        }

        // Resolver inmobiliariaId desde el proyecto — obligatorio para que Admin filtre bien.
        db.collection("proyectos").document(proyectoId).get()
            .addOnSuccessListener(proyectoDoc -> {
                String inmobiliariaId = proyectoDoc.getString("inmobiliariaId");
                data.put("inmobiliariaId", inmobiliariaId != null ? inmobiliariaId : "");

                db.collection("separaciones").add(data)
                    .addOnSuccessListener(ref -> {
                        Log.d(TAG, "Guardada con ID: " + ref.getId());
                        if (inmobiliariaId != null && !inmobiliariaId.isEmpty()) {
                            notificarAdmins(inmobiliariaId, ref, clienteNombre, proyectoNombre,
                                    uidClienteVal, uidAsesor, montoFinal);
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error al guardar: " + e.getMessage()));
            })
            .addOnFailureListener(e -> {
                // Sin inmobiliariaId no podemos notificar al Admin, pero igual guardamos la separación.
                db.collection("separaciones").add(data)
                    .addOnSuccessListener(ref -> Log.d(TAG, "Guardada con ID: " + ref.getId()))
                    .addOnFailureListener(ex -> Log.e(TAG, "Error al guardar: " + ex.getMessage()));
            });
    }

    /**
     * Notifica a los admins de la inmobiliaria con el esquema plano oficial
     * (notifications/{id} + targetUid) — ver INTEGRACION_FINAL... § 11.
     */
    private void notificarAdmins(String inmobiliariaId, DocumentReference separacionRef,
                                  String clienteNombre, String proyectoNombre,
                                  String uidCliente, String uidAsesor, double monto) {
        db.collection("users")
            .whereEqualTo("role", "admin")
            .whereEqualTo("inmobiliariaId", inmobiliariaId)
            .get()
            .addOnSuccessListener(admins -> {
                for (QueryDocumentSnapshot adminDoc : admins) {
                    Map<String, Object> notif = new HashMap<>();
                    notif.put("role",       "admin");
                    notif.put("targetRole", "admin");
                    notif.put("targetUid",  adminDoc.getId());
                    notif.put("type",       "separacion_registrada");
                    notif.put("tipo",       "separacion_registrada");
                    notif.put("title",      "Nueva separación registrada");
                    notif.put("descripcion", clienteNombre + " registró una separación en " + proyectoNombre + ".");
                    notif.put("senderName", clienteNombre);
                    notif.put("createdAt",  FieldValue.serverTimestamp());
                    notif.put("order",      System.currentTimeMillis());
                    notif.put("read",       false);
                    notif.put("isOld",      false);

                    notif.put("separacionId",   separacionRef.getId());
                    notif.put("proyectoNombre", proyectoNombre);
                    notif.put("uidCliente",     uidCliente);
                    notif.put("clienteUid",     uidCliente);
                    notif.put("clienteNombre",  clienteNombre);
                    notif.put("uidAsesor",      uidAsesor);
                    notif.put("inmobiliariaId", inmobiliariaId);
                    notif.put("montoSeparacion", monto);

                    db.collection("notifications").add(notif);
                }
            })
            .addOnFailureListener(e -> Log.w(TAG, "No se pudo notificar a los admins: " + e.getMessage()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
