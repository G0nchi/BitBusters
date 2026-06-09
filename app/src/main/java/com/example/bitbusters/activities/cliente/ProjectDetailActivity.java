package com.example.bitbusters.activities.cliente;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bitbusters.R;
import com.example.bitbusters.adapters.ComentariosAdapter;
import com.example.bitbusters.models.ComentarioEntity;
import com.example.bitbusters.repository.UbicacionRepository;
import com.example.bitbusters.utils.AsesorDatabase;
import com.example.bitbusters.utils.ImageUrls;
import com.example.bitbusters.utils.NotificationHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ProjectDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String EXTRA_PROYECTO   = "proyecto";
    private static final String PREF_SEED_DONE   = "comentarios_seed_done";
    private static final int    REQUEST_LOCATION  = 100;

    // Proyecto
    private String nombreProyecto;

    // Comentarios (Room)
    private AsesorDatabase   db;
    private ComentariosAdapter adapter;
    private TextView         tvRatingPromedio;

    // Mapa
    private GoogleMap                 googleMap;
    private LatLng                    ubicacionProyecto;
    private FusedLocationProviderClient fusedLocationClient;

    // ── Ciclo de vida ──────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        NotificationHelper.crearCanal(this);

        nombreProyecto = resolverNombreProyecto();
        if (nombreProyecto == null || nombreProyecto.isEmpty()) {
            Toast.makeText(this, "Proyecto no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        configurarUIProyecto();
        configurarNavegacion();
        configurarComentarios();
        cargarMapa(nombreProyecto);
    }

    // ── UI del proyecto ────────────────────────────────────────────────────────

    private void configurarUIProyecto() {
        ((TextView) findViewById(R.id.tvNombreProyecto)).setText(nombreProyecto);

        ImageView imgHero = findViewById(R.id.imgHero);
        if (imgHero != null) {
            Glide.with(this)
                    .load(obtenerImagenProyecto(nombreProyecto))
                    .centerCrop()
                    .into(imgHero);
        }

        ImageView imgAsesor = findViewById(R.id.imgAsesor);
        if (imgAsesor != null) {
            Glide.with(this)
                    .load(ImageUrls.AVATAR_FABRI)
                    .centerCrop()
                    .into(imgAsesor);
        }
    }

    // ── Navegación ─────────────────────────────────────────────────────────────

    private void configurarNavegacion() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        View btnAgendar = findViewById(R.id.btnAgendarCita);
        if (btnAgendar != null) {
            btnAgendar.setOnClickListener(v -> {
                Intent intent = new Intent(this, AgendaCitaActivity.class);
                intent.putExtra(EXTRA_PROYECTO, nombreProyecto);
                startActivity(intent);
            });
        }

        findViewById(R.id.btnCompartir).setOnClickListener(v -> { /* TODO compartir */ });
        findViewById(R.id.btnFavorito).setOnClickListener(v -> { /* TODO favoritos */ });
        findViewById(R.id.btnRentar).setOnClickListener(v -> { /* TODO renta */ });
        findViewById(R.id.btnComprar).setOnClickListener(v -> { /* TODO separación */ });
        findViewById(R.id.btnQR).setOnClickListener(v -> { /* TODO mostrar QR */ });

        findViewById(R.id.btnChatAsesor).setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatDetailActivity.class);
            intent.putExtra("contacto",
                    ((TextView) findViewById(R.id.tvNombreAsesor)).getText().toString());
            startActivity(intent);
        });

        View tvVerTodos = findViewById(R.id.tvVerTodosComentarios);
        if (tvVerTodos != null) {
            tvVerTodos.setOnClickListener(v -> {
                Intent intent = new Intent(this, ReviewsActivity.class);
                intent.putExtra(EXTRA_PROYECTO, nombreProyecto);
                startActivity(intent);
            });
        }

        findViewById(R.id.tvVerCostos).setOnClickListener(v -> mostrarDesgloseCostos());

        // Botón Separar Inmueble (bottom bar)
        findViewById(R.id.btnSeparar).setOnClickListener(v -> {
            Intent intentAgenda = new Intent(this, AgendaCitaActivity.class);
            NotificationHelper.lanzarNotificacion(this,
                    "Separación Pendiente",
                    "Tienes 10 minutos para completar el pago",
                    NotificationHelper.NOTIF_SEPARACION,
                    intentAgenda);
            startActivity(new Intent(this, PaymentMethodActivity.class));
        });

        // Botón Cómo llegar → Google Maps externo
        findViewById(R.id.btnComoLlegar).setOnClickListener(v -> {
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
                // Fallback en navegador si Google Maps no está instalado
                Uri browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination="
                        + ubicacionProyecto.latitude + "," + ubicacionProyecto.longitude);
                startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
            }
        });
    }

    // ── Mapa interactivo ───────────────────────────────────────────────────────

    private void cargarMapa(String idProyecto) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        new UbicacionRepository(this).obtenerCoordenadasProyecto(idProyecto,
                new UbicacionRepository.CoordenadasCallback() {
                    @Override
                    public void onSuccess(double lat, double lng, String direccion) {
                        ubicacionProyecto = new LatLng(lat, lng);

                        TextView tvDireccion = findViewById(R.id.tvDireccion);
                        if (tvDireccion != null) tvDireccion.setText("📍 " + direccion);

                        SupportMapFragment mapFragment = (SupportMapFragment)
                                getSupportFragmentManager().findFragmentById(R.id.mapaProyecto);
                        if (mapFragment != null) {
                            mapFragment.getMapAsync(ProjectDetailActivity.this);
                        }
                    }

                    @Override
                    public void onError(String mensaje) {
                        View cardMapa     = findViewById(R.id.cardMapa);
                        View btnComoLlegar = findViewById(R.id.btnComoLlegar);
                        if (cardMapa != null)      cardMapa.setVisibility(View.GONE);
                        if (btnComoLlegar != null) btnComoLlegar.setVisibility(View.GONE);
                        TextView tvDistancia = findViewById(R.id.tvDistancia);
                        if (tvDistancia != null)   tvDistancia.setText(mensaje);
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // Marcador del proyecto
        googleMap.addMarker(new MarkerOptions()
                .position(ubicacionProyecto)
                .title(nombreProyecto)
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
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
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
                    TextView tvDistancia = findViewById(R.id.tvDistancia);
                    if (tvDistancia != null)
                        tvDistancia.setText("No se pudo obtener tu ubicación. Verifica el GPS.");
                }
            }).addOnFailureListener(e -> {
                TextView tvDistancia = findViewById(R.id.tvDistancia);
                if (tvDistancia != null)
                    tvDistancia.setText("Error al obtener ubicación");
                Log.e("MapaUbicacion", "Error: " + e.getMessage());
            });
        } catch (SecurityException e) {
            Log.e("MapaUbicacion", "Sin permisos: " + e.getMessage());
        }
    }

    private void procesarUbicacion(Location location) {
        LatLng ubicacionUsuario = new LatLng(location.getLatitude(), location.getLongitude());

        Log.d("MapaUbicacion", "Usuario: " + location.getLatitude() + ", " + location.getLongitude());
        Log.d("MapaUbicacion", "Proyecto: " + ubicacionProyecto.latitude + ", " + ubicacionProyecto.longitude);

        float[] resultados = new float[1];
        Location.distanceBetween(
                ubicacionUsuario.latitude, ubicacionUsuario.longitude,
                ubicacionProyecto.latitude, ubicacionProyecto.longitude,
                resultados);
        float distanciaKm = resultados[0] / 1000f;

        Log.d("MapaUbicacion", "Distancia calculada: " + distanciaKm + " km");

        TextView tvDistancia = findViewById(R.id.tvDistancia);
        if (tvDistancia != null) {
            if (distanciaKm < 1f) {
                tvDistancia.setText(String.format(Locale.getDefault(),
                        "📍 Estás a %d m del proyecto (línea recta)", (int)(distanciaKm * 1000)));
            } else {
                tvDistancia.setText(String.format(Locale.getDefault(),
                        "📍 Estás a %.1f km del proyecto (línea recta)", distanciaKm));
            }
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
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionUsuario();
            } else {
                TextView tvDistancia = findViewById(R.id.tvDistancia);
                if (tvDistancia != null)
                    tvDistancia.setText("Activa el GPS para ver la distancia");
            }
        }
    }

    // ── Comentarios (Room) ─────────────────────────────────────────────────────

    private void configurarComentarios() {
        db = AsesorDatabase.getInstance(this);
        tvRatingPromedio = findViewById(R.id.tvRatingPromedio);

        RecyclerView rvComentarios = findViewById(R.id.rvComentarios);
        rvComentarios.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComentariosAdapter(this, new ArrayList<>());
        rvComentarios.setAdapter(adapter);

        // Abre DialogFragment en lugar de AddCommentActivity
        findViewById(R.id.btnAgregarComentario).setOnClickListener(v -> {
            AgregarComentarioDialog dialog = AgregarComentarioDialog.newInstance(nombreProyecto);
            dialog.setOnComentarioPublicadoListener(this::cargarComentarios);
            dialog.show(getSupportFragmentManager(), "AgregarComentario");
        });

        cargarComentariosConSeed();
    }

    /** Inserta 3 comentarios de demo la primera vez que corre la app. */
    private void sembrarComentariosDemo() {
        SharedPreferences prefs = getSharedPreferences("bitbusters_prefs", MODE_PRIVATE);
        if (prefs.getBoolean(PREF_SEED_DONE, false)) return;

        long ahora = System.currentTimeMillis();
        long dia   = 24L * 60 * 60 * 1000;

        db.comentarioDao().insertar(new ComentarioEntity(
                "Catalina Ventor", "maria_g", "María González", null,
                5, "Excelente proyecto, muy buena ubicación y atención del asesor.",
                ahora - 2 * dia));
        db.comentarioDao().insertar(new ComentarioEntity(
                "Catalina Ventor", "carlos_m", "Carlos Mendoza", null,
                4, "Los acabados son buenos. El precio me parece razonable para la zona.",
                ahora - 5 * dia));
        db.comentarioDao().insertar(new ComentarioEntity(
                "Catalina Ventor", "andrea_s", "Andrea Silva", null,
                5, "Recién visité el departamento, me encantó.",
                ahora - 7 * dia));

        prefs.edit().putBoolean(PREF_SEED_DONE, true).apply();
    }

    private void cargarComentariosConSeed() {
        Executors.newSingleThreadExecutor().execute(() -> {
            sembrarComentariosDemo();
            fetchYActualizar();
        });
    }

    private void cargarComentarios() {
        Executors.newSingleThreadExecutor().execute(this::fetchYActualizar);
    }

    private void fetchYActualizar() {
        List<ComentarioEntity> lista = db.comentarioDao().obtenerPorProyecto(nombreProyecto);
        Float  promedio = db.comentarioDao().obtenerRatingPromedio(nombreProyecto);
        int    total    = db.comentarioDao().contarPorProyecto(nombreProyecto);
        runOnUiThread(() -> actualizarUIComentarios(lista, promedio, total));
    }

    private void actualizarUIComentarios(List<ComentarioEntity> lista, Float promedio, int total) {
        adapter.actualizarLista(lista);
        if (tvRatingPromedio == null) return;
        if (total == 0) {
            tvRatingPromedio.setText("Sin reseñas aún");
        } else {
            tvRatingPromedio.setText(String.format(Locale.getDefault(), "%.1f (%d %s)",
                    promedio, total, total == 1 ? "reseña" : "reseñas"));
        }
    }

    // ── Métodos auxiliares ─────────────────────────────────────────────────────

    /**
     * Muestra un BottomSheet con el desglose estimado de costos de vida
     * cercanos al proyecto seleccionado.
     */
    private void mostrarDesgloseCostos() {
        com.google.android.material.bottomsheet.BottomSheetDialog bsd =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);

        android.widget.LinearLayout root = new android.widget.LinearLayout(this);
        root.setOrientation(android.widget.LinearLayout.VERTICAL);
        root.setBackgroundColor(android.graphics.Color.WHITE);
        root.setPadding(64, 48, 64, 72);

        android.widget.TextView tvTitulo = new android.widget.TextView(this);
        tvTitulo.setText("Coste de vida estimado");
        tvTitulo.setTextSize(18f);
        tvTitulo.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitulo.setTextColor(0xFF1A1A2E);
        android.widget.LinearLayout.LayoutParams tituloLp = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        tituloLp.bottomMargin = 32;
        tvTitulo.setLayoutParams(tituloLp);
        root.addView(tvTitulo);

        String[][] costos = {
                {"🏠 Alquiler promedio",  "S/ 1,200 / mes"},
                {"💡 Servicios básicos",  "S/   180 / mes"},
                {"🛒 Alimentación",       "S/   800 / mes"},
                {"🚌 Transporte",         "S/   150 / mes"},
                {"📱 Internet y cable",   "S/   120 / mes"},
        };

        for (String[] fila : costos) {
            android.widget.LinearLayout row = new android.widget.LinearLayout(this);
            row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            android.widget.LinearLayout.LayoutParams rowLp =
                    new android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            rowLp.bottomMargin = 20;
            row.setLayoutParams(rowLp);

            android.widget.TextView tvConcepto = new android.widget.TextView(this);
            tvConcepto.setText(fila[0]);
            tvConcepto.setTextSize(14f);
            tvConcepto.setTextColor(0xFF424242);
            tvConcepto.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                    0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            android.widget.TextView tvMonto = new android.widget.TextView(this);
            tvMonto.setText(fila[1]);
            tvMonto.setTextSize(14f);
            tvMonto.setTypeface(null, android.graphics.Typeface.BOLD);
            tvMonto.setTextColor(0xFF1A7EBD);
            tvMonto.setGravity(android.view.Gravity.END);

            row.addView(tvConcepto);
            row.addView(tvMonto);
            root.addView(row);
        }

        android.view.View divisor = new android.view.View(this);
        divisor.setBackgroundColor(0xFFE0E0E0);
        android.widget.LinearLayout.LayoutParams divLp =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 2);
        divLp.topMargin = 12;
        divLp.bottomMargin = 20;
        divisor.setLayoutParams(divLp);
        root.addView(divisor);

        android.widget.LinearLayout rowTotal = new android.widget.LinearLayout(this);
        rowTotal.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        android.widget.LinearLayout.LayoutParams totalLp =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        totalLp.bottomMargin = 40;
        rowTotal.setLayoutParams(totalLp);

        android.widget.TextView tvTotalLabel = new android.widget.TextView(this);
        tvTotalLabel.setText("💰 Total estimado");
        tvTotalLabel.setTextSize(15f);
        tvTotalLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTotalLabel.setTextColor(0xFF1A1A2E);
        tvTotalLabel.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        android.widget.TextView tvTotalMonto = new android.widget.TextView(this);
        tvTotalMonto.setText("S/ 2,450 / mes");
        tvTotalMonto.setTextSize(15f);
        tvTotalMonto.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTotalMonto.setTextColor(0xFF1A1A2E);
        tvTotalMonto.setGravity(android.view.Gravity.END);

        rowTotal.addView(tvTotalLabel);
        rowTotal.addView(tvTotalMonto);
        root.addView(rowTotal);

        android.widget.TextView btnCerrar = new android.widget.TextView(this);
        android.widget.LinearLayout.LayoutParams cerrarLp =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 128);
        btnCerrar.setLayoutParams(cerrarLp);
        btnCerrar.setText("Entendido");
        btnCerrar.setTextSize(15f);
        btnCerrar.setTypeface(null, android.graphics.Typeface.BOLD);
        btnCerrar.setTextColor(android.graphics.Color.WHITE);
        btnCerrar.setGravity(android.view.Gravity.CENTER);
        android.graphics.drawable.GradientDrawable bgCerrar = new android.graphics.drawable.GradientDrawable();
        bgCerrar.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        bgCerrar.setCornerRadius(60f);
        bgCerrar.setColor(0xFF1A7EBD);
        btnCerrar.setBackground(bgCerrar);
        btnCerrar.setOnClickListener(v -> bsd.dismiss());
        root.addView(btnCerrar);

        bsd.setContentView(root);
        bsd.show();
    }

    /** Lee el nombre del proyecto desde extra (navegación normal) o URI deep link (QR). */
    private String resolverNombreProyecto() {
        String nombre = getIntent().getStringExtra(EXTRA_PROYECTO);
        if (nombre != null && !nombre.isEmpty()) {
            Log.d("DetalleProyecto", "Proyecto cargado desde extra: " + nombre);
            return nombre;
        }
        Uri uri = getIntent().getData();
        if (uri != null) {
            Log.d("DeepLink", "URI recibida: " + uri);
            String segmento = uri.getLastPathSegment();
            if (segmento != null && !segmento.isEmpty()) {
                nombre = mapearIdANombre(segmento);
                Log.d("DetalleProyecto", "Proyecto desde deep link, id=" + segmento + " → " + nombre);
                return nombre;
            }
        }
        return null;
    }

    /** Mapea id numérico del QR al nombre de proyecto. */
    private String mapearIdANombre(String id) {
        switch (id) {
            case "1": return "Catalina Ventor";
            case "2": return "Residencial Park";
            case "3": return "Torre Miramar";
            case "4": return "Condominio Las Lomas";
            case "5": return "Catalina Sky";
            default:  return id;
        }
    }

    private int obtenerImagenProyecto(String nombre) {
        if (nombre == null) return ImageUrls.HERO_TORRES_UNIDAS;
        switch (nombre) {
            case "Catalina Ventor":    return ImageUrls.PROYECTO_CATALINA_VENTOR;
            case "Residencial Park":
            case "Residencial El Park": return ImageUrls.PROYECTO_RESIDENCIAL_PARK;
            case "Torre Miramar":      return ImageUrls.PROYECTO_TORRE_MIRAMAR;
            case "Condominio Las Lomas": return ImageUrls.PROYECTO_CONDOMINIO_LOMAS;
            case "Catalina Sky":       return ImageUrls.PROYECTO_CATALINA_SKY;
            default:                   return ImageUrls.HERO_TORRES_UNIDAS;
        }
    }
}
