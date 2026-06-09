package com.example.bitbusters.activities.admin;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Formulario de edición de un proyecto existente del Administrador.
 *
 * Carga los datos del proyecto (por "proyecto_id" del Intent), permite editarlos,
 * y guarda los cambios en AdminProyectosRepository al confirmar.
 */
public class AdminEditarProyectoActivity extends AppCompatActivity {

    // ── Campos de texto ───────────────────────────────────────────────────────
    private TextInputEditText etNombreProyecto, etDescripcion;
    private TextInputEditText etCostoSeparacion, etPrecioTotal;
    private TextInputEditText etNombreComercial, etPrecioPublicado;
    private TextInputEditText etDireccion, etDistrito;
    private LinearLayout      layoutFechaEntrega;
    private TextView          tvFechaEntrega;

    // ── Botones de estado ─────────────────────────────────────────────────────
    private Button btnStateInPlanos, btnStateEnConstruccion, btnStateEnVenta;

    // ── Contenedores dinámicos ─────────────────────────────────────────────────
    private LinearLayout tipologiasContainerEditar;
    private LinearLayout asesoresContainerEditar;
    private LinearLayout imagesContainer;
    private LinearLayout btnAddImage;
    private TextView     tvContadorImagenes;

    // ── Botones de acción ─────────────────────────────────────────────────────
    private Button      btnSaveChanges, btnCancel;
    private Button      btnAddTipologia, btnAddAdvisor;
    private ImageButton btnBackEditProject;

    // ── Estado ────────────────────────────────────────────────────────────────
    private String                  proyectoId    = null;
    private String                  selectedEstado = "";
    private AdminProyectoSessionData sessionData;
    private final List<Uri>         imagenesSeleccionadas = new ArrayList<>();

    private static final String FECHA_PLACEHOLDER = "Seleccionar fecha";

    // ── Ciclo de vida ─────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_editar_proyecto);

        proyectoId  = getIntent().getStringExtra("proyecto_id");
        sessionData = AdminProyectoSessionData.getInstance();

