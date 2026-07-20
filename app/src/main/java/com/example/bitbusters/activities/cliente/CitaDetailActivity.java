package com.example.bitbusters.activities.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bitbusters.R;
import com.example.bitbusters.models.ClientAppointment;

/**
 * Detalle de una cita. Recibe todos los datos ya formateados desde MisCitasActivity.
 * El botón "Reagendar" solo aparece si estado == PENDING o CONFIRMED.
 * Al reagendar se abre AgendaCitaActivity en modo reagendar; al volver,
 * MisCitasActivity actualiza la lista automáticamente vía el listener de Firestore.
 */
public class CitaDetailActivity extends AppCompatActivity {

    static final String EXTRA_FIRESTORE_ID    = "firestoreId";
    static final String EXTRA_SLOT_ID         = "slotId";
    static final String EXTRA_PROYECTO_ID     = "proyectoId";
    static final String EXTRA_PROYECTO_NOMBRE = "proyectoNombre";
    static final String EXTRA_UID_ASESOR      = "uidAsesor";
    static final String EXTRA_FECHA           = "fecha";
    static final String EXTRA_HORA            = "hora";
    static final String EXTRA_ESTADO          = "estado";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cita_detail);

        Intent i = getIntent();
        String firestoreId    = i.getStringExtra(EXTRA_FIRESTORE_ID);
        String slotId         = i.getStringExtra(EXTRA_SLOT_ID);
        String proyectoId     = i.getStringExtra(EXTRA_PROYECTO_ID);
        String proyectoNombre = i.getStringExtra(EXTRA_PROYECTO_NOMBRE);
        String uidAsesor      = i.getStringExtra(EXTRA_UID_ASESOR);
        String fecha          = i.getStringExtra(EXTRA_FECHA);
        String hora           = i.getStringExtra(EXTRA_HORA);
        String estado         = i.getStringExtra(EXTRA_ESTADO);

        // Poblar vistas
        setText(R.id.tvDetallProyecto, proyectoNombre);
        setText(R.id.tvDetallFecha,    fecha);
        setText(R.id.tvDetallHora,     hora);
        setText(R.id.tvDetallEstado,   estado);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Reagendar: solo para citas activas
        View btnReagendar = findViewById(R.id.btnReagendar);
        boolean reagendable = ClientAppointment.STATUS_PENDING.equals(estado)
                || ClientAppointment.STATUS_CONFIRMED.equals(estado);
        btnReagendar.setVisibility(reagendable ? View.VISIBLE : View.GONE);

        btnReagendar.setOnClickListener(v -> {
            Intent reagendarIntent = new Intent(this, AgendaCitaActivity.class);
            reagendarIntent.putExtra(AgendaCitaActivity.EXTRA_PROYECTO,         proyectoNombre);
            reagendarIntent.putExtra(AgendaCitaActivity.EXTRA_PROYECTO_ID,      proyectoId);
            reagendarIntent.putExtra(AgendaCitaActivity.EXTRA_UID_ASESOR,       uidAsesor);
            reagendarIntent.putExtra(AgendaCitaActivity.EXTRA_MODO,             "reagendar");
            reagendarIntent.putExtra(AgendaCitaActivity.EXTRA_CITA_ID,          firestoreId);
            reagendarIntent.putExtra(AgendaCitaActivity.EXTRA_SLOT_ID_ANTERIOR, slotId);
            startActivity(reagendarIntent);
        });
    }

    private void setText(int viewId, String value) {
        TextView tv = findViewById(viewId);
        if (tv != null) tv.setText(value != null ? value : "—");
    }
}
