package com.example.bitbusters.activities.asesor;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.bitbusters.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class AsesorMapaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISO_UBICACION = 101;
    private GoogleMap mMap;

    // Coordenadas de los proyectos asignados
    private static final LatLng MARINA = new LatLng(-12.0773, -77.0905);   // San Miguel
    private static final LatLng TORRES = new LatLng(-12.1191, -77.0296);   // Miraflores
    private static final LatLng PINOS  = new LatLng(-12.1500, -77.0000);   // Surco

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_mapa);

        SupportMapFragment mapFragment = (SupportMapFragment)
            getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Tap en un proyecto del panel → centra la cámara
        findViewById(R.id.item_marina).setOnClickListener(v -> moveCamera(MARINA));
        findViewById(R.id.item_torres).setOnClickListener(v -> moveCamera(TORRES));
        findViewById(R.id.item_pinos).setOnClickListener(v -> moveCamera(PINOS));
    }

    private void moveCamera(LatLng pos) {
        if (mMap != null) mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.addMarker(new MarkerOptions()
            .position(MARINA)
            .title("Vista Marina Residencial")
            .snippet("San Miguel · S/ 320,000")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        mMap.addMarker(new MarkerOptions()
            .position(TORRES)
            .title("Torres del Sol")
            .snippet("Miraflores · S/ 450,000")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        mMap.addMarker(new MarkerOptions()
            .position(PINOS)
            .title("Condominio Los Pinos")
            .snippet("Surco · S/ 580,000")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Encuadra todos los marcadores
        LatLngBounds bounds = new LatLngBounds.Builder()
            .include(MARINA).include(TORRES).include(PINOS).build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120));

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
}