        initializeViews();
        setupListeners();
        initializeStateButtonColors();
        cargarProyectoEnSesion();
        restoreFromSession();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderizarTipologias();
        renderizarAsesores();
    }

    // ── Inicialización ────────────────────────────────────────────────────────

    private void initializeViews() {
        btnBackEditProject        = findViewById(R.id.btnBackEditProject);
        btnSaveChanges            = findViewById(R.id.btnSaveChanges);
        btnCancel                 = findViewById(R.id.btnCancel);

        etNombreProyecto          = findViewById(R.id.etNombreProyecto);
        etDescripcion             = findViewById(R.id.etDescripcion);
        etCostoSeparacion         = findViewById(R.id.etCostoSeparacion);
        etPrecioTotal             = findViewById(R.id.etPrecioTotal);
        etNombreComercial         = findViewById(R.id.etNombreComercial);
        etPrecioPublicado         = findViewById(R.id.etPrecioPublicado);
        etDireccion               = findViewById(R.id.etDireccion);
        etDistrito                = findViewById(R.id.etDistrito);
        layoutFechaEntrega        = findViewById(R.id.layoutFechaEntrega);
        tvFechaEntrega            = findViewById(R.id.tvFechaEntrega);

        btnStateInPlanos          = findViewById(R.id.btnStateInPlanos);
        btnStateEnConstruccion    = findViewById(R.id.btnStateEnConstruccion);
        btnStateEnVenta           = findViewById(R.id.btnStateEnVenta);

        tipologiasContainerEditar = findViewById(R.id.tipologiasContainerEditar);
        asesoresContainerEditar   = findViewById(R.id.asesoresContainerEditar);
        imagesContainer           = findViewById(R.id.imagesContainer);
        btnAddImage               = findViewById(R.id.btnAddImage);
        tvContadorImagenes        = findViewById(R.id.tvContadorImagenes);

        btnAddTipologia           = findViewById(R.id.btnAddTipologia);
        btnAddAdvisor             = findViewById(R.id.btnAddAdvisor);
    }

    // ── Carga del proyecto en sesión ──────────────────────────────────────────

    /**
     * Busca el proyecto en el repositorio e inicializa AdminProyectoSessionData con sus datos.
     * Si el proyecto no existe, muestra un error y cierra.
     */
    private void cargarProyectoEnSesion() {
        if (proyectoId == null || proyectoId.isEmpty()) {
            mostrarToast("Error: proyecto no identificado");
            finish();
            return;
        }

        AdminProyecto p = AdminProyectosRepository.getById(proyectoId);
        if (p == null) {
            mostrarToast("Error: proyecto no encontrado");
            finish();
            return;
        }

        // Solo inicializar la sesión si está vacía (evita sobreescribir cambios del usuario
        // al volver de AdminAgregarTipologiaActivity o AdminAsignarAsesoresActivity)
        if (sessionData.nombreProyecto.isEmpty() && sessionData.tipologias.isEmpty()
                && sessionData.asesoresAsignados.isEmpty()) {
            sessionData.nombreProyecto  = p.getNombre();
            sessionData.descripcion     = p.getDescripcion();
            sessionData.direccion       = p.getDireccion();
            sessionData.distrito        = p.getDistrito();
            sessionData.costoSeparacion = p.getCostoSeparacion();
            sessionData.precioTotal     = p.getPrecioTotal();
            sessionData.nombreComercial = p.getNombreComercial();
            sessionData.precioPublicado = p.getPrecioPublicado();
            sessionData.fechaEntrega    = p.getFechaEntrega();
            sessionData.estado          = p.getEstado();
            sessionData.tipologias      = new ArrayList<>(p.getTipologias());
            sessionData.asesoresAsignados = new ArrayList<>(p.getAsesores());

            // Cargar imágenes existentes. Las rutas locales se convierten a file:// URI
            // para que Glide y la lógica de guardado las distingan de nuevas imágenes (content://).
            for (String uriStr : p.getImagenesUri()) {
                if (uriStr != null && !uriStr.isEmpty()) {
                    try {
                        Uri uri = uriStr.startsWith("/")
                                ? Uri.fromFile(new File(uriStr))
                                : Uri.parse(uriStr);
                        imagenesSeleccionadas.add(uri);
                        agregarMiniatura(uri);
                    } catch (Exception ignored) {}
                }
            }
            actualizarContadorImagenes();
        }
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    private void setupListeners() {
        btnBackEditProject.setOnClickListener(v -> {
            sessionData.clear();
            finish();
        });

        layoutFechaEntrega.setOnClickListener(v -> showDatePicker());

        btnStateInPlanos.setOnClickListener(v       -> selectEstado("En planos", btnStateInPlanos));
        btnStateEnConstruccion.setOnClickListener(v -> selectEstado("Preventa",  btnStateEnConstruccion));
        btnStateEnVenta.setOnClickListener(v        -> selectEstado("En venta",  btnStateEnVenta));

        btnAddTipologia.setOnClickListener(v -> {
            guardarCamposEnSesion();
            startActivityForResult(
                    new Intent(this, AdminAgregarTipologiaActivity.class), 100);
        });

        btnAddAdvisor.setOnClickListener(v -> {
            guardarCamposEnSesion();
            startActivityForResult(
                    new Intent(this, AdminAsignarAsesoresActivity.class), 101);
        });

        btnAddImage.setOnClickListener(v -> {
            guardarCamposEnSesion();
            openGallery();
        });

        btnSaveChanges.setOnClickListener(v -> {
            guardarCamposEnSesion();
            if (!validarFormulario()) return;
            guardarCambios();
        });

        btnCancel.setOnClickListener(v -> mostrarDialogoCancelar());
    }

    private void initializeStateButtonColors() {
        int deepBlue = ContextCompat.getColor(this, R.color.brand_deep_blue);
        btnStateInPlanos.setTextColor(deepBlue);
        btnStateEnConstruccion.setTextColor(deepBlue);
        btnStateEnVenta.setTextColor(deepBlue);
        btnStateInPlanos.setBackground(
                AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnStateEnConstruccion.setBackground(
                AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnStateEnVenta.setBackground(
                AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
    }

    private void selectEstado(String estado, Button selectedButton) {
        selectedEstado     = estado;
        sessionData.estado = estado;

        int deepBlue = ContextCompat.getColor(this, R.color.brand_deep_blue);
        btnStateInPlanos.setBackground(
                AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnStateInPlanos.setTextColor(deepBlue);
        btnStateEnConstruccion.setBackground(
                AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnStateEnConstruccion.setTextColor(deepBlue);
        btnStateEnVenta.setBackground(
                AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnStateEnVenta.setTextColor(deepBlue);

        selectedButton.setBackgroundColor(deepBlue);
        selectedButton.setTextColor(
                ContextCompat.getColor(this, android.R.color.white));
    }

    // ── Restaurar sesión en campos de texto ───────────────────────────────────

    private void restoreFromSession() {
        setTextSafe(etNombreProyecto,  sessionData.nombreProyecto);
        setTextSafe(etDescripcion,     sessionData.descripcion);
        setTextSafe(etCostoSeparacion, sessionData.costoSeparacion);
        setTextSafe(etPrecioTotal,     sessionData.precioTotal);
        setTextSafe(etNombreComercial, sessionData.nombreComercial);
        setTextSafe(etPrecioPublicado, sessionData.precioPublicado);
        setTextSafe(etDireccion,       sessionData.direccion);
        setTextSafe(etDistrito,        sessionData.distrito);

        if (!sessionData.fechaEntrega.isEmpty() && tvFechaEntrega != null) {
            tvFechaEntrega.setText(sessionData.fechaEntrega);
            tvFechaEntrega.setTextColor(Color.BLACK);
        }

        selectedEstado = sessionData.estado;
        if ("En planos".equals(selectedEstado)) selectEstado("En planos", btnStateInPlanos);
        else if ("Preventa".equals(selectedEstado)) selectEstado("Preventa",  btnStateEnConstruccion);
        else if ("En venta".equals(selectedEstado))  selectEstado("En venta",  btnStateEnVenta);
    }

    // ── DatePicker ────────────────────────────────────────────────────────────

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar sel = Calendar.getInstance();
                    sel.set(year, month, dayOfMonth);
                    String fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(sel.getTime());
                    if (tvFechaEntrega != null) {
                        tvFechaEntrega.setText(fecha);
                        tvFechaEntrega.setTextColor(Color.BLACK);
                    }
                    sessionData.fechaEntrega = fecha;
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));

        dialog.getDatePicker().setMinDate(cal.getTimeInMillis());
        dialog.show();
    }

    // ── Renderizado dinámico de tipologías ────────────────────────────────────

    private void renderizarTipologias() {
        if (tipologiasContainerEditar == null) return;
        tipologiasContainerEditar.removeAllViews();

        List<Tipologia> lista = sessionData.tipologias;
        if (lista == null || lista.isEmpty()) {
            TextView tvVacio = new TextView(this);
            tvVacio.setText("Sin tipologías agregadas");
            tvVacio.setTextColor(ContextCompat.getColor(this, R.color.neutral_medium));
            tvVacio.setTextSize(12f);
            tvVacio.setPadding(0, 0, 0, dpToPx(8));
            tipologiasContainerEditar.addView(tvVacio);
            return;
        }

        for (int i = 0; i < lista.size(); i++) {
            tipologiasContainerEditar.addView(crearCardTipologia(lista.get(i)));
        }
    }

    private View crearCardTipologia(final Tipologia tip) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dpToPx(8);
        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(8));
        card.setCardElevation(dpToPx(1));

        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(android.view.Gravity.CENTER_VERTICAL);
        fila.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));

        // Miniatura de tipología
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

        LinearLayout colDatos = new LinearLayout(this);
        colDatos.setOrientation(LinearLayout.VERTICAL);
        colDatos.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvNombre = new TextView(this);
        tvNombre.setText(tip.getNombre() + " – " + tip.getDormitorios() + " dorm.");
        tvNombre.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        tvNombre.setTextSize(13f);
        tvNombre.setTypeface(null, android.graphics.Typeface.BOLD);
        colDatos.addView(tvNombre);

        String precioFmt = String.format(Locale.getDefault(), "S/ %,.0f", tip.getPrecioTotal());
        TextView tvDetalles = new TextView(this);
        tvDetalles.setText(String.format(Locale.getDefault(),
                "%.1f m² · %d baño%s · %s",
                tip.getMetraje(), tip.getBanos(),
                tip.getBanos() > 1 ? "s" : "", precioFmt));
        tvDetalles.setTextColor(ContextCompat.getColor(this, R.color.neutral_dark));
        tvDetalles.setTextSize(11f);
        colDatos.addView(tvDetalles);

        fila.addView(colDatos);

        TextView btnX = new TextView(this);
        btnX.setText("×");
        btnX.setTextSize(18f);
        btnX.setTextColor(Color.WHITE);
        btnX.setGravity(android.view.Gravity.CENTER);
        int xSize = dpToPx(28);
        btnX.setLayoutParams(new LinearLayout.LayoutParams(xSize, xSize));
        GradientDrawable xBg = new GradientDrawable();
        xBg.setShape(GradientDrawable.RECTANGLE);
        xBg.setCornerRadius(dpToPx(4));
        xBg.setColor(Color.parseColor("#CC990000"));
        btnX.setBackground(xBg);

        btnX.setOnClickListener(v -> {
            sessionData.tipologias.remove(tip);
            renderizarTipologias();
        });

        fila.addView(btnX);
        card.addView(fila);
        return card;
    }

    // ── Renderizado dinámico de asesores ─────────────────────────────────────

    private void renderizarAsesores() {
        if (asesoresContainerEditar == null) return;
        asesoresContainerEditar.removeAllViews();

        List<String> lista = sessionData.asesoresAsignados;
        if (lista == null || lista.isEmpty()) {
            TextView tvVacio = new TextView(this);
            tvVacio.setText("Sin asesores asignados");
            tvVacio.setTextColor(ContextCompat.getColor(this, R.color.neutral_medium));
            tvVacio.setTextSize(12f);
            tvVacio.setPadding(0, 0, 0, dpToPx(8));
            asesoresContainerEditar.addView(tvVacio);
            return;
        }

        LinearLayout chipContainer = new LinearLayout(this);
        chipContainer.setOrientation(LinearLayout.VERTICAL);
        chipContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout filaActual = null;
        for (int i = 0; i < lista.size(); i++) {
            final String nombre = lista.get(i);
            if (i % 2 == 0) {
                filaActual = new LinearLayout(this);
                filaActual.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                fp.bottomMargin = dpToPx(4);
                filaActual.setLayoutParams(fp);
                chipContainer.addView(filaActual);
            }

            Chip chip = new Chip(this);
            chip.setText(nombre);
            chip.setCloseIconVisible(true);
            chip.setChipBackgroundColorResource(R.color.brand_deep_blue);
            chip.setTextColor(Color.WHITE);
            chip.setCloseIconTint(
                    android.content.res.ColorStateList.valueOf(Color.WHITE));
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cp.setMarginEnd(dpToPx(8));
            chip.setLayoutParams(cp);

            chip.setOnCloseIconClickListener(v -> {
                sessionData.asesoresAsignados.remove(nombre);
                renderizarAsesores();
            });

            if (filaActual != null) filaActual.addView(chip);
        }

        asesoresContainerEditar.addView(chipContainer);
    }

    // ── Galería de imágenes ───────────────────────────────────────────────────

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar imágenes"), 1001);
    }

    private void agregarMiniatura(final Uri uri) {
        int size   = dpToPx(80);
        int margen = dpToPx(8);
        int radio  = dpToPx(8);

        FrameLayout frame = new FrameLayout(this);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(size, size);
        fp.setMargins(0, 0, margen, 0);
        frame.setLayoutParams(fp);

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

        TextView btnX = new TextView(this);
        int xSize = dpToPx(22);
        FrameLayout.LayoutParams xp = new FrameLayout.LayoutParams(xSize, xSize);
        xp.gravity = Gravity.TOP | Gravity.END;
        btnX.setLayoutParams(xp);
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
        tvContadorImagenes.setText(total + (total != 1 ? " imágenes" : " imagen"));
    }

    // ── Validación ────────────────────────────────────────────────────────────

    private boolean validarFormulario() {
        if (textoSeguro(etNombreProyecto).isEmpty()) {
            mostrarToast("Por favor ingresa el nombre del proyecto");
            etNombreProyecto.requestFocus();
            return false;
        }
        if (textoSeguro(etDescripcion).isEmpty()) {
            mostrarToast("Por favor ingresa una descripción");
            etDescripcion.requestFocus();
            return false;
        }
        if (selectedEstado.isEmpty()) {
            mostrarToast("Por favor selecciona el estado del proyecto");
            return false;
        }
        String fecha = tvFechaEntrega != null && tvFechaEntrega.getText() != null
                ? tvFechaEntrega.getText().toString().trim() : "";
        if (fecha.isEmpty() || FECHA_PLACEHOLDER.equals(fecha)) {
            mostrarToast("Por favor selecciona la fecha de entrega");
            return false;
        }
        if (textoSeguro(etDireccion).isEmpty()) {
            mostrarToast("Por favor ingresa la dirección");
            etDireccion.requestFocus();
            return false;
        }
        if (textoSeguro(etDistrito).isEmpty()) {
            mostrarToast("Por favor ingresa el distrito");
            etDistrito.requestFocus();
            return false;
        }
        String costoStr = textoSeguro(etCostoSeparacion);
        if (costoStr.isEmpty()) {
            mostrarToast("Por favor ingresa el costo de separación");
            etCostoSeparacion.requestFocus();
            return false;
        }
        try {
            if (Double.parseDouble(costoStr) <= 0) {
                mostrarToast("El costo de separación debe ser mayor a 0");
                etCostoSeparacion.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarToast("El costo de separación debe ser un número válido");
            etCostoSeparacion.requestFocus();
            return false;
        }
        String precioStr = textoSeguro(etPrecioTotal);
        if (precioStr.isEmpty()) {
            mostrarToast("Por favor ingresa el precio total");
            etPrecioTotal.requestFocus();
            return false;
        }
        try {
            if (Double.parseDouble(precioStr) <= 0) {
                mostrarToast("El precio total debe ser mayor a 0");
                etPrecioTotal.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarToast("El precio total debe ser un número válido");
            etPrecioTotal.requestFocus();
            return false;
        }
        if (imagenesSeleccionadas.size() < 2) {
            mostrarToast("Debes tener al menos 2 imágenes del proyecto");
            return false;
        }
        return true;
    }

    // ── Guardar cambios ───────────────────────────────────────────────────────

    private void guardarCamposEnSesion() {
        sessionData.nombreProyecto  = textoSeguro(etNombreProyecto);
        sessionData.descripcion     = textoSeguro(etDescripcion);
        sessionData.costoSeparacion = textoSeguro(etCostoSeparacion);
        sessionData.precioTotal     = textoSeguro(etPrecioTotal);
        sessionData.nombreComercial = textoSeguro(etNombreComercial);
        sessionData.precioPublicado = textoSeguro(etPrecioPublicado);
        sessionData.direccion       = textoSeguro(etDireccion);
        sessionData.distrito        = textoSeguro(etDistrito);
    }

    private void guardarCambios() {
        // Procesar imágenes: copiar las nuevas (content://) a almacenamiento interno;
        // las ya existentes (file://) se guardan como ruta absoluta.
        // TODO-Firebase: reemplazar copiarImagenProyecto() por FirebaseStorage.upload().
        List<String> uriStrings = new ArrayList<>();
        for (Uri uri : imagenesSeleccionadas) {
            String scheme = uri.getScheme() != null ? uri.getScheme() : "";
            if ("file".equals(scheme)) {
                // Ya está en almacenamiento local — solo guardar la ruta
                String path = uri.getPath();
                uriStrings.add(path != null ? path : uri.toString());
            } else if ("content".equals(scheme)) {
                // Imagen nueva de galería — copiar a almacenamiento interno
                String localPath = copiarImagenProyecto(uri);
                uriStrings.add(localPath.isEmpty() ? uri.toString() : localPath);
            } else {
                uriStrings.add(uri.toString());
            }
        }

        // Recuperar el QR ya generado (no se regenera al editar)
        AdminProyecto existente = AdminProyectosRepository.getById(proyectoId);
        String qrCode = existente != null ? existente.getQrCode() : "";

        // Construir el proyecto actualizado
        AdminProyecto actualizado = new AdminProyecto(
                proyectoId,
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
                existente != null ? existente.getFechaCreacion() : ""
        );
        actualizado.setQrCode(qrCode);

        AdminProyectosRepository.actualizar(actualizado);
        AdminProyectosRepository.guardar(this);

        sessionData.clear();
        mostrarToast("Proyecto actualizado correctamente");
        finish();
    }

    // ── Cancelar ──────────────────────────────────────────────────────────────

    private void mostrarDialogoCancelar() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cancelar edición")
                .setMessage("¿Deseas descartar los cambios realizados?")
                .setPositiveButton("Sí, descartar", (d, w) -> {
                    sessionData.clear();
                    finish();
                })
                .setNegativeButton("Continuar editando", (d, w) -> d.dismiss())
                .show();
    }

    // ── onActivityResult ──────────────────────────────────────────────────────

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    imagenesSeleccionadas.add(uri);
                    agregarMiniatura(uri);
                }
            } else if (data.getData() != null) {
                Uri uri = data.getData();
                imagenesSeleccionadas.add(uri);
                agregarMiniatura(uri);
            }
            actualizarContadorImagenes();
        }
        // requestCode 100 (tipología) y 101 (asesor): se procesan en onResume
    }

    /**
     * Copia una imagen de galería (content://) a almacenamiento interno.
     * Devuelve la ruta absoluta del archivo, o "" si falla.
     *
     * TODO-Firebase: reemplazar por FirebaseStorage.upload() y devolver la URL de descarga.
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

    // ── Utilidades ────────────────────────────────────────────────────────────

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private String textoSeguro(TextInputEditText et) {
        return (et != null && et.getText() != null) ? et.getText().toString().trim() : "";
    }

    private void setTextSafe(TextInputEditText et, String valor) {
        if (et != null && valor != null && !valor.isEmpty()) et.setText(valor);
    }

    private void mostrarToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
