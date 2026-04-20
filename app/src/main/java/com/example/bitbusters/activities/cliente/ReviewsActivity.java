package com.example.bitbusters.activities.cliente;


import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;

public class ReviewsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        // Recibir nombre del proyecto desde ProjectDetailActivity
        String nombreProyecto = getIntent().getStringExtra("proyecto");
        if (nombreProyecto != null) {
            ((TextView) findViewById(R.id.tvNombreProyecto)).setText(nombreProyecto);
        }

        // Botón volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Click en cards de review (por si se quiere expandir o dar like)
        findViewById(R.id.cardReview1).setOnClickListener(v -> { });
        findViewById(R.id.cardReview2).setOnClickListener(v -> { });
        findViewById(R.id.cardReview3).setOnClickListener(v -> { });
        findViewById(R.id.cardReview4).setOnClickListener(v -> { });

        // TODO Lab 6: cargar reviews desde Firebase
        // DatabaseReference ref = FirebaseDatabase.getInstance()
        //     .getReference("reviews").child(proyectoId);
        // ref.addValueEventListener(...);
    }
}