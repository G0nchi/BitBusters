package com.example.bitbusters.activities.cliente;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.bitbusters.R;
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
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class ViewOnMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_PROYECTO = "proyecto";
    public static final String EXTRA_LATITUD = "latitud";
    public static final String EXTRA_LONGITUD = "longitud";

    private static final int PERMISO_UBICACION = 100;
    private static final LatLng LIMA_DEFAULT = new LatLng(-12.0464, -77.0428);

    private GoogleMap mMap;
    private TextView tvUbicacionActual;
    private TextView tvDireccion;
    private final ProyectoRepository proyectoRepository = new ProyectoRepository();
    private ListenerRegistration listenerProyectos;
    private final List<Proyecto> proyectosConUbicacion = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_on_map);

        tvUbicacionActual = findViewById(R.id.tvUbicacionActual);
        tvDireccion = findViewById(R.id.tvDireccion);

        String nombreProyecto = getIntent().getStringExtra(EXTRA_PROYECTO);
        if (nombreProyecto != null && !nombreProyecto.trim().isEmpty()) {
            tvUbicacionActual.setText(nombreProyecto);
        } else {
            tvUbicacionActual.setText("Mapa de proyectos");
        }

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCentrar).setOnClickListener(v -> centrarMapa());
        findViewById(R.id.chipHospital).setOnClickListener(v ->
                Toast.makeText(this, "Mostrando proyectos reales con ubicación registrada", Toast.LENGTH_SHORT).show());
        findViewById(R.id.chipGrifos).setOnClickListener(v ->
                Toast.makeText(this, proyectosConUbicacion.size() + " proyectos con coordenadas", Toast.LENGTH_SHORT).show());
        findViewById(R.id.chipColegio).setOnClickListener(v ->
                Toast.makeText(this, "Los puntos cercanos dependen de Google Maps", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listenerProyectos != null) {
            listenerProyectos.remove();
            listenerProyectos = null;
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        habilitarUbicacionSiPermiso();

        if (hayCoordenadasEnIntent()) {
            mostrarProyectoIntent();
        } else {
            escucharProyectos();
        }
    }

    private boolean hayCoordenadasEnIntent() {
        return getIntent().hasExtra(EXTRA_LATITUD) && getIntent().hasExtra(EXTRA_LONGITUD);
    }

    private void mostrarProyectoIntent() {
        double lat = getIntent().getDoubleExtra(EXTRA_LATITUD, LIMA_DEFAULT.latitude);
        double lng = getIntent().getDoubleExtra(EXTRA_LONGITUD, LIMA_DEFAULT.longitude);
        String nombre = getIntent().getStringExtra(EXTRA_PROYECTO);
        LatLng ubicacion = new LatLng(lat, lng);
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(ubicacion)
                .title(nombre != null ? nombre : "Proyecto")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 15f));
        tvDireccion.setText(nombre != null ? nombre : "Proyecto seleccionado");
    }

    private void escucharProyectos() {
        listenerProyectos = proyectoRepository.escucharProyectosCliente(new ProyectoRepository.ProyectosListener() {
            @Override
            public void onProyectosActualizados(List<Proyecto> proyectos) {
                proyectosConUbicacion.clear();
                if (proyectos != null) {
                    for (Proyecto proyecto : proyectos) {
                        if (proyecto.getLatitud() != null && proyecto.getLongitud() != null) {
                            proyectosConUbicacion.add(proyecto);
                        }
                    }
                }
                dibujarProyectos();
            }

            @Override
            public void onError(String mensaje) {
                Toast.makeText(ViewOnMapActivity.this, "No se pudo cargar el mapa: " + mensaje, Toast.LENGTH_LONG).show();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LIMA_DEFAULT, 11f));
            }
        });
    }

    private void dibujarProyectos() {
        if (mMap == null) return;
        mMap.clear();

        if (proyectosConUbicacion.isEmpty()) {
            tvDireccion.setText("No hay proyectos con coordenadas registradas.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LIMA_DEFAULT, 11f));
            return;
        }

        LatLngBounds.Builder bounds = LatLngBounds.builder();
        for (Proyecto proyecto : proyectosConUbicacion) {
            LatLng ubicacion = new LatLng(proyecto.getLatitud(), proyecto.getLongitud());
            bounds.include(ubicacion);
            mMap.addMarker(new MarkerOptions()
                    .position(ubicacion)
                    .title(proyecto.getNombre())
                    .snippet(proyecto.getInmobiliariaNombre())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }

        centrarMapa();
        tvDireccion.setText(proyectosConUbicacion.size() + " proyectos activos con ubicación.");
        mMap.setOnMarkerClickListener(marker -> {
            tvDireccion.setText(marker.getTitle());
            return false;
        });
    }

    private void centrarMapa() {
        if (mMap == null) return;
        if (proyectosConUbicacion.isEmpty()) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LIMA_DEFAULT, 11f));
            return;
        }
        if (proyectosConUbicacion.size() == 1) {
            Proyecto proyecto = proyectosConUbicacion.get(0);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(proyecto.getLatitud(), proyecto.getLongitud()), 15f));
            return;
        }
        LatLngBounds.Builder bounds = LatLngBounds.builder();
        for (Proyecto proyecto : proyectosConUbicacion) {
            bounds.include(new LatLng(proyecto.getLatitud(), proyecto.getLongitud()));
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 120));
    }

    private void habilitarUbicacionSiPermiso() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISO_UBICACION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISO_UBICACION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && mMap != null
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }
}
