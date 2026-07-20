package com.example.bitbusters.activities.cliente;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.example.bitbusters.models.Proyecto;
import com.example.bitbusters.repository.ChatRepository;
import com.example.bitbusters.repository.ProyectoRepository;
import com.example.bitbusters.repository.UbicacionRepository;
import com.example.bitbusters.utils.ImageUrls;
import com.example.bitbusters.utils.NotificationHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
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
import java.util.Map;

public class ProjectDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String EXTRA_PROYECTO = "proyecto";
    private static final String EXTRA_PROYECTO_ID = "proyecto_id";
    private static final int    REQUEST_LOCATION = 100;

    // Proyecto
    private String nombreProyecto;
    private String proyectoId;
    private Proyecto proyectoActual;
    private ProyectoRepository proyectoRepository;
    private boolean comentariosConfigurados = false;
    private ProgressBar progressProject;
    private View layoutProjectError;
    private TextView tvProjectError;
    private View scrollProjectContent;
    private ChatRepository chatRepository;

    // Comentarios / valoraciones Firestore
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

        proyectoRepository = new ProyectoRepository();
        chatRepository = new ChatRepository();

        progressProject = findViewById(R.id.progressProject);
        layoutProjectError = findViewById(R.id.layoutProjectError);
        tvProjectError = findViewById(R.id.tvProjectError);
        scrollProjectContent = findViewById(R.id.scrollProjectContent);
        findViewById(R.id.btnRetryProject).setOnClickListener(v -> cargarDatosProyecto());

        nombreProyecto = resolverNombreProyecto();
        if (nombreProyecto == null || nombreProyecto.isEmpty()) {
            Toast.makeText(this, "Proyecto no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        configurarUIProyecto();
        mostrarCargandoProyecto();
        cargarDatosProyecto();
        configurarNavegacion();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (comentariosConfigurados) {
            cargarComentarios();
        }
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
    }

    // ── Navegación ─────────────────────────────────────────────────────────────

    private void configurarNavegacion() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        View btnAgendar = findViewById(R.id.btnAgendarCita);
        if (btnAgendar != null) {
            btnAgendar.setEnabled(false);
            btnAgendar.setAlpha(0.5f);
        }

        findViewById(R.id.btnCompartir).setOnClickListener(v -> { /* TODO compartir */ });
        findViewById(R.id.btnFavorito).setOnClickListener(v -> { /* TODO favoritos */ });
        findViewById(R.id.btnQR).setOnClickListener(v -> mostrarDialogoQR());

        findViewById(R.id.btnChatAsesor).setOnClickListener(v -> abrirChatConAsesor());

        View tvVerTodos = findViewById(R.id.tvVerTodosComentarios);
        if (tvVerTodos != null) {
            tvVerTodos.setOnClickListener(v -> {
                Intent intent = new Intent(this, ReviewsActivity.class);
                intent.putExtra(ReviewsActivity.EXTRA_PROYECTO, nombreProyecto);
                String idProyecto = proyectoActual != null ? proyectoActual.getId() : proyectoId;
                intent.putExtra(ReviewsActivity.EXTRA_PROYECTO_ID, idProyecto != null ? idProyecto : "");
                startActivity(intent);
            });
        }

        findViewById(R.id.tvVerCostos).setOnClickListener(v -> mostrarDesgloseCostos());

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
        mostrarCargaSecundaria(true);
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
                        mostrarCargaSecundaria(false);
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
        mostrarCargaSecundaria(true);
        try {
            CancellationTokenSource cancellationToken = new CancellationTokenSource();
            fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationToken.getToken()
            ).addOnSuccessListener(this, location -> {
                if (location != null) {
                    procesarUbicacion(location);
                } else {
                    mostrarCargaSecundaria(false);
                    TextView tvDistancia = findViewById(R.id.tvDistancia);
                    if (tvDistancia != null)
                        tvDistancia.setText("No se pudo obtener tu ubicación. Verifica el GPS.");
                }
            }).addOnFailureListener(e -> {
                mostrarCargaSecundaria(false);
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
        mostrarCargaSecundaria(false);
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
                mostrarCargaSecundaria(false);
                TextView tvDistancia = findViewById(R.id.tvDistancia);
                if (tvDistancia != null)
                    tvDistancia.setText("Activa el GPS para ver la distancia");
            }
        }
    }

    // ── Comentarios / valoraciones Firestore ───────────────────────────────────

    private void configurarComentarios() {
        tvRatingPromedio = findViewById(R.id.tvRatingPromedio);

        RecyclerView rvComentarios = findViewById(R.id.rvComentarios);
        rvComentarios.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComentariosAdapter(this, new ArrayList<>());
        rvComentarios.setAdapter(adapter);

        findViewById(R.id.btnAgregarComentario).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddCommentActivity.class);
            intent.putExtra("proyecto", nombreProyecto);
            intent.putExtra("proyectoId", proyectoId != null ? proyectoId : "");
            intent.putExtra("uidAsesor", obtenerPrimerAsesorUid());
            startActivity(intent);
        });

        cargarComentarios();
    }

    private void cargarComentarios() {
        com.google.firebase.firestore.Query query;
        if (proyectoId != null && !proyectoId.trim().isEmpty()) {
            query = FirebaseFirestore.getInstance()
                    .collection("valoraciones")
                    .whereEqualTo("proyectoId", proyectoId);
        } else {
            query = FirebaseFirestore.getInstance()
                    .collection("valoraciones")
                    .whereEqualTo("proyecto", nombreProyecto);
        }

        query.get()
                .addOnSuccessListener(snapshot -> {
                    List<ComentarioEntity> lista = new ArrayList<>();
                    int total = 0;
                    int suma = 0;
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        ComentarioEntity comentario = mapComentario(doc);
                        lista.add(comentario);
                        total++;
                        suma += comentario.rating;
                    }
                    lista.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
                    Float promedio = total > 0 ? (float) suma / total : null;
                    actualizarUIComentarios(lista, promedio, total);
                })
                .addOnFailureListener(e -> {
                    adapter.actualizarLista(new ArrayList<>());
                    if (tvRatingPromedio != null) {
                        tvRatingPromedio.setText("No se pudieron cargar reseñas");
                    }
                });
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

    private ComentarioEntity mapComentario(DocumentSnapshot doc) {
        String nombre = firstNonEmpty(
                doc.getString("clienteNombre"),
                doc.getString("nombreCliente"),
                "Cliente");
        String uid = firstNonEmpty(doc.getString("uidCliente"), "", "");
        String texto = firstNonEmpty(doc.getString("comentario"), doc.getString("texto"), "");
        Long calificacionLong = doc.getLong("calificacion");
        int rating = calificacionLong != null ? calificacionLong.intValue() : 0;
        com.google.firebase.Timestamp ts = doc.getTimestamp("timestamp");
        long fecha = ts != null ? ts.toDate().getTime() : System.currentTimeMillis();
        return new ComentarioEntity(
                firstNonEmpty(proyectoId, nombreProyecto, ""),
                uid,
                nombre,
                null,
                rating,
                texto,
                fecha);
    }

    private String obtenerPrimerAsesorUid() {
        if (proyectoActual == null || proyectoActual.getUidAsesores() == null
                || proyectoActual.getUidAsesores().isEmpty()) {
            return "";
        }
        return proyectoActual.getUidAsesores().get(0);
    }

    private String firstNonEmpty(String primary, String secondary, String fallback) {
        if (primary != null && !primary.trim().isEmpty()) return primary;
        if (secondary != null && !secondary.trim().isEmpty()) return secondary;
        return fallback != null ? fallback : "";
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
        proyectoId = getIntent().getStringExtra(EXTRA_PROYECTO_ID);
        String nombre = getIntent().getStringExtra(EXTRA_PROYECTO);
        if (nombre != null && !nombre.isEmpty()) {
            Log.d("DetalleProyecto", "Proyecto cargado desde extra: " + nombre);
            return nombre;
        }
        if (proyectoId != null && !proyectoId.isEmpty()) {
            Log.d("DetalleProyecto", "Proyecto cargado desde id extra: " + proyectoId);
            return proyectoId;
        }
        Uri uri = getIntent().getData();
        if (uri != null) {
            Log.d("DeepLink", "URI recibida: " + uri);
            String segmento = uri.getLastPathSegment();
            if (segmento != null && !segmento.isEmpty()) {
                nombre = mapearIdANombre(segmento);
                if (nombre.equals(segmento)) {
                    proyectoId = segmento;
                } else {
                    proyectoId = null;
                }
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

    // ── Datos dinámicos del proyecto ──────────────────────────────────────────────

    private void cargarDatosProyecto() {
        mostrarCargandoProyecto();
        ProyectoRepository.ProyectoCallback callback = new ProyectoRepository.ProyectoCallback() {
            @Override
            public void onSuccess(Proyecto p) {
                proyectoActual = p;
                if (p.getNombre() != null && !p.getNombre().isEmpty()) {
                    nombreProyecto = p.getNombre();
                }
                configurarUIProyecto();
                mostrarContenidoProyecto();
                pintarDatosProyecto();
                configurarComentariosSiHaceFalta();
                cargarMapaDesdeProyecto();
                List<String> uidAsesores = p.getUidAsesores();
                if (uidAsesores != null && !uidAsesores.isEmpty()) {
                    cargarAsesor(uidAsesores.get(0));
                }
            }
            @Override
            public void onError(String mensaje) {
                Log.e("DetalleProyecto", "Error cargando datos: " + mensaje);
                mostrarErrorProyecto("No se pudo cargar el proyecto: " + mensaje);
            }
        };

        if (proyectoId != null && !proyectoId.isEmpty()) {
            proyectoRepository.obtenerPorId(proyectoId, callback);
        } else {
            proyectoRepository.obtenerPorNombre(nombreProyecto, callback);
        }
    }

    private void mostrarCargandoProyecto() {
        if (progressProject != null) progressProject.setVisibility(View.VISIBLE);
        if (layoutProjectError != null) layoutProjectError.setVisibility(View.GONE);
        if (scrollProjectContent != null) scrollProjectContent.setVisibility(View.GONE);
    }

    private void mostrarContenidoProyecto() {
        if (progressProject != null) progressProject.setVisibility(View.GONE);
        if (layoutProjectError != null) layoutProjectError.setVisibility(View.GONE);
        if (scrollProjectContent != null) scrollProjectContent.setVisibility(View.VISIBLE);
    }

    private void mostrarErrorProyecto(String mensaje) {
        if (progressProject != null) progressProject.setVisibility(View.GONE);
        if (scrollProjectContent != null) scrollProjectContent.setVisibility(View.GONE);
        if (layoutProjectError != null) layoutProjectError.setVisibility(View.VISIBLE);
        if (tvProjectError != null) tvProjectError.setText(mensaje);
    }

    private void mostrarCargaSecundaria(boolean loading) {
        if (progressProject != null && scrollProjectContent != null && scrollProjectContent.getVisibility() == View.VISIBLE) {
            progressProject.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void pintarDatosProyecto() {
        if (proyectoActual == null) return;

        // Habilitar btnAgendar solo si el proyecto tiene asesor asignado
        View btnAgendar = findViewById(R.id.btnAgendarCita);
        if (btnAgendar != null) {
            List<String> asesores = proyectoActual.getUidAsesores();
            String uid1er = (asesores != null && !asesores.isEmpty()) ? asesores.get(0) : null;
            String pid    = proyectoActual.getId();

            if (uid1er == null) {
                Log.w("ProjectDetail", "Proyecto sin uidAsesores: " + pid);
                btnAgendar.setEnabled(false);
                btnAgendar.setAlpha(0.4f);
                Toast.makeText(this,
                        "Este proyecto no tiene asesor asignado. Contacta al administrador.",
                        Toast.LENGTH_LONG).show();
            } else {
                btnAgendar.setEnabled(true);
                btnAgendar.setAlpha(1f);
                btnAgendar.setOnClickListener(v -> {
                    Intent intent = new Intent(this, AgendaCitaActivity.class);
                    intent.putExtra(AgendaCitaActivity.EXTRA_PROYECTO,    proyectoActual.getNombre());
                    intent.putExtra(AgendaCitaActivity.EXTRA_PROYECTO_ID, pid);
                    intent.putExtra(AgendaCitaActivity.EXTRA_UID_ASESOR,  uid1er);
                    startActivity(intent);
                });
            }
        }

        TextView tvNombre = findViewById(R.id.tvNombreProyecto);
        if (tvNombre != null) tvNombre.setText(proyectoActual.getNombre());

        TextView tvUbicacion = findViewById(R.id.tvUbicacion);
        if (tvUbicacion != null) tvUbicacion.setText(proyectoActual.getUbicacion());

        TextView tvPrecio = findViewById(R.id.tvPrecio);
        if (tvPrecio != null) tvPrecio.setText(proyectoActual.getPrecio());

        TextView tvRating = findViewById(R.id.tvRatingBadge);
        if (tvRating != null) {
            String r = proyectoActual.getRating();
            tvRating.setText((r != null && !r.isEmpty()) ? "★ " + r : "");
        }

        TextView tvTipo = findViewById(R.id.tvTipoBadge);
        if (tvTipo != null) tvTipo.setText(proyectoActual.getTipo());

        String url = proyectoActual.getImageUrl();
        ImageView imgHero = findViewById(R.id.imgHero);
        if (imgHero != null && url != null && !url.isEmpty()) {
            Glide.with(this).load(url).centerCrop().into(imgHero);
        }

        pintarTipologias();
    }

    private void configurarComentariosSiHaceFalta() {
        if (!comentariosConfigurados) {
            configurarComentarios();
            comentariosConfigurados = true;
        }
    }

    private void cargarMapaDesdeProyecto() {
        if (proyectoActual == null) {
            cargarMapa(nombreProyecto);
            return;
        }

        Double lat = proyectoActual.getLatitud();
        Double lng = proyectoActual.getLongitud();
        if (lat == null || lng == null) {
            cargarMapa(nombreProyecto);
            return;
        }

        ubicacionProyecto = new LatLng(lat, lng);
        TextView tvDireccion = findViewById(R.id.tvDireccion);
        if (tvDireccion != null) {
            String direccion = proyectoActual.getDireccion();
            tvDireccion.setText("📍 " + (direccion != null && !direccion.isEmpty()
                    ? direccion : proyectoActual.getUbicacion()));
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapaProyecto);
        if (mapFragment != null) {
            mapFragment.getMapAsync(ProjectDetailActivity.this);
        }
    }

    private void pintarTipologias() {
        if (proyectoActual == null) return;
        List<Map<String, Object>> tipologias = proyectoActual.getTipologias();
        if (tipologias == null || tipologias.isEmpty()) return;

        Map<String, Object> primera = tipologias.get(0);

        TextView tvDorm = findViewById(R.id.tvChipDormitorios);
        if (tvDorm != null) {
            Object dorm = primera.get("dormitorios");
            tvDorm.setText(dorm != null ? "🛏 " + dorm + " Cuartos" : "");
        }

        TextView tvBanos = findViewById(R.id.tvChipBanos);
        if (tvBanos != null) {
            Object ban = primera.get("banos");
            tvBanos.setText(ban != null ? "🚿 " + ban + " Baño" : "");
        }

        TextView tvMetraje = findViewById(R.id.tvChipMetraje);
        if (tvMetraje != null) {
            Object met = primera.get("metraje");
            if (met instanceof Double) {
                tvMetraje.setText(String.format(Locale.getDefault(), "📐 %.0f m²", (Double) met));
            } else if (met != null) {
                tvMetraje.setText("📐 " + met + " m²");
            } else {
                tvMetraje.setText("");
            }
        }
    }

    private void mostrarDialogoQR() {
        String idProyecto = proyectoActual != null && proyectoActual.getId() != null
                ? proyectoActual.getId()
                : proyectoId;
        if (idProyecto == null || idProyecto.isEmpty()) {
            Toast.makeText(this, "QR no disponible para este proyecto", Toast.LENGTH_SHORT).show();
            return;
        }

        String qrUrl = proyectoActual != null && proyectoActual.getQrCode() != null
                ? proyectoActual.getQrCode()
                : "";

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        int pad = dpToPx(24);
        layout.setPadding(pad, pad, pad, pad);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Código QR del Proyecto");
        tvTitle.setTextSize(18f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setGravity(Gravity.CENTER);
        layout.addView(tvTitle);

        TextView tvSubtitle = new TextView(this);
        tvSubtitle.setText(nombreProyecto != null ? nombreProyecto : "Proyecto");
        tvSubtitle.setTextSize(13f);
        tvSubtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        subParams.topMargin = dpToPx(4);
        subParams.bottomMargin = dpToPx(12);
        tvSubtitle.setLayoutParams(subParams);
        layout.addView(tvSubtitle);

        ImageView imgQr = new ImageView(this);
        int qrSize = dpToPx(260);
        LinearLayout.LayoutParams qrParams = new LinearLayout.LayoutParams(qrSize, qrSize);
        qrParams.gravity = Gravity.CENTER_HORIZONTAL;
        imgQr.setLayoutParams(qrParams);
        imgQr.setScaleType(ImageView.ScaleType.FIT_CENTER);

        boolean qrRemoto = qrUrl.startsWith("http://") || qrUrl.startsWith("https://");
        if (qrRemoto) {
            Glide.with(this).load(qrUrl).into(imgQr);
        } else {
            try {
                BarcodeEncoder encoder = new BarcodeEncoder();
                Bitmap bitmap = encoder.encodeBitmap(
                        "inmobiliaria://proyecto/" + idProyecto,
                        BarcodeFormat.QR_CODE, 512, 512);
                imgQr.setImageBitmap(bitmap);
            } catch (Exception e) {
                Toast.makeText(this, "Error generando el QR", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        layout.addView(imgQr);

        Button btnCerrar = new Button(this);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.topMargin = dpToPx(16);
        btnCerrar.setLayoutParams(btnParams);
        btnCerrar.setText("Cerrar");
        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        layout.addView(btnCerrar);

        dialog.setContentView(layout);
        dialog.show();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void cargarAsesor(String uidAsesor) {
        FirebaseFirestore.getInstance().collection("users").document(uidAsesor).get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) return;
                String nombreAsesor = doc.getString("nombre");
                String fotoUrl = doc.getString("fotoUrl");

                TextView tvNombreAsesor = findViewById(R.id.tvNombreAsesor);
                if (tvNombreAsesor != null && nombreAsesor != null) {
                    tvNombreAsesor.setText(nombreAsesor);
                }

                ImageView imgAsesor = findViewById(R.id.imgAsesor);
                if (imgAsesor != null) {
                    Glide.with(ProjectDetailActivity.this)
                        .load(fotoUrl)
                        .placeholder(R.drawable.avatar_asesor_fabri)
                        .centerCrop()
                        .into(imgAsesor);
                }
            })
            .addOnFailureListener(e ->
                Log.e("DetalleProyecto", "Error cargando asesor: " + e.getMessage()));
    }

    // ── Chat ──────────────────────────────────────────────────────────────────────

    private void abrirChatConAsesor() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Debes iniciar sesión para chatear", Toast.LENGTH_SHORT).show();
            return;
        }
        if (proyectoActual == null) {
            Toast.makeText(this, "Cargando datos del proyecto, intenta de nuevo", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> uidAsesores = proyectoActual.getUidAsesores();
        if (uidAsesores == null || uidAsesores.isEmpty()) {
            Toast.makeText(this, "Este proyecto no tiene asesor asignado", Toast.LENGTH_LONG).show();
            return;
        }

        String uidCliente = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String uidAsesor  = uidAsesores.get(0);
        String idProyecto = proyectoActual.getId() != null ? proyectoActual.getId() : nombreProyecto;
        Log.d("ChatBoton", "UID asesor seleccionado: " + uidAsesor);

        FirebaseFirestore.getInstance().collection("users").document(uidAsesor).get()
            .addOnSuccessListener(docAsesor -> {
                if (!docAsesor.exists()) {
                    Toast.makeText(this, "El asesor de este proyecto no está disponible", Toast.LENGTH_LONG).show();
                    Log.e("ChatBoton", "No existe el documento users/" + uidAsesor);
                    return;
                }

                String nombreAsesor = docAsesor.getString("nombre");
                String fotoAsesor   = docAsesor.getString("fotoUrl");

                FirebaseFirestore.getInstance().collection("users").document(uidCliente).get()
                    .addOnSuccessListener(docCliente -> {
                        String nombreClienteRaw = docCliente.getString("nombre");
                        String nombreCliente = (nombreClienteRaw != null && !nombreClienteRaw.isEmpty())
                                ? nombreClienteRaw : "Cliente";

                        chatRepository.abrirOCrearChat(
                            uidCliente, uidAsesor,
                            idProyecto, nombreProyecto,
                            nombreCliente, nombreAsesor, fotoAsesor,
                            chatId -> {
                                Intent intent = new Intent(ProjectDetailActivity.this, ChatDetailActivity.class);
                                intent.putExtra("chatId", chatId);
                                intent.putExtra("uidAsesor", uidAsesor);
                                intent.putExtra("nombreAsesor", nombreAsesor);
                                intent.putExtra("fotoAsesor", fotoAsesor);
                                startActivity(intent);
                            },
                            error -> {
                                Toast.makeText(ProjectDetailActivity.this, error, Toast.LENGTH_LONG).show();
                                Log.e("ChatBoton", "Error abrirOCrearChat: " + error);
                            }
                        );
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al cargar tu perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("ChatBoton", "Error users/" + uidCliente + ": " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al cargar el asesor: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ChatBoton", "Error users/" + uidAsesor + ": " + e.getMessage());
            });
    }
}
