package com.example.bitbusters.activities.asesor;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;

public class ValorarVisitaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valorar_visita);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_guardar).setOnClickListener(v -> {
            Toast.makeText(this, "¡Valoración guardada!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
