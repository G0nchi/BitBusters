package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.AdminAsesorInmobiliariaAdapter;
import com.example.bitbusters.data.FirestoreAsesoresRepository;
import com.example.bitbusters.models.AdminAsesorInmobiliaria;
import com.example.bitbusters.utils.AdminPreferencesManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AdminEditarInmobiliariaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private RecyclerView rvAsesores;
    private AdminAsesorInmobiliariaAdapter adapter;
    private GoogleMap mapaUbicacion;
    private Button btnVerTodosAsesoresEdit;
    private TextView tvAsesoresEmptyEdit;
    private TextInputEditText etNombreComercialEdit;
    private TextInputEditText etRazonSocialEdit;
    private final FirestoreAsesoresRepository asesoresRepository = new FirestoreAsesoresRepository();
    private final List<AdminAsesorInmobiliaria> asesoresActuales = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_editar_inmobiliaria);
        bindEmpresaFields();
        cargarDatosInmobiliaria();
        setupNavigationListeners();
        setupRecyclerView();
        setupMap();
    }

    @Override
    protected void onStart() {
        super.onStart();
        cargarAsesoresDesdeFirestore();
    }

    private void bindEmpresaFields() {
        etNombreComercialEdit = findViewById(R.id.etNombreComercialEdit);
        etRazonSocialEdit = findViewById(R.id.etRazonSocialEdit);
    }

    private void cargarDatosInmobiliaria() {
        String inmobiliaria = AdminPreferencesManager.obtenerInmobiliaria(this);
        if (etNombreComercialEdit != null) {
            etNombreComercialEdit.setText(inmobiliaria);
        }
        if (etRazonSocialEdit != null) {
            etRazonSocialEdit.setText(inmobiliaria);
        }
    }

    private void guardarDatosInmobiliaria() {
        String nombreComercial = getText(etNombreComercialEdit);
        if (nombreComercial.isEmpty()) {
            Toast.makeText(this, "Ingresa el nombre comercial", Toast.LENGTH_SHORT).show();
            return;
        }
        AdminPreferencesManager.guardarInmobiliaria(this, nombreComercial);
        Toast.makeText(this, "Información actualizada", Toast.LENGTH_SHORT).show();
        finish();
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
            btnSaveChanges.setOnClickListener(v -> guardarDatosInmobiliaria());
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
        tvAsesoresEmptyEdit = findViewById(R.id.tvAsesoresEmptyEdit);
        if (rvAsesores != null) {
            rvAsesores.setLayoutManager(new LinearLayoutManager(this));
            adapter = new AdminAsesorInmobiliariaAdapter(
                    new ArrayList<>(),
                    null,
                    false
            );
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
        if (tvAsesoresEmptyEdit != null) {
            tvAsesoresEmptyEdit.setVisibility(hayAsesores ? View.GONE : View.VISIBLE);
        }
        if (btnVerTodosAsesoresEdit != null) {
            btnVerTodosAsesoresEdit.setVisibility(asesoresActuales.size() > 4 ? View.VISIBLE : View.GONE);
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

    private String getText(TextInputEditText input) {
        return input != null && input.getText() != null ? input.getText().toString().trim() : "";
    }
}
