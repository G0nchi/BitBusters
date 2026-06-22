package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.databinding.ActivityConfirmacionSeparacionBinding;
import com.example.bitbusters.utils.AsesorNotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ConfirmacionSeparacionActivity extends AppCompatActivity {

    public static final String EXTRA_CLIENTE     = "extra_cliente";
    public static final String EXTRA_PROYECTO    = "extra_proyecto";
    public static final String EXTRA_MONTO       = "extra_monto";
    public static final String EXTRA_FECHA       = "extra_fecha";
    public static final String EXTRA_HORA        = "extra_hora";
    public static final String EXTRA_METODO_PAGO = "extra_metodo_pago";

    private ActivityConfirmacionSeparacionBinding binding;

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

        String clienteDisplay  = (cliente  != null && !cliente.isEmpty())  ? cliente  : "el cliente";
        String proyectoDisplay = (proyecto != null && !proyecto.isEmpty()) ? proyecto : "el proyecto";

        // Notificación local del sistema
        AsesorNotificationHelper.showNuevaSeparacion(this, clienteDisplay, proyectoDisplay);

        // Guardar en Firestore
        guardarSeparacionEnFirestore(cliente, proyecto, monto, fecha, hora, metodoPago);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(this, AsesorHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }

    private void guardarSeparacionEnFirestore(String cliente, String proyecto,
                                               String monto, String fecha,
                                               String hora, String metodoPago) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uidAsesor  = (user != null) ? user.getUid() : "anonimo";

        Map<String, Object> data = new HashMap<>();
        data.put("uidAsesor",    uidAsesor);
        data.put("cliente",      cliente   != null ? cliente   : "");
        data.put("proyecto",     proyecto  != null ? proyecto  : "");
        data.put("monto",        monto     != null ? monto     : "0");
        data.put("fecha",        fecha     != null ? fecha     : "");
        data.put("hora",         hora      != null ? hora      : "");
        data.put("metodoPago",   metodoPago != null ? metodoPago : "Tarjeta");
        data.put("estado",       "Pendiente");
        data.put("timestamp",    FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
            .collection("separaciones")
            .add(data)
            .addOnSuccessListener(ref ->
                Log.d("Separacion", "Guardada con ID: " + ref.getId()))
            .addOnFailureListener(e ->
                Log.e("Separacion", "Error al guardar: " + e.getMessage()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
