package com.example.bitbusters.activities.admin;

import androidx.appcompat.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
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

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Formulario de creación de proyecto inmobiliario del Administrador.
 *
 * Partes implementadas:
 *  Parte 1 — Tipologías dinámicas: onResume() renderiza cards desde la sesión
 *  Parte 2 — Asesores dinámicos: onResume() renderiza chips desde la sesión
 *  Parte 3 — Distrito con AlertDialog de búsqueda en tiempo real
 *  Parte 4 — Al guardar, crea AdminProyecto y lo agrega a AdminProyectosRepository
 */
public class AdminCrearProyectoActivity extends AppCompatActivity {

    // ── Campos de texto ───────────────────────────────────────────────────────
    private TextInputEditText etNombreProyecto, etDescripcionProyecto;
    private TextView          tvFechaEntrega;
    private TextInputEditText etDireccion;
    private TextView          tvDistrito;          // Parte 3: no editable, abre diálogo
    private LinearLayout      layoutDistritoSelector;
    private TextInputEditText etCostoSeparacion, etPrecioTotal;
    private TextInputEditText etNombreComercial, etPrecioPublicado;

    // ── Botones de estado del proyecto ───────────────────────────────────────
    private Button btnEnPlanos, btnPreventa, btnEnVenta;

    // ── Botones de acción ────────────────────────────────────────────────────
    private Button      btnAgregarTipologia, btnAgregarAsesor;
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

    // Placeholder para el campo fecha (para validar que hay fecha real)
    private static final String FECHA_PLACEHOLDER    = "Seleccionar fecha";
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
            if (!validarFormulario()) return;
            guardarProyecto();
        });

        // Cancelar: diálogo de confirmación
        btnCancelarCrear.setOnClickListener(v -> mostrarDialogoCancelar());
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
            tvDistrito.setText(distritoSeleccionado);
            tvDistrito.setTextColor(Color.BLACK);
            sessionData.distrito = distritoSeleccionado;
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
            Glide.with(this).load(new File(tipImagePath)).centerCrop().into(imgTip);
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

        // 4. Fecha de entrega
        String fecha = tvFechaEntrega.getText() != null
                ? tvFechaEntrega.getText().toString().trim() : "";
        if (fecha.isEmpty() || FECHA_PLACEHOLDER.equals(fecha)) {
            mostrarToast("Por favor selecciona la fecha de entrega del proyecto");
            return false;
        }

        // 5. Dirección
        String direccion = textoSeguro(etDireccion);
        if (direccion.isEmpty()) {
            mostrarToast("Por favor ingresa la dirección del proyecto");
            etDireccion.requestFocus();
            return false;
        }

        // 6. Distrito (Parte 3: selector)
        String distrito = tvDistrito != null && tvDistrito.getText() != null
                ? tvDistrito.getText().toString().trim() : "";
        if (distrito.isEmpty() || DISTRITO_PLACEHOLDER.equals(distrito)) {
            mostrarToast("Por favor selecciona el distrito del proyecto");
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
        // Generar ID único
        String nuevoId = UUID.randomUUID().toString();

        // Copiar cada imagen del proyecto a almacenamiento interno y guardar la ruta local.
        // TODO-Firebase: reemplazar copiarImagenProyecto() por FirebaseStorage.upload()
        //                y guardar la URL de descarga devuelta.
        List<String> uriStrings = new ArrayList<>();
        for (Uri uri : imagenesSeleccionadas) {
            String localPath = copiarImagenProyecto(uri);
            uriStrings.add(localPath.isEmpty() ? uri.toString() : localPath);
        }

        // Timestamp de creación
        String fechaCreacion = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        // Generar QR del proyecto y guardar en almacenamiento interno
        String qrPath = generarYGuardarQR(nuevoId,
                "inmobiliaria://proyecto/" + nuevoId);

        // Construir el objeto AdminProyecto
        AdminProyecto proyecto = new AdminProyecto(
                nuevoId,
                sessionData.nombreProyecto,
                sessionData.descripcion,
                sessionData.direccion,
                sessionData.distrito,
                sessionData.costoSeparacion,
                sessionData.precioTotal,
                sessionData.nombreComercial,
                sessionData.precioPublicado,
                sessionData.fechaEntrega,
                sessionData.estado,
                new ArrayList<>(sessionData.tipologias),
                new ArrayList<>(sessionData.asesoresAsignados),
                uriStrings,
                fechaCreacion
        );
        proyecto.setQrCode(qrPath != null ? qrPath : "");

        // Agregar al repositorio compartido y persistir en disco
        AdminProyectosRepository.agregar(proyecto);
        AdminProyectosRepository.guardar(this);

        // ── Lab 5 (Parte 1): Incrementar contador en SharedPreferences ─────────
        AdminPreferencesManager.incrementarProyectosCount(this);

        // ── Lab 5 (Parte 3): Actualizar contador en Internal Storage ──────────
        AdminStorageManager.actualizarContador(
                this, AdminStorageManager.CAMPO_PROYECTOS_REGISTRADOS);

        // ── Lab 5 (Parte 2): Notificación de proyecto guardado ────────────────
        // El PendingIntent abre AdminDetallesProyectoActivity con el ID del proyecto
        Intent destinoNotif = new Intent(this, AdminDetallesProyectoActivity.class);
        destinoNotif.putExtra("proyecto_id", nuevoId);
        NotificationHelper.lanzarNotificacionAdmin(
                this,
                "Proyecto Registrado",
                "El proyecto \"" + sessionData.nombreProyecto + "\" ha sido guardado correctamente.",
                NotificationHelper.NOTIF_ADMIN_PROYECTO_GUARDADO,
                destinoNotif
        );

        // Limpiar sesión y cerrar el formulario
        sessionData.clear();
        mostrarToast("Proyecto guardado exitosamente");
        finish();
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
        sessionData.nombreComercial = textoSeguro(etNombreComercial);
        sessionData.precioPublicado = textoSeguro(etPrecioPublicado);

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
        if (!sessionData.nombreComercial.isEmpty()) {
            etNombreComercial.setText(sessionData.nombreComercial);
        }
        if (!sessionData.precioPublicado.isEmpty()) {
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

    /**
     * Copia una imagen de galería (content://) a almacenamiento interno del app.
     * Devuelve la ruta absoluta del archivo destino, o "" si falla.
     *
     * TODO-Firebase: reemplazar por FirebaseStorage.getInstance()
     *     .getReference("proyecto_images/{uuid}").putFile(uri)
     *     y devolver la URL de descarga.
     */
    private String copiarImagenProyecto(Uri uri) {
        try {
            File dir = new File(getFilesDir(), "proyecto_images");
            if (!dir.exists()) dir.mkdirs();
            File dest = new File(dir, "img_" + UUID.randomUUID() + ".jpg");
            try (InputStream in  = getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(dest)) {
                if (in == null) return "";
                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            }
            return dest.getAbsolutePath();
        } catch (Exception e) {
            return "";
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
    private String textoSeguro(TextInputEditText et) {
        return (et != null && et.getText() != null) ? et.getText().toString().trim() : "";
    }

    /** Muestra un Toast corto con el mensaje indicado. */
    private void mostrarToast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}
