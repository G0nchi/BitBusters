package com.example.bitbusters.activities.admin;

import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.bitbusters.R;
import com.example.bitbusters.data.AdminProyectoSessionData;
import com.example.bitbusters.data.AdminProyectosRepository;
import com.example.bitbusters.models.AdminProyecto;
import com.example.bitbusters.models.Tipologia;
import com.example.bitbusters.utils.AdminPreferencesManager;
import com.example.bitbusters.utils.AdminStorageManager;
import com.example.bitbusters.utils.NotificationHelper;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Formulario de creación de proyecto inmobiliario del Administrador.
 *
 * Partes implementadas:
 *  Parte 1 — Tipologías dinámicas: onResume() renderiza cards desde la sesión
 *  Parte 2 — Asesores dinámicos: onResume() renderiza chips desde la sesión
 *  Parte 3 — Distrito con AlertDialog de búsqueda en tiempo real
 *  Parte 4 — Al guardar, crea AdminProyecto y lo agrega a AdminProyectosRepository
 */
public class AdminCrearProyectoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "AdminCrearProyecto";

    // ── Campos de texto ───────────────────────────────────────────────────────
    private TextInputEditText etNombreProyecto, etDescripcionProyecto;
    private TextView          tvFechaEntrega;
    private AppCompatAutoCompleteTextView etDireccion;
    private TextView          tvDistrito;          // Parte 3: no editable, abre diálogo
    private LinearLayout      layoutDistritoSelector;
    private TextInputEditText etCostoSeparacion, etPrecioTotal;
    private TextInputEditText etNombreComercial, etPrecioPublicado;

    // ── Botones de estado del proyecto ───────────────────────────────────────
    private Button btnEnPlanos, btnPreventa, btnEnVenta;

    // ── Botones de acción ────────────────────────────────────────────────────
    private Button      btnAgregarTipologia, btnAgregarAsesor;
    private Button      btnBuscarDireccion, btnAjustarUbicacion;
    private Button      btnGuardarProyecto,  btnCancelarCrear;
    private ImageButton btnBackCreateProject;
    private LinearLayout btnAddImage;

    // ── Contenedores dinámicos ────────────────────────────────────────────────
    private LinearLayout tipologiasContainer, asesoresContainer, imagesContainer;
    private ChipGroup    chipGroupAreas;
    private TextView     tvContadorImagenes;

    // ── Estado interno ────────────────────────────────────────────────────────
    private String                  selectedEstado = "";
    private AdminProyectoSessionData sessionData;
    private final List<Uri>         imagenesSeleccionadas = new ArrayList<>();
    private final List<Address>     sugerenciasDireccion = new ArrayList<>();
    private final List<AutocompletePrediction> sugerenciasPlaces = new ArrayList<>();
    private ArrayAdapter<String>    direccionesAdapter;
    private final Handler           direccionHandler = new Handler(Looper.getMainLooper());
    private Runnable                buscarDireccionRunnable;
    private GoogleMap               mapaUbicacion;
    private Coordenadas             coordenadasSeleccionadas;
    private boolean                 actualizandoDireccionDesdeSugerencia = false;
    private PlacesClient            placesClient;
    private AutocompleteSessionToken placesSessionToken;

    // Placeholder para el campo fecha (para validar que hay fecha real)
    private static final String FECHA_PLACEHOLDER    = "Seleccionar fecha";
    private static final String FECHA_NO_APLICA      = "No aplica para proyectos en venta";
    private static final String DISTRITO_PLACEHOLDER = "Seleccionar distrito";

    // ── Parte 3: Lista completa de distritos de Lima ──────────────────────────
    private static final String[] DISTRITOS_LIMA = {
        "Ate", "Barranco", "Breña", "Carabayllo", "Chorrillos", "Cieneguilla",
        "Comas", "El Agustino", "Independencia", "Jesús María", "La Molina",
        "La Victoria", "Lince", "Los Olivos", "Lurigancho", "Lurín",
        "Magdalena del Mar", "Miraflores", "Pachacámac", "Pucusana", "Pueblo Libre",
        "Puente Piedra", "Punta Hermosa", "Punta Negra", "Rímac", "San Bartolo",
        "San Borja", "San Isidro", "San Juan de Lurigancho", "San Juan de Miraflores",
        "San Luis", "San Martín de Porres", "San Miguel", "Santa Anita",
        "Santa María del Mar", "Santa Rosa", "Santiago de Surco", "Surquillo",
        "Villa El Salvador", "Villa María del Triunfo",
        // Callao
        "Callao", "Bellavista", "Carmen de La Legua", "La Perla", "La Punta",
        "Mi Perú", "Ventanilla"
    };

    // ── Ciclo de vida ─────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_crear_proyecto);

        sessionData = AdminProyectoSessionData.getInstance();

        initializeViews();
        restoreSessionData();
        setupListeners();
        inicializarPlaces();
        configurarAutocompletadoDireccion();
        configurarMapaUbicacion();
        initializeStateButtonColors();
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreSessionData();
        renderizarTipologias();   // Parte 1: mostrar tipologías guardadas en sesión
        renderizarAsesores();     // Parte 2: mostrar asesores guardados en sesión
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (buscarDireccionRunnable != null) {
            direccionHandler.removeCallbacks(buscarDireccionRunnable);
        }
        // No limpiar aquí — solo en cancelar o guardar
    }

    // ── Inicialización ────────────────────────────────────────────────────────

    private void initializeViews() {
        etNombreProyecto      = findViewById(R.id.etNombreProyecto);
        etDescripcionProyecto = findViewById(R.id.etDescripcionProyecto);
        tvFechaEntrega        = findViewById(R.id.tvFechaEntrega);
        etDireccion           = findViewById(R.id.etDireccion);

        // Parte 3: selector de distrito (no editable)
        tvDistrito             = findViewById(R.id.tvDistrito);
        layoutDistritoSelector = findViewById(R.id.layoutDistritoSelector);

        etCostoSeparacion     = findViewById(R.id.etCostoSeparacion);
        etPrecioTotal         = findViewById(R.id.etPrecioTotal);
        etNombreComercial     = findViewById(R.id.etNombreComercial);
        etPrecioPublicado     = findViewById(R.id.etPrecioPublicado);

        btnEnPlanos           = findViewById(R.id.btnEnPlanos);
        btnPreventa           = findViewById(R.id.btnPreventa);
        btnEnVenta            = findViewById(R.id.btnEnVenta);

        btnAgregarTipologia   = findViewById(R.id.btnAgregarTipologia);
        btnAgregarAsesor      = findViewById(R.id.btnAgregarAsesor);
        btnBuscarDireccion    = findViewById(R.id.btnBuscarDireccion);
        btnAjustarUbicacion   = findViewById(R.id.btnAjustarUbicacion);
        btnAddImage           = findViewById(R.id.btnAddImage);
        btnGuardarProyecto    = findViewById(R.id.btnGuardarProyecto);
        btnCancelarCrear      = findViewById(R.id.btnCancelarCrear);
        btnBackCreateProject  = findViewById(R.id.btnBackCreateProject);

        tipologiasContainer   = findViewById(R.id.tipologiasContainer);
        asesoresContainer     = findViewById(R.id.asesoresContainer);
        imagesContainer       = findViewById(R.id.imagesContainer);
        chipGroupAreas        = findViewById(R.id.chipGroupAreas);
        tvContadorImagenes    = findViewById(R.id.tvContadorImagenes);
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    private void inicializarPlaces() {
        String apiKey = getString(R.string.google_maps_key);
        if (TextUtils.isEmpty(apiKey)) {
            Log.w(TAG, "No hay API key para Places");
            return;
        }
        try {
            if (!Places.isInitialized()) {
                Places.initializeWithNewPlacesApiEnabled(getApplicationContext(), apiKey);
            }
            placesClient = Places.createClient(this);
        } catch (Exception e) {
            Log.w(TAG, "No se pudo inicializar Places: " + e.getMessage());
            placesClient = null;
        }
    }

    private void setupListeners() {
        // Botón atrás: sale sin confirmación
        btnBackCreateProject.setOnClickListener(v -> {
            sessionData.clear();
            finish();
        });

        // Fecha de entrega — abre DatePickerDialog
        tvFechaEntrega.setOnClickListener(v -> showDatePicker());

        // Estado del proyecto
        btnEnPlanos.setOnClickListener(v -> selectEstado("En planos", btnEnPlanos));
        btnPreventa.setOnClickListener(v -> selectEstado("Preventa",  btnPreventa));
        btnEnVenta.setOnClickListener(v  -> selectEstado("En venta",  btnEnVenta));

        // Parte 3: Selector de distrito con AlertDialog de búsqueda
        if (layoutDistritoSelector != null) {
            layoutDistritoSelector.setOnClickListener(v -> mostrarDialogoDistrito());
        }

        if (btnBuscarDireccion != null) {
            btnBuscarDireccion.setOnClickListener(v -> buscarDireccionDesdeBoton());
        }

        if (btnAjustarUbicacion != null) {
            btnAjustarUbicacion.setOnClickListener(v -> mostrarDialogoAjustarUbicacion());
        }

        // Tipología: guardar formulario → navegar
        btnAgregarTipologia.setOnClickListener(v -> {
            saveCurrentFormData();
            startActivityForResult(
                    new Intent(this, AdminAgregarTipologiaActivity.class), 100);
        });

        // Asesor: guardar formulario → navegar
        btnAgregarAsesor.setOnClickListener(v -> {
            saveCurrentFormData();
            startActivityForResult(
                    new Intent(this, AdminAsignarAsesoresActivity.class), 101);
        });

        // Galería: selección múltiple de imágenes
        btnAddImage.setOnClickListener(v -> {
            saveCurrentFormData();
            openGallery();
        });

        // Guardar proyecto (Parte 4)
        btnGuardarProyecto.setOnClickListener(v -> {
            saveCurrentFormData();
            if (coordenadasSeleccionadas == null && !textoSeguro(etDireccion).isEmpty()) {
                resolverDireccionAntesDeGuardar();
                return;
            }
            validarYGuardarProyecto();
        });

        // Cancelar: diálogo de confirmación
        btnCancelarCrear.setOnClickListener(v -> mostrarDialogoCancelar());
    }

    private void validarYGuardarProyecto() {
        saveCurrentFormData();
        if (!validarFormulario()) return;
        guardarProyecto();
    }

    private void buscarDireccionDesdeBoton() {
        if (textoSeguro(etDireccion).length() < 4) {
            mostrarToast("Ingresa una dirección para buscar");
            etDireccion.requestFocus();
            return;
        }

        if (btnBuscarDireccion != null) {
            btnBuscarDireccion.setEnabled(false);
            btnBuscarDireccion.setText("...");
        }

        resolverDireccionConCallback(() -> {
            if (btnBuscarDireccion != null) {
                btnBuscarDireccion.setEnabled(true);
                btnBuscarDireccion.setText("Buscar");
            }
            mostrarToast("Ubicación encontrada");
        }, () -> {
            if (btnBuscarDireccion != null) {
                btnBuscarDireccion.setEnabled(true);
                btnBuscarDireccion.setText("Buscar");
            }
            mostrarToast("No se pudo ubicar esa dirección");
        });
    }

    private void configurarMapaUbicacion() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapCrearProyecto);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            habilitarGestosMapaEnScroll(mapFragment);
        }
    }

    private void habilitarGestosMapaEnScroll(SupportMapFragment mapFragment) {
        View mapView = mapFragment.getView();
        if (mapView == null) return;
        mapView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_MOVE) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapaUbicacion = googleMap;
        LatLng lima = new LatLng(-12.0464, -77.0428);
        mapaUbicacion.moveCamera(CameraUpdateFactory.newLatLngZoom(lima, 11f));
        if (coordenadasSeleccionadas != null) {
            pintarUbicacionEnMapa(coordenadasSeleccionadas);
        }
    }

    private void configurarAutocompletadoDireccion() {
        if (etDireccion == null) return;

        direccionesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );
        etDireccion.setAdapter(direccionesAdapter);
        etDireccion.setThreshold(4);

        etDireccion.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < sugerenciasPlaces.size()) {
                aplicarPrediccionPlace(sugerenciasPlaces.get(position));
            } else if (position >= 0 && position < sugerenciasDireccion.size()) {
                aplicarDireccionSeleccionada(sugerenciasDireccion.get(position));
            }
        });

        etDireccion.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                resolverDireccionActual(false);
                return true;
            }
            return false;
        });

        etDireccion.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) resolverDireccionActual(false);
        });

        etDireccion.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (actualizandoDireccionDesdeSugerencia) return;
                programarBusquedaDireccion(s != null ? s.toString().trim() : "");
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void programarBusquedaDireccion(String texto) {
        if (buscarDireccionRunnable != null) {
            direccionHandler.removeCallbacks(buscarDireccionRunnable);
        }
        if (texto.length() < 4) return;

        buscarDireccionRunnable = () -> buscarSugerenciasDireccion(texto);
        direccionHandler.postDelayed(buscarDireccionRunnable, 650);
    }

    private void buscarSugerenciasDireccion(String texto) {
        if (placesClient != null) {
            buscarSugerenciasPlaces(texto);
            return;
        }

        buscarSugerenciasConGeocoder(texto);
    }

    private void buscarSugerenciasPlaces(String texto) {
        if (placesSessionToken == null) {
            placesSessionToken = AutocompleteSessionToken.newInstance();
        }

        FindAutocompletePredictionsRequest request =
                FindAutocompletePredictionsRequest.builder()
                        .setQuery(crearTextoBusquedaPlaces(texto))
                        .setCountries("PE")
                        .setLocationBias(RectangularBounds.newInstance(
                                new LatLng(-12.35, -77.20),
                                new LatLng(-11.70, -76.75)))
                        .setSessionToken(placesSessionToken)
                        .build();

        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener(response -> {
                    sugerenciasPlaces.clear();
                    sugerenciasDireccion.clear();
                    sugerenciasPlaces.addAll(response.getAutocompletePredictions());

                    if (direccionesAdapter == null) return;
                    direccionesAdapter.clear();
                    for (AutocompletePrediction prediction : sugerenciasPlaces) {
                        direccionesAdapter.add(prediction.getFullText(null).toString());
                    }
                    direccionesAdapter.notifyDataSetChanged();
                    if (!sugerenciasPlaces.isEmpty() && etDireccion != null && etDireccion.hasFocus()) {
                        etDireccion.showDropDown();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Places Autocomplete falló: " + e.getMessage());
                    buscarSugerenciasConGeocoder(texto);
                });
    }

    private String crearTextoBusquedaPlaces(String texto) {
        return texto + ", Lima, Perú";
    }

    private void buscarSugerenciasConGeocoder(String texto) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Address> resultados = obtenerDireccionesDesdeConsulta(
                    crearConsultaDireccion(texto, ""), 5);
            runOnUiThread(() -> {
                sugerenciasPlaces.clear();
                sugerenciasDireccion.clear();
                sugerenciasDireccion.addAll(resultados);
                if (direccionesAdapter == null) return;

                direccionesAdapter.clear();
                for (Address address : resultados) {
                    direccionesAdapter.add(formatearDireccionSugerida(address));
                }
                direccionesAdapter.notifyDataSetChanged();
                if (!resultados.isEmpty() && etDireccion != null && etDireccion.hasFocus()) {
                    etDireccion.showDropDown();
                }
            });
        });
    }

    private void aplicarPrediccionPlace(AutocompletePrediction prediction) {
        if (prediction == null || placesClient == null) return;

        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.DISPLAY_NAME,
                Place.Field.FORMATTED_ADDRESS,
                Place.Field.LOCATION,
                Place.Field.ADDRESS_COMPONENTS
        );

        FetchPlaceRequest request = FetchPlaceRequest.builder(prediction.getPlaceId(), fields)
                .setSessionToken(placesSessionToken)
                .build();

        placesClient.fetchPlace(request)
                .addOnSuccessListener(response -> {
                    Place place = response.getPlace();
                    aplicarPlaceSeleccionado(place, prediction);
                    placesSessionToken = null;
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "FetchPlace falló: " + e.getMessage());
                    actualizandoDireccionDesdeSugerencia = true;
                    etDireccion.setText(prediction.getFullText(null).toString(), false);
                    etDireccion.setSelection(etDireccion.getText() != null ? etDireccion.getText().length() : 0);
                    actualizandoDireccionDesdeSugerencia = false;
                    geocodificarDireccionActual(true);
                    placesSessionToken = null;
                });
    }

    private void resolverDireccionActual(boolean mostrarErrores) {
        if (etDireccion == null) return;
        String direccion = textoSeguro(etDireccion);
        if (direccion.length() < 4) return;

        if (placesClient == null) {
            geocodificarDireccionActual(mostrarErrores);
            return;
        }

        if (placesSessionToken == null) {
            placesSessionToken = AutocompleteSessionToken.newInstance();
        }

        FindAutocompletePredictionsRequest request =
                FindAutocompletePredictionsRequest.builder()
                        .setQuery(crearTextoBusquedaPlaces(direccion))
                        .setCountries("PE")
                        .setLocationBias(RectangularBounds.newInstance(
                                new LatLng(-12.35, -77.20),
                                new LatLng(-11.70, -76.75)))
                        .setSessionToken(placesSessionToken)
                        .build();

        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener(response -> {
                    List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
                    if (predictions == null || predictions.isEmpty()) {
                        geocodificarDireccionActual(mostrarErrores);
                        return;
                    }
                    aplicarPrediccionPlace(predictions.get(0));
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Resolver dirección con Places falló: " + e.getMessage());
                    geocodificarDireccionActual(mostrarErrores);
                });
    }

    private void resolverDireccionAntesDeGuardar() {
        btnGuardarProyecto.setEnabled(false);
        btnGuardarProyecto.setText("Ubicando...");

        resolverDireccionConCallback(() -> {
            btnGuardarProyecto.setEnabled(true);
            btnGuardarProyecto.setText("Guardar proyecto");
            validarYGuardarProyecto();
        }, () -> {
            btnGuardarProyecto.setEnabled(true);
            btnGuardarProyecto.setText("Guardar proyecto");
            mostrarToast("Selecciona una dirección válida para ubicar el proyecto en el mapa");
        });
    }

    private void resolverDireccionConCallback(Runnable onSuccess, Runnable onError) {
        if (etDireccion == null) {
            onError.run();
            return;
        }

        String direccion = textoSeguro(etDireccion);
        if (direccion.length() < 4) {
            onError.run();
            return;
        }

        if (placesClient == null) {
            geocodificarDireccionConCallback(onSuccess, onError);
            return;
        }

        if (placesSessionToken == null) {
            placesSessionToken = AutocompleteSessionToken.newInstance();
        }

        FindAutocompletePredictionsRequest request =
                FindAutocompletePredictionsRequest.builder()
                        .setQuery(crearTextoBusquedaPlaces(direccion))
                        .setCountries("PE")
                        .setLocationBias(RectangularBounds.newInstance(
                                new LatLng(-12.35, -77.20),
                                new LatLng(-11.70, -76.75)))
                        .setSessionToken(placesSessionToken)
                        .build();

        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener(response -> {
                    List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
                    if (predictions == null || predictions.isEmpty()) {
                        geocodificarDireccionConCallback(onSuccess, onError);
                        return;
                    }
                    aplicarPrediccionPlaceConCallback(predictions.get(0), onSuccess, onError);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Resolver dirección antes de guardar falló: " + e.getMessage());
                    geocodificarDireccionConCallback(onSuccess, onError);
                });
    }

    private void aplicarPrediccionPlaceConCallback(AutocompletePrediction prediction,
                                                   Runnable onSuccess,
                                                   Runnable onError) {
        if (prediction == null || placesClient == null) {
            onError.run();
            return;
        }

        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.DISPLAY_NAME,
                Place.Field.FORMATTED_ADDRESS,
                Place.Field.LOCATION,
                Place.Field.ADDRESS_COMPONENTS
        );

        FetchPlaceRequest request = FetchPlaceRequest.builder(prediction.getPlaceId(), fields)
                .setSessionToken(placesSessionToken)
                .build();

        placesClient.fetchPlace(request)
                .addOnSuccessListener(response -> {
                    aplicarPlaceSeleccionado(response.getPlace(), prediction);
                    if (coordenadasSeleccionadas != null) {
                        onSuccess.run();
                    } else {
                        onError.run();
                    }
                    placesSessionToken = null;
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "FetchPlace antes de guardar falló: " + e.getMessage());
                    placesSessionToken = null;
                    geocodificarDireccionConCallback(onSuccess, onError);
                });
    }

    private void geocodificarDireccionConCallback(Runnable onSuccess, Runnable onError) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Address> resultados = obtenerDireccionesDesdeConsulta(
                    crearConsultaDireccion(textoSeguro(etDireccion), ""), 1);
            Address address = resultados.isEmpty() ? null : resultados.get(0);
            runOnUiThread(() -> {
                if (address == null) {
                    onError.run();
                    return;
                }
                aplicarDireccionSeleccionada(address);
                if (coordenadasSeleccionadas != null) {
                    onSuccess.run();
                } else {
                    onError.run();
                }
            });
        });
    }

    private void aplicarPlaceSeleccionado(Place place, AutocompletePrediction prediction) {
        if (place == null || etDireccion == null) return;

        String direccion = place.getFormattedAddress();
        if (direccion == null || direccion.trim().isEmpty()) {
            direccion = prediction.getFullText(null).toString();
        }

        actualizandoDireccionDesdeSugerencia = true;
        etDireccion.setText(limpiarDireccionPeru(direccion), false);
        etDireccion.setSelection(etDireccion.getText() != null ? etDireccion.getText().length() : 0);
        actualizandoDireccionDesdeSugerencia = false;

        String distrito = extraerDistrito(place);
        if (!distrito.isEmpty() && distritoDebeAutocompletarse()) {
            actualizarDistritoSeleccionado(distrito);
        }

        LatLng latLng = place.getLocation();
        if (latLng != null) {
            coordenadasSeleccionadas = new Coordenadas(latLng.latitude, latLng.longitude);
            pintarUbicacionEnMapa(coordenadasSeleccionadas);
        } else {
            geocodificarDireccionActual(true);
        }

        saveCurrentFormData();
    }

    private void aplicarDireccionSeleccionada(Address address) {
        if (address == null || etDireccion == null) return;

        String direccion = extraerDireccionLegible(address);
        actualizandoDireccionDesdeSugerencia = true;
        etDireccion.setText(direccion, false);
        etDireccion.setSelection(etDireccion.getText() != null ? etDireccion.getText().length() : 0);
        actualizandoDireccionDesdeSugerencia = false;

        String distrito = extraerDistrito(address);
        if (!distrito.isEmpty() && distritoDebeAutocompletarse()) {
            actualizarDistritoSeleccionado(distrito);
        }

        coordenadasSeleccionadas = new Coordenadas(address.getLatitude(), address.getLongitude());
        pintarUbicacionEnMapa(coordenadasSeleccionadas);
        saveCurrentFormData();
    }

    private void geocodificarDireccionActual(boolean mostrarErrores) {
        if (etDireccion == null) return;
        String direccion = textoSeguro(etDireccion);
        if (direccion.length() < 4) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Address> resultados = obtenerDireccionesDesdeConsulta(
                    crearConsultaDireccion(direccion, ""), 1);
            Address address = resultados.isEmpty() ? null : resultados.get(0);
            runOnUiThread(() -> {
                if (address == null) {
                    if (mostrarErrores) mostrarToast("No se pudo ubicar esa dirección");
                    return;
                }
                aplicarDireccionSeleccionada(address);
            });
        });
    }

    private List<Address> obtenerDireccionesDesdeConsulta(String consulta, int maxResultados) {
        try {
            if (!Geocoder.isPresent()) return new ArrayList<>();
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> resultados = geocoder.getFromLocationName(consulta, maxResultados);
            return resultados != null ? resultados : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String crearConsultaDireccion(String direccion, String distrito) {
        return direccion + ", Lima, Perú";
    }

    private String distritoActualSeleccionado() {
        if (tvDistrito == null || tvDistrito.getText() == null) return "";
        String distrito = tvDistrito.getText().toString().trim();
        return DISTRITO_PLACEHOLDER.equals(distrito) ? "" : distrito;
    }

    private boolean distritoDebeAutocompletarse() {
        return distritoActualSeleccionado().isEmpty();
    }

    private String formatearDireccionSugerida(Address address) {
        String linea = address.getMaxAddressLineIndex() >= 0 ? address.getAddressLine(0) : "";
        if (linea == null || linea.trim().isEmpty()) {
            String feature = address.getFeatureName();
            return feature != null ? feature : "";
        }
        return linea.replace(", Peru", "").replace(", Perú", "");
    }

    private String extraerDireccionLegible(Address address) {
        String thoroughfare = address.getThoroughfare();
        String numero = address.getSubThoroughfare();
        if (thoroughfare != null && !thoroughfare.trim().isEmpty()) {
            return numero != null && !numero.trim().isEmpty()
                    ? thoroughfare + " " + numero
                    : thoroughfare;
        }
        String linea = formatearDireccionSugerida(address);
        return linea != null ? linea : "";
    }

    private String extraerDistrito(Address address) {
        List<String> candidatos = Arrays.asList(
                address.getSubLocality(),
                address.getLocality(),
                address.getSubAdminArea(),
                address.getAdminArea(),
                address.getMaxAddressLineIndex() >= 0 ? address.getAddressLine(0) : ""
        );
        for (String candidato : candidatos) {
            String distrito = buscarDistritoConocido(candidato);
            if (!distrito.isEmpty()) return distrito;
        }
        return "";
    }

    private String extraerDistrito(Place place) {
        if (place.getAddressComponents() == null) return "";
        for (AddressComponent component : place.getAddressComponents().asList()) {
            List<String> tipos = component.getTypes();
            String nombre = component.getName();
            if (tipos == null || nombre == null) continue;
            if (tipos.contains("sublocality")
                    || tipos.contains("sublocality_level_1")
                    || tipos.contains("locality")
                    || tipos.contains("administrative_area_level_3")
                    || tipos.contains("administrative_area_level_2")) {
                String distrito = buscarDistritoConocido(nombre);
                if (!distrito.isEmpty()) return distrito;
            }
        }
        String direccion = place.getFormattedAddress();
        return buscarDistritoConocido(direccion);
    }

    private String limpiarDireccionPeru(String direccion) {
        if (direccion == null) return "";
        return direccion
                .replace(", Peru", "")
                .replace(", Perú", "")
                .trim();
    }

    private String buscarDistritoConocido(String texto) {
        if (texto == null || texto.trim().isEmpty()) return "";
        String normalizado = normalizarTexto(texto);
        for (String distrito : DISTRITOS_LIMA) {
            if (normalizado.contains(normalizarTexto(distrito))) {
                return distrito;
            }
        }
        return "";
    }

    private String normalizarTexto(String valor) {
        String sinTildes = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return sinTildes.toLowerCase(Locale.getDefault()).trim();
    }

    private void actualizarDistritoSeleccionado(String distrito) {
        if (tvDistrito == null || distrito == null || distrito.trim().isEmpty()) return;
        tvDistrito.setText(distrito.trim());
        tvDistrito.setTextColor(Color.BLACK);
        sessionData.distrito = distrito.trim();
    }

    private void pintarUbicacionEnMapa(Coordenadas coordenadas) {
        if (mapaUbicacion == null || coordenadas == null) return;
        LatLng punto = new LatLng(coordenadas.latitud, coordenadas.longitud);
        mapaUbicacion.clear();
        mapaUbicacion.addMarker(new MarkerOptions()
                .position(punto)
                .title(textoSeguro(etNombreProyecto).isEmpty()
                        ? "Ubicación del proyecto"
                        : textoSeguro(etNombreProyecto)));
        mapaUbicacion.animateCamera(CameraUpdateFactory.newLatLngZoom(punto, 16f));
    }

    private void mostrarDialogoAjustarUbicacion() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        TextView titulo = new TextView(this);
        titulo.setText("Ajustar ubicación");
        titulo.setTextColor(ContextCompat.getColor(this, R.color.brand_deep_blue));
        titulo.setTextSize(18f);
        titulo.setTypeface(null, android.graphics.Typeface.BOLD);
        titulo.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(8));
        root.addView(titulo);

        TextView ayuda = new TextView(this);
        ayuda.setText("Mueve el mapa hasta que el pin quede sobre el punto exacto.");
        ayuda.setTextColor(ContextCompat.getColor(this, R.color.neutral_dark));
        ayuda.setTextSize(12f);
        ayuda.setPadding(dpToPx(16), 0, dpToPx(16), dpToPx(8));
        root.addView(ayuda);

        FrameLayout mapaContainer = new FrameLayout(this);
        LinearLayout.LayoutParams mapParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        mapaContainer.setLayoutParams(mapParams);

        MapView mapView = new MapView(this);
        mapView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        mapaContainer.addView(mapView);

        ImageView pinCentro = new ImageView(this);
        int pinSize = dpToPx(44);
        FrameLayout.LayoutParams pinParams = new FrameLayout.LayoutParams(pinSize, pinSize);
        pinParams.gravity = Gravity.CENTER;
        pinParams.bottomMargin = dpToPx(22);
        pinCentro.setLayoutParams(pinParams);
        pinCentro.setImageResource(R.drawable.ic_location_pin);
        pinCentro.setColorFilter(ContextCompat.getColor(this, R.color.brand_deep_blue));
        pinCentro.setContentDescription("Pin de ubicación");
        mapaContainer.addView(pinCentro);

        root.addView(mapaContainer);

        LinearLayout acciones = new LinearLayout(this);
        acciones.setOrientation(LinearLayout.HORIZONTAL);
        acciones.setGravity(Gravity.CENTER_VERTICAL);
        acciones.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(16));

        Button btnCancelar = new Button(this);
        btnCancelar.setText("Cancelar");
        btnCancelar.setTextColor(ContextCompat.getColor(this, R.color.brand_deep_blue));
        btnCancelar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
        acciones.addView(btnCancelar, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Button btnUsar = new Button(this);
        btnUsar.setText("Usar ubicación");
        btnUsar.setTextColor(Color.WHITE);
        btnUsar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.brand_deep_blue)));
        acciones.addView(btnUsar, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        root.addView(acciones);

        final Coordenadas[] seleccion = {
                coordenadasSeleccionadas != null
                        ? coordenadasSeleccionadas
                        : new Coordenadas(-12.0464, -77.0428)
        };

        mapView.onCreate(null);
        mapView.onResume();
        mapView.getMapAsync(map -> {
            LatLng inicial = new LatLng(seleccion[0].latitud, seleccion[0].longitud);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(inicial,
                    coordenadasSeleccionadas != null ? 17f : 11f));
            map.setOnCameraIdleListener(() -> {
                LatLng centro = map.getCameraPosition().target;
                seleccion[0] = new Coordenadas(centro.latitude, centro.longitude);
            });
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnUsar.setOnClickListener(v -> {
            coordenadasSeleccionadas = seleccion[0];
            pintarUbicacionEnMapa(coordenadasSeleccionadas);
            mostrarToast("Ubicación ajustada");
            dialog.dismiss();
        });

        dialog.setContentView(root);
        dialog.setOnDismissListener(d -> {
            mapView.onPause();
            mapView.onDestroy();
        });
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
        }
        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
        }
    }

    private void initializeStateButtonColors() {
        int deepBlue = ContextCompat.getColor(this, R.color.brand_deep_blue);
        btnEnPlanos.setTextColor(deepBlue);
        btnPreventa.setTextColor(deepBlue);
        btnEnVenta.setTextColor(deepBlue);
        btnEnPlanos.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnPreventa.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnEnVenta.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
    }

    private void selectEstado(String estado, Button selectedButton) {
        selectedEstado     = estado;
        sessionData.estado = estado;

        int deepBlue = ContextCompat.getColor(this, R.color.brand_deep_blue);
        btnEnPlanos.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnEnPlanos.setTextColor(deepBlue);
        btnPreventa.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnPreventa.setTextColor(deepBlue);
        btnEnVenta.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnEnVenta.setTextColor(deepBlue);

        selectedButton.setBackgroundColor(deepBlue);
        selectedButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));

        actualizarDisponibilidadFechaEntrega();
    }

    private void actualizarDisponibilidadFechaEntrega() {
        if (tvFechaEntrega == null) return;
        boolean requiereFecha = !"En venta".equals(selectedEstado);
        tvFechaEntrega.setEnabled(requiereFecha);
        tvFechaEntrega.setAlpha(requiereFecha ? 1f : 0.55f);
        if (!requiereFecha) {
            tvFechaEntrega.setText(FECHA_NO_APLICA);
            tvFechaEntrega.setTextColor(ContextCompat.getColor(this, R.color.neutral_medium));
            sessionData.fechaEntrega = "";
        } else if (sessionData.fechaEntrega.isEmpty()) {
            tvFechaEntrega.setText(FECHA_PLACEHOLDER);
            tvFechaEntrega.setTextColor(ContextCompat.getColor(this, R.color.neutral_medium));
        }
    }

    // ── Parte 3: Diálogo selector de distrito con búsqueda ───────────────────

    /**
     * Muestra un AlertDialog con:
     *  - EditText para filtrar distritos en tiempo real
     *  - ListView con la lista filtrada
     *  - Botón "Cancelar" que cierra sin seleccionar
     * Al tocar un ítem → cierra y muestra el distrito en el campo.
     */
    private void mostrarDialogoDistrito() {
        // Lista mutable para filtrar
        final List<String> listaFiltrada = new ArrayList<>(Arrays.asList(DISTRITOS_LIMA));

        // Construir vista del diálogo programáticamente
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = dpToPx(16);
        layout.setPadding(pad, dpToPx(8), pad, 0);

        // Campo de búsqueda
        EditText etBusqueda = new EditText(this);
        etBusqueda.setHint("Buscar distrito...");
        etBusqueda.setSingleLine(true);
        LinearLayout.LayoutParams etParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        etParams.bottomMargin = dpToPx(8);
        etBusqueda.setLayoutParams(etParams);
        layout.addView(etBusqueda);

        // ListView con los distritos
        ListView listView = new ListView(this);
        listView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(280)));
        layout.addView(listView);

        // Adapter del ListView
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, listaFiltrada);
        listView.setAdapter(arrayAdapter);

        // Crear el diálogo
        final AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Seleccionar distrito")
                .setView(layout)
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .create();

        // Filtro en tiempo real al escribir en el buscador
        etBusqueda.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String filtro = s.toString().toLowerCase(Locale.getDefault()).trim();
                listaFiltrada.clear();
                for (String d : DISTRITOS_LIMA) {
                    if (d.toLowerCase(Locale.getDefault()).contains(filtro)) {
                        listaFiltrada.add(d);
                    }
                }
                arrayAdapter.notifyDataSetChanged();
            }
        });

        // Al seleccionar un distrito → actualizar campo y guardar en sesión
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String distritoSeleccionado = listaFiltrada.get(position);
            actualizarDistritoSeleccionado(distritoSeleccionado);
            if (!textoSeguro(etDireccion).isEmpty()) {
                resolverDireccionActual(false);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    // ── Corrección 4: DatePicker con fecha mínima = hoy ───────────────────────

    /**
     * Abre el DatePickerDialog con la fecha actual como mínima.
     * Al seleccionar → muestra la fecha en negro y la guarda en la sesión.
     */
    private void showDatePicker() {
        if ("En venta".equals(selectedEstado)) {
            mostrarToast("Los proyectos en venta no requieren fecha estimada de entrega");
            return;
        }

        Calendar hoy = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar seleccionado = Calendar.getInstance();
                    seleccionado.set(year, month, dayOfMonth);

                    String fechaFormateada = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(seleccionado.getTime());
                    tvFechaEntrega.setText(fechaFormateada);
                    tvFechaEntrega.setTextColor(Color.BLACK);
                    sessionData.fechaEntrega = fechaFormateada;
                },
                hoy.get(Calendar.YEAR),
                hoy.get(Calendar.MONTH),
                hoy.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(hoy.getTimeInMillis());
        dialog.show();
    }

    // ── Parte 1: Renderizar tipologías desde la sesión ────────────────────────

    /**
     * Lee AdminProyectoSessionData.tipologias y renderiza cada una como
     * una MaterialCardView en tipologiasContainer.
     * Si no hay tipologías, muestra "Sin tipologías agregadas".
     */
    private void renderizarTipologias() {
        if (tipologiasContainer == null) return;
        tipologiasContainer.removeAllViews();

        List<Tipologia> lista = sessionData.tipologias;

        if (lista == null || lista.isEmpty()) {
            // Estado vacío
            TextView tvVacio = new TextView(this);
            tvVacio.setText("Sin tipologías agregadas");
            tvVacio.setTextColor(ContextCompat.getColor(this, R.color.neutral_medium));
            tvVacio.setTextSize(12f);
            tvVacio.setPadding(0, 0, 0, dpToPx(8));
            tipologiasContainer.addView(tvVacio);
            return;
        }

        for (int i = 0; i < lista.size(); i++) {
            final int index  = i;
            Tipologia tip    = lista.get(i);
            View cardView    = crearCardTipologia(tip, index);
            tipologiasContainer.addView(cardView);
        }
    }

    /**
     * Crea una card visual para una tipología con nombre, dormitorios, baños,
     * metraje, precio y un botón X para eliminarla.
     */
    private View crearCardTipologia(final Tipologia tip, final int index) {
        // Crear MaterialCardView programáticamente
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dpToPx(8);
        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(8));
        card.setCardElevation(dpToPx(1));

        // Fila interior: miniatura + datos + botón X
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(android.view.Gravity.CENTER_VERTICAL);
        fila.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));

        // Miniatura de la tipología (si tiene imagen guardada)
        String tipImagePath = tip.getImageUri();
        if (!tipImagePath.isEmpty()) {
            ImageView imgTip = new ImageView(this);
            int imgSize = dpToPx(52);
            LinearLayout.LayoutParams imgParams =
                    new LinearLayout.LayoutParams(imgSize, imgSize);
            imgParams.setMarginEnd(dpToPx(10));
            imgTip.setLayoutParams(imgParams);
            imgTip.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imgTip.setClipToOutline(true);
            GradientDrawable imgBg = new GradientDrawable();
            imgBg.setShape(GradientDrawable.RECTANGLE);
            imgBg.setCornerRadius(dpToPx(6));
            imgBg.setColor(0xFFE0E0E0);
            imgTip.setBackground(imgBg);
            Glide.with(this)
                    .load(esUrlRemota(tipImagePath) ? tipImagePath : new File(tipImagePath))
                    .centerCrop()
                    .into(imgTip);
            fila.addView(imgTip);
        }

        // Columna izquierda: nombre + detalles
        LinearLayout colDatos = new LinearLayout(this);
        colDatos.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams colParams =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        colDatos.setLayoutParams(colParams);

        TextView tvNombre = new TextView(this);
        tvNombre.setText(tip.getNombre() + " – " + tip.getDormitorios() + " dorm.");
        tvNombre.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        tvNombre.setTextSize(13f);
        tvNombre.setTypeface(null, android.graphics.Typeface.BOLD);
        colDatos.addView(tvNombre);

        // Formato precio: S/ 280,000
        String precioFmt = String.format(Locale.getDefault(), "S/ %,.0f", tip.getPrecioTotal());
        TextView tvDetalles = new TextView(this);
        tvDetalles.setText(String.format(Locale.getDefault(),
                "%.1f m² · %d baño%s · %s",
                tip.getMetraje(), tip.getBanos(),
                tip.getBanos() > 1 ? "s" : "",
                precioFmt));
        tvDetalles.setTextColor(ContextCompat.getColor(this, R.color.neutral_dark));
        tvDetalles.setTextSize(11f);
        colDatos.addView(tvDetalles);

        fila.addView(colDatos);

        // Botón X para eliminar esta tipología
        TextView btnX = new TextView(this);
        btnX.setText("×");
        btnX.setTextSize(18f);
        btnX.setTextColor(Color.WHITE);
        btnX.setGravity(android.view.Gravity.CENTER);
        int xSize = dpToPx(28);
        LinearLayout.LayoutParams xParams =
                new LinearLayout.LayoutParams(xSize, xSize);
        btnX.setLayoutParams(xParams);
        btnX.setBackgroundColor(Color.parseColor("#CCDD0000")); // rojo semitransparente
        GradientDrawable xBg = new GradientDrawable();
        xBg.setShape(GradientDrawable.RECTANGLE);
        xBg.setCornerRadius(dpToPx(4));
        xBg.setColor(Color.parseColor("#CC990000"));
        btnX.setBackground(xBg);

        btnX.setOnClickListener(v -> {
            // Eliminar la tipología de la sesión y re-renderizar
            sessionData.tipologias.remove(tip);
            renderizarTipologias();
        });

        fila.addView(btnX);
        card.addView(fila);
        return card;
    }

    // ── Parte 2: Renderizar asesores desde la sesión ──────────────────────────

    /**
     * Lee AdminProyectoSessionData.asesoresAsignados y renderiza cada nombre
     * como un Chip con botón X en asesoresContainer.
     * Si no hay asesores, muestra "Sin asesores asignados".
     */
    private void renderizarAsesores() {
        if (asesoresContainer == null) return;
        asesoresContainer.removeAllViews();

        List<String> lista = sessionData.asesoresAsignados;

        if (lista == null || lista.isEmpty()) {
            // Estado vacío
            TextView tvVacio = new TextView(this);
            tvVacio.setText("Sin asesores asignados");
            tvVacio.setTextColor(ContextCompat.getColor(this, R.color.neutral_medium));
            tvVacio.setTextSize(12f);
            tvVacio.setPadding(0, 0, 0, dpToPx(8));
            asesoresContainer.addView(tvVacio);
            return;
        }

        // Contenedor de chips en flujo horizontal con wrapping
        LinearLayout chipRow = new LinearLayout(this);
        chipRow.setOrientation(LinearLayout.HORIZONTAL);
        // Nota: LinearLayout no hace wrap automático; para muchos asesores usar
        // un LinearLayout vertical con chips apilados horizontalmente en pares,
        // o un FlowLayout. Aquí usamos vertical para simplicidad.
        LinearLayout chipContainer = new LinearLayout(this);
        chipContainer.setOrientation(LinearLayout.VERTICAL);
        chipContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Agregar un LinearLayout horizontal por cada par de asesores
        LinearLayout filaActual = null;
        for (int i = 0; i < lista.size(); i++) {
            final String nombreAsesor = lista.get(i);

            if (i % 2 == 0) {
                // Nueva fila cada 2 asesores
                filaActual = new LinearLayout(this);
                filaActual.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams filaParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                filaParams.bottomMargin = dpToPx(4);
                filaActual.setLayoutParams(filaParams);
                chipContainer.addView(filaActual);
            }

            // Crear chip para este asesor
            Chip chip = new Chip(this);
            chip.setText(nombreAsesor);
            chip.setCloseIconVisible(true);
            chip.setChipBackgroundColorResource(R.color.brand_deep_blue);
            chip.setTextColor(Color.WHITE);
            chip.setCloseIconTint(
                    android.content.res.ColorStateList.valueOf(Color.WHITE));
            LinearLayout.LayoutParams chipParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            chipParams.setMarginEnd(dpToPx(8));
            chip.setLayoutParams(chipParams);

            // X del chip elimina el asesor de la sesión
            chip.setOnCloseIconClickListener(v -> {
                sessionData.asesoresAsignados.remove(nombreAsesor);
                renderizarAsesores();
            });

            if (filaActual != null) filaActual.addView(chip);
        }

        asesoresContainer.addView(chipContainer);
    }

    // ── Galería con selección múltiple ────────────────────────────────────────

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar imágenes"), 1001);
    }

    /**
     * Agrega una miniatura 80×80dp al contenedor horizontal con botón X.
     * Usa Glide para cargar la imagen de forma asíncrona y eficiente.
     */
    private void agregarMiniatura(final Uri uri) {
        int size   = dpToPx(80);
        int margen = dpToPx(8);
        int radio  = dpToPx(8);

        FrameLayout frame = new FrameLayout(this);
        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(size, size);
        frameParams.setMargins(0, 0, margen, 0);
        frame.setLayoutParams(frameParams);

        // ImageView con esquinas redondeadas
        ImageView imgView = new ImageView(this);
        imgView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgView.setClipToOutline(true);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(radio);
        bg.setColor(0xFFE0E0E0);
        imgView.setBackground(bg);
        Glide.with(this).load(uri).centerCrop().into(imgView);

        // Botón × superpuesto en esquina superior derecha
        TextView btnX = new TextView(this);
        int xSize = dpToPx(22);
        FrameLayout.LayoutParams xParams = new FrameLayout.LayoutParams(xSize, xSize);
        xParams.gravity = Gravity.TOP | Gravity.END;
        btnX.setLayoutParams(xParams);
        btnX.setText("×");
        btnX.setTextColor(Color.WHITE);
        btnX.setGravity(Gravity.CENTER);
        btnX.setTextSize(14f);
        btnX.setBackgroundColor(0xCCDD0000);

        frame.addView(imgView);
        frame.addView(btnX);

        btnX.setOnClickListener(v -> {
            imagenesSeleccionadas.remove(uri);
            imagesContainer.removeView(frame);
            actualizarContadorImagenes();
        });

        imagesContainer.addView(frame);
    }

    private void actualizarContadorImagenes() {
        if (tvContadorImagenes == null) return;
        int total = imagenesSeleccionadas.size();
        String sufijo = (total != 1) ? "es seleccionadas" : " seleccionada";
        tvContadorImagenes.setText(total + " imagen" + sufijo);
    }

    // ── Validación del formulario ─────────────────────────────────────────────

    /**
     * Valida todos los campos requeridos. Muestra un Toast específico ante el
     * primer campo inválido. Se llama solo al intentar guardar.
     *
     * @return true si el formulario es válido; false si hay algún error.
     */
    private boolean validarFormulario() {
        // 1. Nombre del proyecto
        String nombre = textoSeguro(etNombreProyecto);
        if (nombre.isEmpty()) {
            mostrarToast("Por favor ingresa el nombre del proyecto");
            etNombreProyecto.requestFocus();
            return false;
        }

        // 2. Descripción
        String descripcion = textoSeguro(etDescripcionProyecto);
        if (descripcion.isEmpty()) {
            mostrarToast("Por favor ingresa una descripción del proyecto");
            etDescripcionProyecto.requestFocus();
            return false;
        }

        // 3. Estado
        if (selectedEstado.isEmpty()) {
            mostrarToast("Por favor selecciona el estado del proyecto");
            return false;
        }

        // 4. Fecha de entrega: solo aplica para planos y preventa.
        if (!"En venta".equals(selectedEstado)) {
            String fecha = tvFechaEntrega.getText() != null
                    ? tvFechaEntrega.getText().toString().trim() : "";
            if (fecha.isEmpty() || FECHA_PLACEHOLDER.equals(fecha) || FECHA_NO_APLICA.equals(fecha)) {
                mostrarToast("Por favor selecciona la fecha de entrega del proyecto");
                return false;
            }
        }

        // 5. Dirección
        String direccion = textoSeguro(etDireccion);
        if (direccion.isEmpty()) {
            mostrarToast("Por favor ingresa la dirección del proyecto");
            etDireccion.requestFocus();
            return false;
        }

        // 6. Distrito: puede venir del selector o autocompletarse al elegir dirección.
        String distrito = tvDistrito != null && tvDistrito.getText() != null
                ? tvDistrito.getText().toString().trim() : "";
        if (distrito.isEmpty() || DISTRITO_PLACEHOLDER.equals(distrito)) {
            mostrarToast("Selecciona un distrito o una dirección sugerida del mapa");
            return false;
        }

        if (coordenadasSeleccionadas == null) {
            mostrarToast("Selecciona una dirección válida para ubicar el proyecto en el mapa");
            return false;
        }

        // 7. Costo de separación
        String costoStr = textoSeguro(etCostoSeparacion);
        if (costoStr.isEmpty()) {
            mostrarToast("Por favor ingresa el costo de separación");
            etCostoSeparacion.requestFocus();
            return false;
        }
        try {
            double costo = Double.parseDouble(costoStr);
            if (costo <= 0) {
                mostrarToast("El costo de separación debe ser mayor a 0");
                etCostoSeparacion.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarToast("El costo de separación debe ser un número válido");
            etCostoSeparacion.requestFocus();
            return false;
        }

        // 8. Precio total
        String precioStr = textoSeguro(etPrecioTotal);
        if (precioStr.isEmpty()) {
            mostrarToast("Por favor ingresa el precio total del inmueble");
            etPrecioTotal.requestFocus();
            return false;
        }
        try {
            double precio = Double.parseDouble(precioStr);
            if (precio <= 0) {
                mostrarToast("El precio total debe ser mayor a 0");
                etPrecioTotal.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarToast("El precio total debe ser un número válido");
            etPrecioTotal.requestFocus();
            return false;
        }

        // 9. Imágenes (mínimo 2)
        if (imagenesSeleccionadas.size() < 2) {
            mostrarToast("Debes seleccionar al menos 2 imágenes del proyecto");
            return false;
        }

        return true; // ✅ todos los campos son válidos
    }

    // ── Parte 4: Guardar proyecto en el repositorio ───────────────────────────

    /**
     * Crea un AdminProyecto con todos los datos del formulario y la sesión,
     * lo agrega a AdminProyectosRepository, lanza la notificación y termina.
     */
    private void guardarProyecto() {
        btnGuardarProyecto.setEnabled(false);
        btnGuardarProyecto.setText("Guardando...");

        // Generar ID único
        String nuevoId = UUID.randomUUID().toString();

        // Timestamp de creación
        String fechaCreacion = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        // Generar QR del proyecto y guardar en almacenamiento interno
        String qrPath = generarYGuardarQR(nuevoId,
                "inmobiliaria://proyecto/" + nuevoId);

        subirArchivosProyecto(nuevoId, qrPath, new SubidaArchivosCallback() {
            @Override
            public void onSuccess(List<String> imagenesUrls, String qrUrl) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    Coordenadas coordenadas = coordenadasSeleccionadas != null
                            ? coordenadasSeleccionadas
                            : obtenerCoordenadasDesdeDireccion(sessionData.direccion, sessionData.distrito);
                    runOnUiThread(() -> guardarProyectoConCoordenadas(
                            nuevoId, imagenesUrls, fechaCreacion,
                            qrUrl != null && !qrUrl.isEmpty() ? qrUrl : qrPath,
                            coordenadas));
                });
            }

            @Override
            public void onError(String mensaje) {
                btnGuardarProyecto.setEnabled(true);
                btnGuardarProyecto.setText("Guardar proyecto");
                mostrarToast("No se pudieron subir los archivos: " + mensaje);
            }
        });
    }

    private interface SubidaArchivosCallback {
        void onSuccess(List<String> imagenesUrls, String qrUrl);
        void onError(String mensaje);
    }

    private void subirArchivosProyecto(String proyectoId, String qrPath,
                                       SubidaArchivosCallback callback) {
        StorageReference root;
        try {
            root = FirebaseStorage.getInstance().getReference()
                    .child("proyectos")
                    .child(proyectoId);
        } catch (Exception e) {
            callback.onError("Firebase Storage no está configurado: " + e.getMessage());
            return;
        }

        List<Tipologia> tipologiasConImagen = obtenerTipologiasConImagenLocal();
        int totalSubidas = imagenesSeleccionadas.size()
                + tipologiasConImagen.size()
                + (qrPath != null && !qrPath.isEmpty() ? 1 : 0);
        if (totalSubidas == 0) {
            callback.onError("No hay archivos para subir");
            return;
        }

        String[] imagenesUrls = new String[imagenesSeleccionadas.size()];
        final String[] qrUrl = {""};
        AtomicInteger pendientes = new AtomicInteger(totalSubidas);
        AtomicBoolean finalizado = new AtomicBoolean(false);

        Runnable completarSiTermino = () -> {
            if (pendientes.decrementAndGet() != 0 || !finalizado.compareAndSet(false, true)) {
                return;
            }
            callback.onSuccess(new ArrayList<>(Arrays.asList(imagenesUrls)), qrUrl[0]);
        };

        for (int i = 0; i < imagenesSeleccionadas.size(); i++) {
            final int index = i;
            Uri uri = imagenesSeleccionadas.get(i);
            StorageReference ref = root.child("imagenes")
                    .child("imagen_" + index + "_" + UUID.randomUUID() + ".jpg");
            ref.putFile(uri)
                    .addOnSuccessListener(task -> ref.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                imagenesUrls[index] = downloadUri.toString();
                                completarSiTermino.run();
                            })
                            .addOnFailureListener(e -> {
                                if (finalizado.compareAndSet(false, true)) {
                                    callback.onError(e.getMessage());
                                }
                            }))
                    .addOnFailureListener(e -> {
                        if (finalizado.compareAndSet(false, true)) {
                            callback.onError(e.getMessage());
                        }
                    });
        }

        for (int i = 0; i < tipologiasConImagen.size(); i++) {
            final int index = i;
            Tipologia tipologia = tipologiasConImagen.get(i);
            Uri uri = crearUriArchivoTipologia(tipologia.getImageUri());
            if (uri == null) {
                if (finalizado.compareAndSet(false, true)) {
                    callback.onError("Imagen inválida en tipología " + tipologia.getNombre());
                }
                return;
            }
            StorageReference ref = root.child("tipologias")
                    .child("tipologia_" + index + "_" + UUID.randomUUID() + ".jpg");
            ref.putFile(uri)
                    .addOnSuccessListener(task -> ref.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                tipologia.setImageUri(downloadUri.toString());
                                completarSiTermino.run();
                            })
                            .addOnFailureListener(e -> {
                                if (finalizado.compareAndSet(false, true)) {
                                    callback.onError(e.getMessage());
                                }
                            }))
                    .addOnFailureListener(e -> {
                        if (finalizado.compareAndSet(false, true)) {
                            callback.onError(e.getMessage());
                        }
                    });
        }

        if (qrPath != null && !qrPath.isEmpty()) {
            File qrFile = new File(qrPath);
            StorageReference qrRef = root.child("qr").child("qr.png");
            qrRef.putFile(Uri.fromFile(qrFile))
                    .addOnSuccessListener(task -> qrRef.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                qrUrl[0] = downloadUri.toString();
                                completarSiTermino.run();
                            })
                            .addOnFailureListener(e -> {
                                if (finalizado.compareAndSet(false, true)) {
                                    callback.onError(e.getMessage());
                                }
                            }))
                    .addOnFailureListener(e -> {
                        if (finalizado.compareAndSet(false, true)) {
                            callback.onError(e.getMessage());
                        }
                    });
        }
    }

    private List<Tipologia> obtenerTipologiasConImagenLocal() {
        List<Tipologia> resultado = new ArrayList<>();
        if (sessionData == null || sessionData.tipologias == null) return resultado;
        for (Tipologia tipologia : sessionData.tipologias) {
            if (tipologia == null) continue;
            String imageUri = tipologia.getImageUri();
            if (!imageUri.isEmpty() && !esUrlRemota(imageUri)) {
                resultado.add(tipologia);
            }
        }
        return resultado;
    }

    private Uri crearUriArchivoTipologia(String valor) {
        if (valor == null || valor.trim().isEmpty()) return null;
        if (valor.startsWith("/")) return Uri.fromFile(new File(valor));
        return Uri.parse(valor);
    }

    private boolean esUrlRemota(String valor) {
        return valor != null && (valor.startsWith("http://") || valor.startsWith("https://"));
    }

    private void guardarProyectoConCoordenadas(String nuevoId, List<String> uriStrings,
                                               String fechaCreacion, String qrPath,
                                               Coordenadas coordenadas) {
        String nombreComercial = sessionData.nombreProyecto;
        String precioPublicado = formatearPrecioPublicado(sessionData.precioTotal);

        // Construir el objeto AdminProyecto
        AdminProyecto proyecto = new AdminProyecto(
                nuevoId,
                sessionData.nombreProyecto,
                sessionData.descripcion,
                sessionData.direccion,
                sessionData.distrito,
                sessionData.costoSeparacion,
                sessionData.precioTotal,
                nombreComercial,
                precioPublicado,
                sessionData.fechaEntrega,
                sessionData.estado,
                new ArrayList<>(sessionData.tipologias),
                new ArrayList<>(sessionData.asesoresAsignados),
                uriStrings,
                fechaCreacion
        );
        proyecto.setQrCode(qrPath != null ? qrPath : "");
        poblarCamposCompartidos(proyecto, uriStrings, coordenadas, fechaCreacion);

        AdminProyectosRepository.guardarEnFirestore(proyecto,
                new AdminProyectosRepository.GuardarCallback() {
                    @Override
                    public void onSuccess(String proyectoId) {
                        AdminProyectosRepository.guardar(AdminCrearProyectoActivity.this);

                        // ── Lab 5 (Parte 1): Incrementar contador en SharedPreferences ─────────
                        AdminPreferencesManager.incrementarProyectosCount(AdminCrearProyectoActivity.this);

                        // ── Lab 5 (Parte 3): Actualizar contador en Internal Storage ──────────
                        AdminStorageManager.actualizarContador(
                                AdminCrearProyectoActivity.this,
                                AdminStorageManager.CAMPO_PROYECTOS_REGISTRADOS);

                        // ── Lab 5 (Parte 2): Notificación de proyecto guardado ────────────────
                        Intent destinoNotif = new Intent(AdminCrearProyectoActivity.this,
                                AdminDetallesProyectoActivity.class);
                        destinoNotif.putExtra("proyecto_id", proyectoId);
                        NotificationHelper.lanzarNotificacionAdmin(
                                AdminCrearProyectoActivity.this,
                                "Proyecto Registrado",
                                "El proyecto \"" + sessionData.nombreProyecto
                                        + "\" ha sido guardado correctamente.",
                                NotificationHelper.NOTIF_ADMIN_PROYECTO_GUARDADO,
                                destinoNotif
                        );

                        sessionData.clear();
                        mostrarToast("Proyecto guardado exitosamente");
                        finish();
                    }

                    @Override
                    public void onError(String mensaje) {
                        btnGuardarProyecto.setEnabled(true);
                        btnGuardarProyecto.setText("Guardar proyecto");
                        mostrarToast("No se pudo guardar en Firebase: " + mensaje);
                    }
                });
    }

    private void poblarCamposCompartidos(AdminProyecto proyecto, List<String> imagenes,
                                         Coordenadas coordenadas, String fechaCreacion) {
        String inmobiliariaNombre = AdminPreferencesManager.obtenerInmobiliaria(this);
        String precioPublicado = proyecto.getPrecioPublicado().isEmpty()
                ? "S/ " + proyecto.getPrecioTotal()
                : proyecto.getPrecioPublicado();
        String imageUrl = imagenes != null && !imagenes.isEmpty() ? imagenes.get(0) : "";
        String ubicacion = proyecto.getDireccion().isEmpty()
                ? proyecto.getDistrito()
                : proyecto.getDireccion()
                + (proyecto.getDistrito().isEmpty() ? "" : ", " + proyecto.getDistrito());

        proyecto.setAdminUid(AdminProyectosRepository.obtenerAdminUidActual());
        proyecto.setInmobiliariaNombre(inmobiliariaNombre);
        proyecto.setInmobiliariaId(AdminProyectosRepository.crearInmobiliariaId(inmobiliariaNombre));
        proyecto.setTipo("Departamento");
        proyecto.setPrecio(precioPublicado);
        proyecto.setPrecioPublicado(precioPublicado);
        proyecto.setUbicacion(ubicacion);
        proyecto.setImageUrl(imageUrl);
        proyecto.setVisible(true);
        proyecto.setActivo(true);
        proyecto.setRatingPromedio(0.0);
        proyecto.setTotalResenas(0);
        proyecto.setFechaActualizacion(fechaCreacion);
        if (coordenadas != null) {
            proyecto.setLatitud(coordenadas.latitud);
            proyecto.setLongitud(coordenadas.longitud);
        }
    }

    private Coordenadas obtenerCoordenadasDesdeDireccion(String direccion, String distrito) {
        try {
            if (!Geocoder.isPresent()) return null;
            String consulta = direccion + ", " + distrito + ", Lima, Perú";
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> resultados = geocoder.getFromLocationName(consulta, 1);
            if (resultados == null || resultados.isEmpty()) return null;
            Address address = resultados.get(0);
            return new Coordenadas(address.getLatitude(), address.getLongitude());
        } catch (Exception e) {
            return null;
        }
    }

    private static class Coordenadas {
        final double latitud;
        final double longitud;

        Coordenadas(double latitud, double longitud) {
            this.latitud = latitud;
            this.longitud = longitud;
        }
    }

    // ── Cancelar con confirmación ────────────────────────────────────────────

    private void mostrarDialogoCancelar() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cancelar creación")
                .setMessage("¿Estás seguro que deseas cancelar? Se perderán los datos ingresados.")
                .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                    sessionData.clear();
                    finish();
                })
                .setNegativeButton("Continuar editando", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ── Guardar / Restaurar estado del formulario ────────────────────────────

    /**
     * Persiste el estado actual de los campos de texto en la sesión
     * antes de navegar a otra Activity.
     */
    private void saveCurrentFormData() {
        sessionData.nombreProyecto  = textoSeguro(etNombreProyecto);
        sessionData.descripcion     = textoSeguro(etDescripcionProyecto);
        sessionData.direccion       = textoSeguro(etDireccion);
        sessionData.costoSeparacion = textoSeguro(etCostoSeparacion);
        sessionData.precioTotal     = textoSeguro(etPrecioTotal);
        sessionData.nombreComercial = sessionData.nombreProyecto;
        sessionData.precioPublicado = formatearPrecioPublicado(sessionData.precioTotal);

        // Distrito: solo guardar si no es el placeholder
        if (tvDistrito != null && tvDistrito.getText() != null) {
            String dv = tvDistrito.getText().toString();
            if (!DISTRITO_PLACEHOLDER.equals(dv)) {
                sessionData.distrito = dv;
            }
        }
    }

    /**
     * Restaura los campos de texto a partir de los valores guardados en la sesión.
     */
    private void restoreSessionData() {
        if (!sessionData.nombreProyecto.isEmpty()) {
            etNombreProyecto.setText(sessionData.nombreProyecto);
        }
        if (!sessionData.descripcion.isEmpty()) {
            etDescripcionProyecto.setText(sessionData.descripcion);
        }
        if (!sessionData.fechaEntrega.isEmpty()) {
            tvFechaEntrega.setText(sessionData.fechaEntrega);
            tvFechaEntrega.setTextColor(Color.BLACK);
        }
        if (!sessionData.direccion.isEmpty()) {
            etDireccion.setText(sessionData.direccion);
        }
        // Parte 3: restaurar distrito
        if (tvDistrito != null && !sessionData.distrito.isEmpty()) {
            tvDistrito.setText(sessionData.distrito);
            tvDistrito.setTextColor(Color.BLACK);
        }
        if (!sessionData.costoSeparacion.isEmpty()) {
            etCostoSeparacion.setText(sessionData.costoSeparacion);
        }
        if (!sessionData.precioTotal.isEmpty()) {
            etPrecioTotal.setText(sessionData.precioTotal);
        }
        if (etNombreComercial != null && !sessionData.nombreComercial.isEmpty()) {
            etNombreComercial.setText(sessionData.nombreComercial);
        }
        if (etPrecioPublicado != null && !sessionData.precioPublicado.isEmpty()) {
            etPrecioPublicado.setText(sessionData.precioPublicado);
        }

        // Restaurar estado seleccionado
        selectedEstado = sessionData.estado;
        if ("En planos".equals(selectedEstado)) selectEstado("En planos", btnEnPlanos);
        else if ("Preventa".equals(selectedEstado)) selectEstado("Preventa", btnPreventa);
        else if ("En venta".equals(selectedEstado))  selectEstado("En venta",  btnEnVenta);
    }

    // ── Resultado de actividades (tipología, asesor, galería) ────────────────

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Procesar imágenes seleccionadas desde la galería
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                // Selección múltiple
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    imagenesSeleccionadas.add(uri);
                    agregarMiniatura(uri);
                }
            } else if (data.getData() != null) {
                // Selección individual (fallback)
                Uri uri = data.getData();
                imagenesSeleccionadas.add(uri);
                agregarMiniatura(uri);
            }
            actualizarContadorImagenes();
        }
        // requestCode 100 (tipología) y 101 (asesor): se procesan en onResume
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private String formatearPrecioPublicado(String precioTotal) {
        if (precioTotal == null || precioTotal.trim().isEmpty()) return "";
        try {
            double precio = Double.parseDouble(precioTotal.trim());
            return String.format(Locale.getDefault(), "S/ %,.0f", precio);
        } catch (NumberFormatException e) {
            return "S/ " + precioTotal.trim();
        }
    }

    /**
     * Genera un QR con el contenido indicado, lo guarda en almacenamiento interno
     * y devuelve la ruta absoluta del archivo PNG.
     *
     * @return ruta absoluta, o null si falló la generación.
     */
    private String generarYGuardarQR(String proyectoId, String contenido) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(contenido, BarcodeFormat.QR_CODE, 512, 512);

            File dir = new File(getFilesDir(), "qr_proyectos");
            if (!dir.exists()) dir.mkdirs();
            File qrFile = new File(dir, "qr_" + proyectoId + ".png");

            try (FileOutputStream fos = new FileOutputStream(qrFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }
            return qrFile.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    /** Convierte dp a px usando la densidad real del dispositivo. */
    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    /**
     * Extrae el texto de un TextInputEditText de forma segura (nunca null).
     *
     * @param et Campo de texto.
     * @return Texto con trim(), o "" si el campo es null.
     */
    private String textoSeguro(EditText et) {
        return (et != null && et.getText() != null) ? et.getText().toString().trim() : "";
    }

    /** Muestra un Toast corto con el mensaje indicado. */
    private void mostrarToast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}
