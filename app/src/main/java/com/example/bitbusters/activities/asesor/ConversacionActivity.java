package com.example.bitbusters.activities.asesor;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bitbusters.R;
import java.util.ArrayList;
import java.util.List;

public class ConversacionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversacion);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        setupMessages();
    }

    private void setupMessages() {
        List<MensajeAdapter.Mensaje> msgs = new ArrayList<>();
        msgs.add(new MensajeAdapter.Mensaje(
            "Buenos días Juan, quería confirmar si la cita del lunes sigue en pie.", "9:45 AM", false));
        msgs.add(new MensajeAdapter.Mensaje(
            "¡Buenos días Carlos! Sí, todo confirmado para el lunes 7 a las 10:30 AM.", "9:48 AM", true));
        msgs.add(new MensajeAdapter.Mensaje(
            "Perfecto. ¿El departamento 302 sigue disponible?", "9:50 AM", false));
        msgs.add(new MensajeAdapter.Mensaje(
            "Sí, está disponible. Te comparto el detalle de la cita 👇", "9:51 AM", true));
        msgs.add(new MensajeAdapter.Mensaje(
            "¡Excelente! Ahí estaré puntual 🤝", "10:02 AM", false));
        msgs.add(new MensajeAdapter.Mensaje(
            "¿Podemos confirmar la cita del lunes?", "10:32 AM", false));

        RecyclerView rv = findViewById(R.id.rv_mensajes);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rv.setLayoutManager(lm);
        rv.setAdapter(new MensajeAdapter(msgs));
    }
}
