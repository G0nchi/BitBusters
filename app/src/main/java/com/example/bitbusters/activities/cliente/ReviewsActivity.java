package com.example.bitbusters.activities.cliente;


import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.ClientReviewsAdapter;
import com.example.bitbusters.data.ClientDataRepository;

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

        RecyclerView recyclerView = findViewById(R.id.recyclerViewReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ClientReviewsAdapter adapter = new ClientReviewsAdapter();
        adapter.submitList(ClientDataRepository.getReviews());
        recyclerView.setAdapter(adapter);
    }
}