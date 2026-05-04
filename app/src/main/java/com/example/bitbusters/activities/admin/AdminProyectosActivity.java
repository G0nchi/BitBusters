package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.example.bitbusters.R;
import com.google.android.material.card.MaterialCardView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.adapters.AdminProyectoAdapter;
import com.example.bitbusters.models.Proyecto;
import com.example.bitbusters.data.ProjectSessionData;

public class AdminProyectosActivity extends AdminMainActivity {

    private RecyclerView rvProyectos;
    private AdminProyectoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_proyectos);
        setupHeaderListeners();
        setupBottomNavigation(R.id.nav_proyectos);
        setupCreateProjectButton();
        setupRecyclerView();
    }

    private void setupCreateProjectButton() {
        Button btnCreateProject = findViewById(R.id.btnCreateProject);
        if (btnCreateProject != null) {
            btnCreateProject.setOnClickListener(v -> {
                startActivity(new Intent(AdminProyectosActivity.this, AdminCrearProyectoActivity.class));
            });
        }
    }

    private void setupRecyclerView() {
        rvProyectos = findViewById(R.id.rvProyectos);
        if (rvProyectos != null) {
            rvProyectos.setLayoutManager(new LinearLayoutManager(this));
            adapter = new AdminProyectoAdapter(getProyectosList(), proyecto -> {
                startActivity(new Intent(AdminProyectosActivity.this, AdminDetallesProyectoActivity.class));
            });
            rvProyectos.setAdapter(adapter);
        }
    }

    private java.util.List<Proyecto> getProyectosList() {
        return ProjectSessionData.getProyectos();
    }
}
