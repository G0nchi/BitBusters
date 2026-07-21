package com.example.bitbusters.activities.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bitbusters.utils.ImmersiveMode;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;
import com.example.bitbusters.utils.LogHelper;
import com.example.bitbusters.utils.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SuperadminApprovalEvaluationActivity extends AppCompatActivity {

    private String asesorUid;
    private String asesorNombre;
    private String asesorCompany;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_superadmin_approval_evaluation);

        NotificationHelper.crearCanal(this);

        bindInsets();

        asesorUid = getIntent().getStringExtra("uid");
        populateViews();
        setupClicks();
    }

    private void bindInsets() {
        View root = findViewById(R.id.main);
        if (root == null) return;
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void populateViews() {
        Intent intent = getIntent();
        String initials = intent.getStringExtra("initials");
        String nombre   = intent.getStringExtra("nombre");
        String email    = intent.getStringExtra("email");
        String company  = intent.getStringExtra("company");
        asesorNombre = nombre;
        asesorCompany = company;

        TextView tvInitials = findViewById(R.id.asesorInitials);
        TextView tvName     = findViewById(R.id.asesorName);
        TextView tvEmail    = findViewById(R.id.asesorEmail);
        TextView tvCompany  = findViewById(R.id.asesorCompany);

        if (tvInitials != null && initials != null) tvInitials.setText(initials);
        if (tvName     != null && nombre   != null) tvName.setText(nombre);
        if (tvEmail    != null && email    != null) tvEmail.setText(email);
        if (tvCompany  != null && company  != null) tvCompany.setText(company);
    }

    private void setupClicks() {
        View backButton    = findViewById(R.id.backButton);
        View rejectButton  = findViewById(R.id.rejectButton);
        View approveButton = findViewById(R.id.approveButton);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        if (rejectButton != null) {
            rejectButton.setOnClickListener(v -> updateStatus("inactive", false));
        }
        if (approveButton != null) {
            approveButton.setOnClickListener(v -> updateStatus("active", true));
        }
    }

    private void updateStatus(String newStatus, boolean approved) {
        if (asesorUid == null || asesorUid.isEmpty()) {
            finish();
            return;
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(asesorUid)
            .update("status", newStatus)
            .addOnSuccessListener(aVoid -> {
                writeNotificationToFirestore(approved);
                logHabilitacionAsesor(approved);

                Intent destino = new Intent(this, SuperadminApprovalsActivity.class);
                if (approved) {
                    Toast.makeText(this, getString(R.string.sa_approve), Toast.LENGTH_SHORT).show();
                    NotificationHelper.lanzarNotificacion(
                        this,
                        "Solicitud aprobada",
                        "La solicitud de aprobación fue aceptada correctamente",
                        NotificationHelper.NOTIF_APROBACION_ACEPTADA,
                        destino
                    );
                } else {
                    Toast.makeText(this, getString(R.string.sa_reject), Toast.LENGTH_SHORT).show();
                    NotificationHelper.lanzarNotificacion(
                        this,
                        "Solicitud rechazada",
                        "La solicitud de aprobación fue rechazada",
                        NotificationHelper.NOTIF_APROBACION_RECHAZADA,
                        destino
                    );
                }
                finish();
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Error al actualizar. Intente de nuevo.", Toast.LENGTH_SHORT).show()
            );
    }

    private void logHabilitacionAsesor(boolean approved) {
        FirebaseUser superadmin = FirebaseAuth.getInstance().getCurrentUser();
        String superadminEmail = (superadmin != null && superadmin.getEmail() != null)
                ? superadmin.getEmail() : "superadmin";
        String nombreLog = asesorNombre != null ? asesorNombre : asesorUid;
        String companyLog = asesorCompany != null ? asesorCompany : "";

        LogHelper.logEvent(
                LogHelper.TYPE_ENABLEMENT,
                approved ? LogHelper.STATUS_CONFIRMED : LogHelper.STATUS_FAILED,
                approved ? "Asesor habilitado por superadmin" : "Solicitud de asesor rechazada por superadmin",
                superadminEmail,
                approved ? "Cambio de estado a habilitado" : "Cambio de estado a rechazado",
                "Detalle del Cambio",
                "Asesor " + nombreLog + ": Pendiente -> " + (approved ? "Activo" : "Rechazado"),
                "Empresa: " + companyLog,
                Arrays.asList("modulo:habilitaciones"));
    }

    private void writeNotificationToFirestore(boolean approved) {
        Map<String, Object> notif = new HashMap<>();
        notif.put("title",     approved ? "Solicitud aprobada" : "Solicitud rechazada");
        notif.put("body",      approved
                ? "Tu solicitud de asesor fue aprobada. Ya puedes acceder a la plataforma."
                : "Tu solicitud de asesor fue rechazada. Contacta al soporte para más información.");
        notif.put("type",      approved ? "approval_accepted" : "approval_rejected");
        notif.put("timestamp", FieldValue.serverTimestamp());
        notif.put("read",      false);

        FirebaseFirestore.getInstance()
                .collection("notifications")
                .document(asesorUid)
                .collection("items")
                .add(notif);
    }
}
