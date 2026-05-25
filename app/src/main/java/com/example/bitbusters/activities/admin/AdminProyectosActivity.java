package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.AdminProyectoListAdapter;
import com.example.bitbusters.data.AdminProyectosRepository;
import com.example.bitbusters.models.AdminProyecto;

/**
 * Lista de proyectos del Administrador.
 * Lee de AdminProyectosRepository (fuente única de verdad) en onResume()
 * para reflejar siempre los proyectos más recientes.
 */
public class AdminProyectosActivity extends AdminMainActivity {

    private RecyclerView          rvProyectos;
    private AdminProyectoListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_proyectos);
        setupHeaderListeners();
        setupBottomNavigation(R.id.nav_proyectos);
        setupCreateProjectButton();
        setupRecyclerView();
    }

    /**
     * Se llama cada vez que la Activity vuelve al frente (ej: al regresar de
     * AdminCrearProyectoActivity). Refresca la lista con los datos más actuales.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.setData(AdminProyectosRepository.getTodos());
        }
    }

    private void setupCreateProjectButton() {
        Button btnCreateProject = findViewById(R.id.btnCreateProject);
        if (btnCreateProject != null) {
            btnCreateProject.setOnClickListener(v ->
                    startActivity(new Intent(AdminProyectosActivity.this,
                            AdminCrearProyectoActivity.class))
            );
        }
    }

    private void setupRecyclerView() {
        rvProyectos = findViewById(R.id.rvProyectos);
        if (rvProyectos == null) return;

        rvProyectos.setLayoutManager(new LinearLayoutManager(this));

        // Usar AdminProyectoListAdapter con datos del repositorio
        adapter = new AdminProyectoListAdapter(
                AdminProyectosRepository.getTodos(),
                proyecto -> {
                    // Al tocar un proyecto → abrir detalle con su ID
                    Intent intent = new Intent(AdminProyectosActivity.this,
                            AdminDetallesProyectoActivity.class);
                    intent.putExtra("proyecto_id", proyecto.getId());
                    startActivity(intent);
                }
        );
        rvProyectos.setAdapter(adapter);
    }
}
