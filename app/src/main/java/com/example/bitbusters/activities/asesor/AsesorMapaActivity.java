package com.example.bitbusters.activities.asesor;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bitbusters.databinding.ActivityAsesorMapaBinding;
import com.example.bitbusters.models.Proyecto;
import com.example.bitbusters.repository.ProyectoRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import com.example.bitbusters.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsesorMapaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityAsesorMapaBinding binding;

    private static final int PERMISO_UBICACION = 101;
    private GoogleMap mMap;
    private ProyectoMapaAdapter adapter;
    private final ProyectoRepository proyectoRepository = new ProyectoRepository();
    private ListenerRegistration listenerProyectos;

    /** proyectoId → LatLng, para poder centrar la cámara al tocar un ítem de la lista. */
    private final Map<String, LatLng> posicionesPorProyecto = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAsesorMapaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment)
            getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        binding.btnBack.setOnClickListener(v -> finish());

        binding.rvProyectosMapa.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProyectoMapaAdapter(proyecto -> {
            LatLng pos = posicionesPorProyecto.get(proyecto.getId());
            if (pos != null) moveCamera(pos);
        });
        binding.rvProyectosMapa.setAdapter(adapter);
    }

    private void moveCamera(LatLng pos) {
        if (mMap != null) mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        cargarProyectosAsignados();

        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISO_UBICACION);
        }
    }

    /** Se suscribe (misma vía que Cliente: ProyectoRepository) a los proyectos asignados al asesor y dibuja un marcador por cada uno. */
    private void cargarProyectosAsignados() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        listenerProyectos = proyectoRepository.escucharProyectosAsesor(user.getUid(),
                new ProyectoRepository.ProyectosListener() {
                    @Override
                    public void onProyectosActualizados(List<Proyecto> proyectos) {
                        dibujarProyectos(proyectos);
                    }

                    @Override
                    public void onError(String mensaje) {
                        binding.tvSinProyectosMapa.setVisibility(View.VISIBLE);
                        binding.rvProyectosMapa.setVisibility(View.GONE);
                    }
                });
    }

    private void dibujarProyectos(List<Proyecto> proyectos) {
        List<Proyecto> conUbicacion = new ArrayList<>();
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        posicionesPorProyecto.clear();
        if (mMap != null) mMap.clear();

        for (Proyecto p : proyectos) {
            Double lat = p.getLatitud();
            Double lng = p.getLongitud();
            if (lat == null || lng == null) continue;

            LatLng pos = new LatLng(lat, lng);
            posicionesPorProyecto.put(p.getId(), pos);
            conUbicacion.add(p);
            bounds.include(pos);

            String ubicacion = p.getUbicacion() != null && !p.getUbicacion().isEmpty()
                    ? p.getUbicacion() : p.getDistrito();
            if (mMap != null) {
                mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(p.getNombre())
                    .snippet(ubicacion != null ? ubicacion : "")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }
        }

        adapter.setData(conUbicacion);
        binding.tvSinProyectosMapa.setVisibility(
                conUbicacion.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvProyectosMapa.setVisibility(
                conUbicacion.isEmpty() ? View.GONE : View.VISIBLE);

        if (!conUbicacion.isEmpty() && mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 120));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
        @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISO_UBICACION && grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED && mMap != null) {
            if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerProyectos != null) {
            listenerProyectos.remove();
            listenerProyectos = null;
        }
        binding = null;
    }
}
