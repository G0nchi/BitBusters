package com.example.bitbusters.activities.asesor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.bitbusters.R;
import com.example.bitbusters.databinding.ActivityProyectoDetalleBinding;
import com.example.bitbusters.models.Proyecto;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

public class ProyectoDetalleActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "ProyectoDetalleMapa";
    private static final int REQUEST_LOCATION = 201;

    public static final String EXTRA_PROYECTO_ID = "extra_proyecto_id";

    private ActivityProyectoDetalleBinding binding;
    private String proyectoId;
    private String proyectoNombre;

    private LatLng ubicacionProyecto;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProyectoDetalleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        proyectoId = getIntent().getStringExtra(EXTRA_PROYECTO_ID);
        cargarProyecto();
        setupBackButton();
        setupActionButtons();
        setupBottomNav();
    }

    private void cargarProyecto() {
        if (proyectoId == null || proyectoId.isEmpty()) return;

        FirebaseFirestore.getInstance()
            .collection("proyectos")
            .document(proyectoId)
            .get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) return;
                Proyecto p = doc.toObject(Proyecto.class);
                if (p != null) {
                    bindData(p);
                    cargarMapaDesdeProyecto(p);
                }
            });
    }

    private void bindData(Proyecto p) {
        proyectoNombre = p.getNombre() != null ? p.getNombre() : "";
        binding.tvNombre.setText(proyectoNombre);

        String ubicacion = p.getUbicacion() != null && !p.getUbicacion().isEmpty()
                ? p.getUbicacion() : p.getDistrito();
        binding.tvCiudad.setText(ubicacion != null ? ubicacion : "");

        String precio = p.getPrecio() != null && !p.getPrecio().isEmpty()
                ? p.getPrecio()
                : (p.getPrecioPublicado() != null ? p.getPrecioPublicado() : p.getPrecioTotal());
        binding.tvPrecio.setText(precio != null ? precio : "");

        Double ratingProm = p.getRatingPromedio();
        binding.tvRating.setText(ratingProm != null
                ? String.format(Locale.getDefault(), "%.1f", ratingProm)
                : (p.getRating() != null ? p.getRating() : "—"));

        List<String> imagenes = p.getImagenesUri();
        String imageUrl = (imagenes != null && !imagenes.isEmpty())
                ? imagenes.get(0)
                : p.getImageUrl();
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.bg_proyecto_torres)
            .error(R.drawable.bg_proyecto_torres)
            .centerCrop()
            .into(binding.vPlaceholder);

        String estado = p.getEstado() != null ? p.getEstado() : "";
        binding.tvEstado.setText(estado);
        switch (estado) {
            case "en_venta":
            case "En Venta":
                binding.tvEstado.setBackgroundResource(R.drawable.badge_en_venta);
                binding.tvEstado.setTextColor(Color.parseColor("#186A3B"));
                break;
            case "preventa":
            case "Preventa":
                binding.tvEstado.setBackgroundResource(R.drawable.badge_preventa);
                binding.tvEstado.setTextColor(Color.parseColor("#9A5700"));
                break;
            default:
                binding.tvEstado.setBackgroundResource(R.drawable.badge_en_planos);
                binding.tvEstado.setTextColor(Color.parseColor("#1A5799"));
                break;
        }
    }

    // ── Mapa real del proyecto (mismo patrón que ProjectDetailActivity de Cliente) ──

    private void cargarMapaDesdeProyecto(Proyecto p) {
        Double lat = p.getLatitud();
        Double lng = p.getLongitud();
        if (lat == null || lng == null) {
            binding.cardMapaProyecto.setVisibility(View.GONE);
            binding.btnComoLlegarProyecto.setVisibility(View.GONE);
            binding.tvDistanciaProyecto.setText("Este proyecto no tiene ubicación registrada");
            return;
        }

        ubicacionProyecto = new LatLng(lat, lng);
        String direccion = p.getDireccion();
        binding.tvDireccionProyecto.setText("📍 " + (direccion != null && !direccion.isEmpty()
                ? direccion : p.getUbicacion()));

        binding.btnComoLlegarProyecto.setOnClickListener(v -> abrirGoogleMapsExterno());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapaProyectoDetalle);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    private void abrirGoogleMapsExterno() {
        if (ubicacionProyecto == null) {
            Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri gmmUri = Uri.parse("google.navigation:q="
                + ubicacionProyecto.latitude + "," + ubicacionProyecto.longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Uri browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination="
                    + ubicacionProyecto.latitude + "," + ubicacionProyecto.longitude);
            startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        googleMap.addMarker(new MarkerOptions()
                .position(ubicacionProyecto)
                .title(proyectoNombre)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionProyecto, 15f));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        obtenerUbicacionUsuario();
    }

    private void obtenerUbicacionUsuario() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }

        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && esUbicacionReciente(location)) {
                procesarUbicacion(location);
            } else {
                solicitarUbicacionActual();
            }
        }).addOnFailureListener(e -> solicitarUbicacionActual());
    }

    private boolean esUbicacionReciente(Location location) {
        long edadMillis = System.currentTimeMillis() - location.getTime();
        return edadMillis <= 2 * 60 * 1000L;
    }

    private void solicitarUbicacionActual() {
        try {
            CancellationTokenSource cancellationToken = new CancellationTokenSource();
            fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationToken.getToken()
            ).addOnSuccessListener(this, location -> {
                if (location != null) {
                    procesarUbicacion(location);
                } else {
                    binding.tvDistanciaProyecto.setText("No se pudo obtener tu ubicación. Verifica el GPS.");
                }
            }).addOnFailureListener(e -> {
                binding.tvDistanciaProyecto.setText("Error al obtener ubicación");
                Log.e(TAG, "Error: " + e.getMessage());
            });
        } catch (SecurityException e) {
            Log.e(TAG, "Sin permisos: " + e.getMessage());
        }
    }

    private void procesarUbicacion(Location location) {
        LatLng ubicacionUsuario = new LatLng(location.getLatitude(), location.getLongitude());

        float[] resultados = new float[1];
        Location.distanceBetween(
                ubicacionUsuario.latitude, ubicacionUsuario.longitude,
                ubicacionProyecto.latitude, ubicacionProyecto.longitude,
                resultados);
        float distanciaKm = resultados[0] / 1000f;

        if (distanciaKm < 1f) {
            binding.tvDistanciaProyecto.setText(String.format(Locale.getDefault(),
                    "📍 Estás a %d m del proyecto (línea recta)", (int) (distanciaKm * 1000)));
        } else {
            binding.tvDistanciaProyecto.setText(String.format(Locale.getDefault(),
                    "📍 Estás a %.1f km del proyecto (línea recta)", distanciaKm));
        }

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(ubicacionUsuario)
                .include(ubicacionProyecto)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionUsuario();
            } else {
                binding.tvDistanciaProyecto.setText("Activa el GPS para ver la distancia");
            }
        }
    }

    private void setupActionButtons() {
        binding.btnRegistrar.setOnClickListener(v -> {
            Intent intent = new Intent(this, NuevaSeparacionActivity.class);
            intent.putExtra(NuevaSeparacionActivity.EXTRA_PROYECTO, proyectoNombre);
            intent.putExtra(NuevaSeparacionActivity.EXTRA_PROYECTO_ID, proyectoId);
            startActivity(intent);
        });
        binding.btnContactar.setOnClickListener(v ->
            new ContactarClienteBottomSheet().show(getSupportFragmentManager(), "contactar"));
    }

    private void setupBackButton() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_inicio);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_citas) {
                startActivity(new Intent(this, CitasAgendadasActivity.class));
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, MensajesActivity.class));
            } else if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, AsesorHomeActivity.class));
                finish();
            }
            return true;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
