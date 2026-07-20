package com.example.bitbusters.activities.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.ClienteInmobiliariasAdapter;
import com.example.bitbusters.models.ClienteInmobiliaria;
import com.example.bitbusters.models.Proyecto;
import com.example.bitbusters.repository.ProyectoRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClienteInmobiliariasActivity extends AppCompatActivity {

    private ProgressBar progress;
    private TextView tvEmpty;
    private RecyclerView rvInmobiliarias;
    private ClienteInmobiliariasAdapter adapter;
    private ListenerRegistration listener;
    private final ProyectoRepository proyectoRepository = new ProyectoRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_inmobiliarias);

        progress = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvInmobiliarias = findViewById(R.id.rvInmobiliarias);
        adapter = new ClienteInmobiliariasAdapter(this::abrirProyectosInmobiliaria);
        rvInmobiliarias.setLayoutManager(new LinearLayoutManager(this));
        rvInmobiliarias.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    protected void onStart() {
        super.onStart();
        escucharInmobiliariasDesdeProyectos();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }

    private void escucharInmobiliariasDesdeProyectos() {
        mostrarCargando();
        listener = proyectoRepository.escucharProyectosCliente(new ProyectoRepository.ProyectosListener() {
            @Override
            public void onProyectosActualizados(List<Proyecto> proyectos) {
                List<ClienteInmobiliaria> inmobiliarias = agrupar(proyectos);
                adapter.setData(inmobiliarias);
                if (inmobiliarias.isEmpty()) {
                    mostrarVacio();
                } else {
                    mostrarContenido();
                }
            }

            @Override
            public void onError(String mensaje) {
                tvEmpty.setText("No se pudieron cargar inmobiliarias: " + mensaje);
                mostrarVacio();
            }
        });
    }

    private List<ClienteInmobiliaria> agrupar(List<Proyecto> proyectos) {
        Map<String, Integer> counts = new HashMap<>();
        Map<String, String> nombres = new HashMap<>();
        Map<String, Set<String>> distritos = new HashMap<>();

        if (proyectos != null) {
            for (Proyecto proyecto : proyectos) {
                String id = firstNonEmpty(proyecto.getInmobiliariaId(), proyecto.getAdminUid(), "sin_inmobiliaria");
                String nombre = firstNonEmpty(proyecto.getInmobiliariaNombre(), "Inmobiliaria no registrada", "Inmobiliaria no registrada");
                counts.put(id, counts.containsKey(id) ? counts.get(id) + 1 : 1);
                nombres.put(id, nombre);
                Set<String> set = distritos.containsKey(id) ? distritos.get(id) : new LinkedHashSet<>();
                if (proyecto.getUbicacion() != null && !proyecto.getUbicacion().trim().isEmpty()) {
                    set.add(proyecto.getUbicacion());
                }
                distritos.put(id, set);
            }
        }

        List<ClienteInmobiliaria> result = new ArrayList<>();
        for (String id : counts.keySet()) {
            int total = counts.get(id);
            String ubicaciones = String.join(", ", distritos.get(id));
            String descripcion = total + (total == 1 ? " proyecto activo" : " proyectos activos");
            if (!ubicaciones.trim().isEmpty()) descripcion += " · " + ubicaciones;
            result.add(new ClienteInmobiliaria(id, nombres.get(id), descripcion, total));
        }
        result.sort((a, b) -> Integer.compare(b.getTotalProyectos(), a.getTotalProyectos()));
        return result;
    }

    private void abrirProyectosInmobiliaria(ClienteInmobiliaria inmobiliaria) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("mostrar_todos", true);
        intent.putExtra("inmobiliaria_id", inmobiliaria.getId());
        intent.putExtra("inmobiliaria_nombre", inmobiliaria.getNombre());
        startActivity(intent);
    }

    private void mostrarCargando() {
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvInmobiliarias.setVisibility(View.GONE);
    }

    private void mostrarContenido() {
        progress.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        rvInmobiliarias.setVisibility(View.VISIBLE);
    }

    private void mostrarVacio() {
        progress.setVisibility(View.GONE);
        rvInmobiliarias.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    private String firstNonEmpty(String primary, String secondary, String fallback) {
        if (primary != null && !primary.trim().isEmpty()) return primary;
        if (secondary != null && !secondary.trim().isEmpty()) return secondary;
        return fallback;
    }
}
