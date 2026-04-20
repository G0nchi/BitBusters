package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;

public class PagoSeparacionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago_separacion);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_confirmar).setOnClickListener(v ->
            startActivity(new Intent(this, ConfirmacionSeparacionActivity.class)));
    }
}
