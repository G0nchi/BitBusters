package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
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
    }

    private void setupRecyclerView() {
        rvAsesores = findViewById(R.id.rvAsesoresDetalles);
        if (rvAsesores != null) {
            rvAsesores.setLayoutManager(new LinearLayoutManager(this));
            adapter = new AdminAsesorInmobiliariaAdapter(
                AdminDataRepository.getAsesoresInmobiliaria(),
                new AdminAsesorInmobiliariaAdapter.OnAsesorActionListener() {
                    @Override
                    public void onEditAsesor(int position) {
                        // Navigate to edit activity if needed
                    }

                    @Override
                    public void onDeleteAsesor(int position) {
                        // Delete not available in details view
                    }
                }
            );
            rvAsesores.setAdapter(adapter);
        }
    }
}
