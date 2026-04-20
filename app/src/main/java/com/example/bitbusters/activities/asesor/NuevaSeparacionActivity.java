package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;

public class NuevaSeparacionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_separacion);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_continuar).setOnClickListener(v ->
            startActivity(new Intent(this, PagoSeparacionActivity.class)));
    }
}
