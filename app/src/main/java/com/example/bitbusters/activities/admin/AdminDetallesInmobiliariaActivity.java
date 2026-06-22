package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.AdminAsesorInmobiliariaAdapter;
import com.example.bitbusters.data.AdminDataRepository;

public class AdminDetallesInmobiliariaActivity extends AppCompatActivity {

    private RecyclerView rvAsesores;
    private AdminAsesorInmobiliariaAdapter adapter;
    private Button btnVerTodosAsesoresDetalles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_detalles_inmobiliaria);
        setupNavigationListeners();
        setupRecyclerView();
    }

    private void setupNavigationListeners() {
        // Back button in header
        ImageButton btnBackImmob = findViewById(R.id.btnBackImmob);
        if (btnBackImmob != null) {
            btnBackImmob.setOnClickListener(v -> finish());
        }

        // Edit button in header (pencil icon)
        ImageButton btnEditImmobHeader = findViewById(R.id.btnEditImmobHeader);
        if (btnEditImmobHeader != null) {
            btnEditImmobHeader.setOnClickListener(v -> {
                startActivity(new Intent(AdminDetallesInmobiliariaActivity.this, AdminEditarInmobiliariaActivity.class));
            });
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
        if (rvAsesores != null) {
            rvAsesores.setLayoutManager(new LinearLayoutManager(this));
            int total = AdminDataRepository.getAsesoresInmobiliaria().size();
            adapter = new AdminAsesorInmobiliariaAdapter(
                AdminDataRepository.getUltimosAsesoresInmobiliaria(4),
                null,
                false
            );
            rvAsesores.setAdapter(adapter);

            if (btnVerTodosAsesoresDetalles != null) {
                btnVerTodosAsesoresDetalles.setVisibility(total > 4 ? View.VISIBLE : View.GONE);
            }
        }
    }
}
