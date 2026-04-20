package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.example.bitbusters.R;
import com.google.android.material.card.MaterialCardView;

public class AdminProyectosActivity extends AdminMainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_proyectos);
        setupHeaderListeners();
        setupBottomNavigation(R.id.nav_proyectos);
        setupProjectCardListeners();
        setupCreateProjectButton();
    }

    private void setupCreateProjectButton() {
        Button btnCreateProject = findViewById(R.id.btnCreateProject);
        if (btnCreateProject != null) {
            btnCreateProject.setOnClickListener(v -> {
                startActivity(new Intent(AdminProyectosActivity.this, AdminCrearProyectoActivity.class));
            });
        }
    }

    private void setupProjectCardListeners() {
        // Project cards - add click listeners to navigate to project details
        MaterialCardView projectCard1 = findViewById(R.id.projectCard1);
        MaterialCardView projectCard2 = findViewById(R.id.projectCard2);
        MaterialCardView projectCard3 = findViewById(R.id.projectCard3);
        MaterialCardView projectCard4 = findViewById(R.id.projectCard4);

        View.OnClickListener projectDetailsListener = v -> {
            startActivity(new Intent(AdminProyectosActivity.this, AdminDetallesProyectoActivity.class));
        };

        if (projectCard1 != null) projectCard1.setOnClickListener(projectDetailsListener);
        if (projectCard2 != null) projectCard2.setOnClickListener(projectDetailsListener);
        if (projectCard3 != null) projectCard3.setOnClickListener(projectDetailsListener);
        if (projectCard4 != null) projectCard4.setOnClickListener(projectDetailsListener);
    }
}
