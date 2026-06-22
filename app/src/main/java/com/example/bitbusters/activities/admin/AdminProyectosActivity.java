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
import com.example.bitbusters.utils.AdminPreferencesManager;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * Lista de proyectos del Administrador.
 * Lee de AdminProyectosRepository (fuente única de verdad) en onResume()
 * para reflejar siempre los proyectos más recientes.
 */
public class AdminProyectosActivity extends AdminMainActivity {

    private RecyclerView          rvProyectos;
    private AdminProyectoListAdapter adapter;
    private ListenerRegistration proyectosListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_proyectos);
        // Cargar proyectos guardados en disco (reemplaza los demo si hay guardados)
        AdminProyectosRepository.cargar(this);
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
    protected void onStart() {
        super.onStart();
        iniciarListenerFirestore();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (proyectosListener != null) {
            proyectosListener.remove();
            proyectosListener = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.setData(AdminProyectosRepository.getTodos());
        }
    }

    private void iniciarListenerFirestore() {
        String inmobiliariaNombre = AdminPreferencesManager.obtenerInmobiliaria(this);
        String inmobiliariaId = AdminProyectosRepository.crearInmobiliariaId(inmobiliariaNombre);
        String adminUid = AdminProyectosRepository.obtenerAdminUidActual();

        proyectosListener = AdminProyectosRepository.escucharPorAdministrador(
                adminUid,
                inmobiliariaId,
                new AdminProyectosRepository.ProyectosListener() {
                    @Override
                    public void onProyectosActualizados(List<AdminProyecto> proyectos) {
                        if (adapter != null) adapter.setData(proyectos);
                    }

                    @Override
                    public void onError(String mensaje) {
                        // Mantener la lista local visible si Firestore falla.
                        if (adapter != null) adapter.setData(AdminProyectosRepository.getTodos());
                    }
                });
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
