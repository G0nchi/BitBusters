package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.AdminAsesorInmobiliariaAdapter;
import com.example.bitbusters.data.FirestoreAsesoresRepository;
import com.example.bitbusters.models.AdminAsesorInmobiliaria;
import com.example.bitbusters.utils.AdminPreferencesManager;

import java.util.ArrayList;
import java.util.List;

public class AdminDetallesInmobiliariaActivity extends AppCompatActivity {

    private RecyclerView rvAsesores;
    private AdminAsesorInmobiliariaAdapter adapter;
    private Button btnVerTodosAsesoresDetalles;
    private TextView tvAsesoresEmptyDetalles;
    private final FirestoreAsesoresRepository asesoresRepository = new FirestoreAsesoresRepository();
    private final List<AdminAsesorInmobiliaria> asesoresActuales = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_detalles_inmobiliaria);
        cargarDatosInmobiliaria();
        setupNavigationListeners();
        setupRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        cargarDatosInmobiliaria();
        cargarAsesoresDesdeFirestore();
    }

    private void cargarDatosInmobiliaria() {
        String inmobiliaria = AdminPreferencesManager.obtenerInmobiliaria(this);

        TextView tvNombre = findViewById(R.id.tvNombreInmobiliariaDetalles);
        if (tvNombre != null) {
            tvNombre.setText(inmobiliaria);
        }

        TextView tvRazonSocial = findViewById(R.id.tvRazonSocialDetalles);
        if (tvRazonSocial != null) {
            tvRazonSocial.setText(inmobiliaria);
        }
    }

    private void setupNavigationListeners() {
        ImageButton btnBackImmob = findViewById(R.id.btnBackImmob);
        if (btnBackImmob != null) {
            btnBackImmob.setOnClickListener(v -> finish());
        }

        ImageButton btnEditImmobHeader = findViewById(R.id.btnEditImmobHeader);
        if (btnEditImmobHeader != null) {
            btnEditImmobHeader.setOnClickListener(v ->
                    startActivity(new Intent(AdminDetallesInmobiliariaActivity.this,
                            AdminEditarInmobiliariaActivity.class)));
        }

        btnVerTodosAsesoresDetalles = findViewById(R.id.btnVerTodosAsesoresDetalles);
        if (btnVerTodosAsesoresDetalles != null) {
            btnVerTodosAsesoresDetalles.setOnClickListener(v ->
                    startActivity(new Intent(AdminDetallesInmobiliariaActivity.this,
                            AdminListaAsesoresInmobiliariaActivity.class)));
        }
    }

    private void setupRecyclerView() {
        rvAsesores = findViewById(R.id.rvAsesoresDetalles);
        tvAsesoresEmptyDetalles = findViewById(R.id.tvAsesoresEmptyDetalles);
        if (rvAsesores != null) {
            rvAsesores.setLayoutManager(new LinearLayoutManager(this));
            adapter = new AdminAsesorInmobiliariaAdapter(new ArrayList<>(), null, false);
            rvAsesores.setAdapter(adapter);
        }
    }

    private void cargarAsesoresDesdeFirestore() {
        asesoresRepository.obtenerAsesoresRegistrados(this, new FirestoreAsesoresRepository.AsesoresCallback() {
            @Override
            public void onSuccess(List<AdminAsesorInmobiliaria> asesores) {
                asesoresActuales.clear();
                if (asesores != null) {
                    asesoresActuales.addAll(asesores);
                }
                renderAsesores();
            }

            @Override
            public void onError(String mensaje) {
                asesoresActuales.clear();
                renderAsesores();
            }
        });
    }

    private void renderAsesores() {
        boolean hayAsesores = !asesoresActuales.isEmpty();
        List<AdminAsesorInmobiliaria> recientes = hayAsesores
                ? new ArrayList<>(asesoresActuales.subList(0, Math.min(4, asesoresActuales.size())))
                : new ArrayList<>();

        if (adapter != null) {
            adapter.setData(recientes);
        }

        if (rvAsesores != null) {
            rvAsesores.setVisibility(hayAsesores ? View.VISIBLE : View.GONE);
        }
        if (tvAsesoresEmptyDetalles != null) {
            tvAsesoresEmptyDetalles.setVisibility(hayAsesores ? View.GONE : View.VISIBLE);
        }
        if (btnVerTodosAsesoresDetalles != null) {
            btnVerTodosAsesoresDetalles.setVisibility(asesoresActuales.size() > 4 ? View.VISIBLE : View.GONE);
        }
    }
}
