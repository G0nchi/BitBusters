package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.AdminAsesorInmobiliariaAdapter;
import com.example.bitbusters.data.AdminDataRepository;
import com.example.bitbusters.models.AdminAsesorInmobiliaria;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class AdminEditarInmobiliariaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private RecyclerView rvAsesores;
    private AdminAsesorInmobiliariaAdapter adapter;
    private GoogleMap mapaUbicacion;
    private Button btnVerTodosAsesoresEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_editar_inmobiliaria);
        setupNavigationListeners();
        setupRecyclerView();
        setupMap();
    }

    private void setupNavigationListeners() {
        // Back button in header
        ImageButton btnBackEdit = findViewById(R.id.btnBackEdit);
        if (btnBackEdit != null) {
            btnBackEdit.setOnClickListener(v -> finish());
        }

        // Save changes button
        Button btnSaveChanges = findViewById(R.id.btnSaveChanges);
        if (btnSaveChanges != null) {
            btnSaveChanges.setOnClickListener(v -> finish());
        }

        // Cancel button
        Button btnCancelEdit = findViewById(R.id.btnCancelEdit);
        if (btnCancelEdit != null) {
            btnCancelEdit.setOnClickListener(v -> finish());
        }

        // Add photo button - open gallery
        Button btnAddPhotoEdit = findViewById(R.id.btnAddPhotoEdit);
        if (btnAddPhotoEdit != null) {
            btnAddPhotoEdit.setOnClickListener(v -> {
                Intent pickImageIntent = new Intent(Intent.ACTION_PICK);
                pickImageIntent.setType("image/*");
                startActivity(pickImageIntent);
            });
        }

        // Add advisor button
        Button btnAddAdvisorEdit = findViewById(R.id.btnAddAdvisorEdit);
        if (btnAddAdvisorEdit != null) {
            btnAddAdvisorEdit.setOnClickListener(v -> {
                startActivity(new Intent(AdminEditarInmobiliariaActivity.this, AdminRegistrarAsesorActivity.class));
            });
        }

        btnVerTodosAsesoresEdit = findViewById(R.id.btnVerTodosAsesoresEdit);
        if (btnVerTodosAsesoresEdit != null) {
            btnVerTodosAsesoresEdit.setOnClickListener(v ->
                    startActivity(new Intent(AdminEditarInmobiliariaActivity.this,
                            AdminListaAsesoresInmobiliariaActivity.class)));
        }
    }

    private void setupRecyclerView() {
        rvAsesores = findViewById(R.id.rvAsesoresEdit);
        if (rvAsesores != null) {
            rvAsesores.setLayoutManager(new LinearLayoutManager(this));
            List<AdminAsesorInmobiliaria> recientes = AdminDataRepository.getUltimosAsesoresInmobiliaria(4);
            adapter = new AdminAsesorInmobiliariaAdapter(
                recientes,
                new AdminAsesorInmobiliariaAdapter.OnAsesorActionListener() {
                    @Override
                    public void onEditAsesor(int position) {
                        // TODO: Edit advisor
                    }

                    @Override
                    public void onDeleteAsesor(int position) {
                        adapter.removeItem(position);
                    }
                }
                ,
                false
            );
            rvAsesores.setAdapter(adapter);
        }

        boolean mostrarVerTodos = AdminDataRepository.getAsesoresInmobiliaria().size() > 4;
        if (btnVerTodosAsesoresEdit != null) {
            btnVerTodosAsesoresEdit.setVisibility(mostrarVerTodos ? View.VISIBLE : View.GONE);
        }
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapUbicacionInmobiliariaEdit);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            habilitarGestosMapaEnScroll(mapFragment);
        }
    }

    private void habilitarGestosMapaEnScroll(SupportMapFragment mapFragment) {
        View mapView = mapFragment.getView();
        if (mapView == null) {
            return;
        }
        mapView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                case android.view.MotionEvent.ACTION_MOVE:
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapaUbicacion = googleMap;
        mapaUbicacion.getUiSettings().setZoomControlsEnabled(true);
        mapaUbicacion.getUiSettings().setMapToolbarEnabled(false);

        LatLng ubicacionBase = new LatLng(-12.0464, -77.0428);
        mapaUbicacion.clear();
        mapaUbicacion.addMarker(new MarkerOptions()
                .position(ubicacionBase)
                .title("Ubicación de la inmobiliaria"));
        mapaUbicacion.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionBase, 14f));
    }
}
