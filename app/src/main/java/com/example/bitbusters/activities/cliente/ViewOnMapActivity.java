package com.example.bitbusters.activities.cliente;

import com.example.bitbusters.R;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class ViewOnMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISO_UBICACION = 100;
    private GoogleMap mMap;

    // Coordenadas del proyecto (La Perla, Callao)
    private final LatLng coordProyecto = new LatLng(-12.0600, -77.1200);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_on_map);

        // Inicializar mapa
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Botón volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Botón centrar en ubicación del proyecto
        findViewById(R.id.btnCentrar).setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordProyecto, 15f));
            }
        });

        // Chips de filtro
        findViewById(R.id.chipHospital).setOnClickListener(v ->
                Toast.makeText(this, "1 Hospital cercano", Toast.LENGTH_SHORT).show());
        findViewById(R.id.chipGrifos).setOnClickListener(v ->
                Toast.makeText(this, "2 Grifos cercanos", Toast.LENGTH_SHORT).show());
        findViewById(R.id.chipColegio).setOnClickListener(v ->
                Toast.makeText(this, "1 Colegio cercano", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Estilo del mapa
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Centrar cámara en el proyecto
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordProyecto, 14f));

        // Marcador del proyecto principal
        mMap.addMarker(new MarkerOptions()
                .position(coordProyecto)
                .title("Torres Unidas")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Círculo de radio alrededor del proyecto
        mMap.addCircle(new CircleOptions()
                .center(coordProyecto)
                .radius(800)
                .strokeColor(0x551A2E44)
                .fillColor(0x221A2E44)
                .strokeWidth(2f));

        // Marcadores de propiedades cercanas
        LatLng prop1 = new LatLng(-12.0550, -77.1150);
        LatLng prop2 = new LatLng(-12.0620, -77.1100);
        LatLng prop3 = new LatLng(-12.0650, -77.1250);

        mMap.addMarker(new MarkerOptions()
                .position(prop1)
                .title("Propiedad 1")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.addMarker(new MarkerOptions()
                .position(prop2)
                .title("Propiedad 2")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.addMarker(new MarkerOptions()
                .position(prop3)
                .title("Propiedad 3")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        // Línea de ruta desde usuario hasta proyecto
        LatLng coordUsuario = new LatLng(-12.0480, -77.1300);
        mMap.addPolyline(new PolylineOptions()
                .add(coordUsuario, coordProyecto)
                .color(0xFF4CAF50)
                .width(6f));

        // Marcador del usuario
        mMap.addMarker(new MarkerOptions()
                .position(coordUsuario)
                .title("Tu ubicación")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Click en marcador muestra dirección en panel
        mMap.setOnMarkerClickListener(marker -> {
            ((TextView) findViewById(R.id.tvDireccion)).setText(marker.getTitle());
            return false;
        });

        // Pedir permiso de ubicación
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
        if (requestCode == PERMISO_UBICACION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }
}