package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.example.bitbusters.R;
import com.google.android.material.card.MaterialCardView;

public class AdminSeparacionesActivity extends AdminMainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_separaciones);
        setupBottomNavigation(R.id.nav_separaciones);
        setupSeparationCardListeners();
    }

    private void setupSeparationCardListeners() {
        // Separation cards - add click listeners to navigate to separation details
        MaterialCardView separationCard1 = findViewById(R.id.separationCard1);
        MaterialCardView separationCard2 = findViewById(R.id.separationCard2);
        MaterialCardView separationCard3 = findViewById(R.id.separationCard3);

        View.OnClickListener separationDetailsListener = v -> {
            startActivity(new Intent(AdminSeparacionesActivity.this, AdminDetallesSeparacionActivity.class));
        };

        if (separationCard1 != null) separationCard1.setOnClickListener(separationDetailsListener);
        if (separationCard2 != null) separationCard2.setOnClickListener(separationDetailsListener);
        if (separationCard3 != null) separationCard3.setOnClickListener(separationDetailsListener);
    }
}
