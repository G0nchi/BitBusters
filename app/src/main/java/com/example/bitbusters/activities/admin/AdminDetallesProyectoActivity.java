package com.example.bitbusters.activities.admin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;

import java.io.File;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.bitbusters.R;
import com.example.bitbusters.data.AdminProyectosRepository;
import com.example.bitbusters.models.AdminProyecto;
import com.example.bitbusters.models.Tipologia;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

/**
 * Pantalla de detalle de un proyecto del Administrador.
 *
 * Parte 4: Lee el "proyecto_id" del Intent, busca el proyecto en
 * AdminProyectosRepository y puebla dinámicamente todas las vistas.
 */
public class AdminDetallesProyectoActivity extends AppCompatActivity implements OnMapReadyCallback {

    // ── Vistas dinámicas ─────────────────────────────────────────────────────
    private TextView     tvNombreProyectoDetalle, tvUbicacionProyectoDetalle;
    private TextView     tvEstadoProyectoDetalle, tvFechaEntregaProyectoDetalle;
    private TextView     tvDescripcionDetalle, tvCostoSeparacionDetalle;
    private TextView     tvPrecioDesdeDetalle, tvNombreComercialDetalle;
    private TextView     tvDireccionDetalle, tvDistritoDetalle;
    private LinearLayout tipologiasContainerDetalle, asesoresContainerDetalle;
    private LinearLayout fotosContainerDetalle;
    private TextView     tvSinFotos;
    private AdminProyecto proyectoActual;
    private GoogleMap mapaDetalle;

    /** ID del proyecto actualmente mostrado; puede ser null para proyectos demo sin ID */
    private String proyectoId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_detalles_proyecto);

        initializeViews();
        cargarDatosProyecto();
        setupListeners();
        configurarMapa();
    }

    private void initializeViews() {
        tvNombreProyectoDetalle      = findViewById(R.id.tvNombreProyectoDetalle);
        tvUbicacionProyectoDetalle   = findViewById(R.id.tvUbicacionProyectoDetalle);
        tvEstadoProyectoDetalle      = findViewById(R.id.tvEstadoProyectoDetalle);
        tvFechaEntregaProyectoDetalle = findViewById(R.id.tvFechaEntregaProyectoDetalle);
        tvDescripcionDetalle         = findViewById(R.id.tvDescripcionDetalle);
        tvCostoSeparacionDetalle     = findViewById(R.id.tvCostoSeparacionDetalle);
        tvPrecioDesdeDetalle         = findViewById(R.id.tvPrecioDesdeDetalle);
        tvNombreComercialDetalle     = findViewById(R.id.tvNombreComercialDetalle);
        tvDireccionDetalle           = findViewById(R.id.tvDireccionDetalle);
        tvDistritoDetalle            = findViewById(R.id.tvDistritoDetalle);
        tipologiasContainerDetalle   = findViewById(R.id.tipologiasContainerDetalle);
        asesoresContainerDetalle     = findViewById(R.id.asesoresContainerDetalle);
        fotosContainerDetalle        = findViewById(R.id.fotosContainerDetalle);
        tvSinFotos                   = findViewById(R.id.tvSinFotos);
    }

    // ── Carga de datos desde el repositorio ──────────────────────────────────

    /**
     * Lee "proyecto_id" del Intent y busca el proyecto en AdminProyectosRepository.
     * Si no se encuentra, muestra un estado vacío genérico.
     */
    private void cargarDatosProyecto() {
        proyectoId = getIntent().getStringExtra("proyecto_id");

        AdminProyecto proyecto = AdminProyectosRepository.getById(proyectoId);
        if (proyecto == null) {
            // Fallback: proyecto no encontrado (no debería ocurrir en flujo normal)
            setTextSafe(tvNombreProyectoDetalle,   "Proyecto no encontrado");
            setTextSafe(tvUbicacionProyectoDetalle, "—");
            return;
        }

        poblarVistas(proyecto);
    }

    private void configurarMapa() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapDetalleProyecto);
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
        mapaDetalle = googleMap;
        pintarMapaProyecto();
    }

    /**
     * Llena todas las vistas con los datos del proyecto.
     *
     * @param p Proyecto a mostrar.
     */
    private void poblarVistas(AdminProyecto p) {
        proyectoActual = p;

        // ── Hero ──────────────────────────────────────────────────────────────
        setTextSafe(tvNombreProyectoDetalle, p.getNombre());
        setTextSafe(tvUbicacionProyectoDetalle,
                p.getDireccion() + (p.getDistrito().isEmpty() ? "" : ", " + p.getDistrito()));

        // ── Badge de estado con color ─────────────────────────────────────────
        setTextSafe(tvEstadoProyectoDetalle, p.getEstado());
        if (tvEstadoProyectoDetalle != null) {
            switch (p.getEstado()) {
                case "En venta":
                    tvEstadoProyectoDetalle.setBackgroundColor(Color.parseColor("#4CAF50"));
                    break;
                case "Preventa":
                    tvEstadoProyectoDetalle.setBackgroundColor(Color.parseColor("#FF9800"));
                    break;
                case "En planos":
                    tvEstadoProyectoDetalle.setBackgroundColor(Color.parseColor("#2196F3"));
                    break;
                default:
                    tvEstadoProyectoDetalle.setBackgroundColor(Color.parseColor("#9E9E9E"));
            }
        }

        // ── Fecha de entrega ──────────────────────────────────────────────────
        String fechaTexto = p.getFechaEntrega().isEmpty()
                ? "Entrega estimada: —"
                : "Entrega estimada: " + p.getFechaEntrega();
        setTextSafe(tvFechaEntregaProyectoDetalle, fechaTexto);

        // ── Card de información general ────────────────────────────────────────
        setTextSafe(tvDescripcionDetalle,
                p.getDescripcion().isEmpty() ? "Sin descripción" : p.getDescripcion());
        setTextSafe(tvCostoSeparacionDetalle,
                p.getCostoSeparacion().isEmpty() ? "—" : "S/ " + p.getCostoSeparacion());
        setTextSafe(tvPrecioDesdeDetalle,
                p.getPrecioTotal().isEmpty() ? "—" : "S/ " + p.getPrecioTotal());
        setTextSafe(tvNombreComercialDetalle,
                p.getInmobiliariaNombre().isEmpty() ? "—" : p.getInmobiliariaNombre());

        // ── Ubicación ─────────────────────────────────────────────────────────
        setTextSafe(tvDireccionDetalle,
                p.getDireccion().isEmpty() ? "—" : p.getDireccion());
        setTextSafe(tvDistritoDetalle,
                p.getDistrito().isEmpty() ? "—" : p.getDistrito());

        // ── Tipologías dinámicas ───────────────────────────────────────────────
        renderizarTipologias(p.getTipologias());

        // ── Fotos dinámicas ───────────────────────────────────────────────────
        renderizarFotos(p.getImagenesUri());

        // ── Asesores dinámicos ─────────────────────────────────────────────────
        renderizarAsesores(p.getAsesores());

        pintarMapaProyecto();
    }

    private void pintarMapaProyecto() {
        if (mapaDetalle == null || proyectoActual == null) return;

        Double lat = proyectoActual.getLatitud();
        Double lng = proyectoActual.getLongitud();
        if (lat == null || lng == null) {
            LatLng lima = new LatLng(-12.0464, -77.0428);
            mapaDetalle.moveCamera(CameraUpdateFactory.newLatLngZoom(lima, 11f));
            return;
        }

        LatLng ubicacion = new LatLng(lat, lng);
        mapaDetalle.clear();
        mapaDetalle.addMarker(new MarkerOptions()
                .position(ubicacion)
                .title(proyectoActual.getNombre())
                .snippet(proyectoActual.getDireccion()));
        mapaDetalle.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 16f));
    }

    // ── Renderizado de secciones dinámicas ───────────────────────────────────

    /**
     * Agrega una fila por tipología en tipologiasContainerDetalle.
     * Si la lista está vacía muestra "Sin tipologías".
     */
    private void renderizarTipologias(List<Tipologia> lista) {
        if (tipologiasContainerDetalle == null) return;
        tipologiasContainerDetalle.removeAllViews();

        if (lista == null || lista.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("Sin tipologías agregadas");
            tv.setTextColor(getColor(R.color.neutral_medium));
            tv.setTextSize(12f);
            tipologiasContainerDetalle.addView(tv);
            return;
        }

        for (Tipologia tip : lista) {
            // Fila: miniatura + datos izquierda + precio derecha
            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setGravity(android.view.Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams filaParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            filaParams.bottomMargin = dpToPx(10);
            fila.setLayoutParams(filaParams);

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
                imgTip.setOnClickListener(v -> mostrarImagenFullscreen(tipImagePath));
                fila.addView(imgTip);
            }

            // Columna izquierda
            LinearLayout colIzq = new LinearLayout(this);
            colIzq.setOrientation(LinearLayout.VERTICAL);
            colIzq.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvNombre = new TextView(this);
            tvNombre.setText(tip.getNombre() + " – " + tip.getDormitorios() + " dorm.");
            tvNombre.setTextColor(getColor(R.color.text_primary));
            tvNombre.setTextSize(12f);
            tvNombre.setTypeface(null, android.graphics.Typeface.BOLD);
            colIzq.addView(tvNombre);

            TextView tvDetalle = new TextView(this);
            tvDetalle.setText(String.format(Locale.getDefault(),
                    "%.1f m² · %d baño%s",
                    tip.getMetraje(), tip.getBanos(), tip.getBanos() > 1 ? "s" : ""));
            tvDetalle.setTextColor(getColor(R.color.neutral_dark));
            tvDetalle.setTextSize(11f);
            colIzq.addView(tvDetalle);

            fila.addView(colIzq);

            // Precio en verde (derecha)
            TextView tvPrecio = new TextView(this);
            String precioFmt = String.format(Locale.getDefault(), "S/ %,.0f", tip.getPrecioTotal());
            tvPrecio.setText(precioFmt);
            tvPrecio.setTextColor(getColor(R.color.brand_lime));
            tvPrecio.setTextSize(12f);
            tvPrecio.setTypeface(null, android.graphics.Typeface.BOLD);
            fila.addView(tvPrecio);

            // Separador visual
            View separador = new View(this);
            LinearLayout.LayoutParams sepParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
            sepParams.topMargin = dpToPx(4);
            separador.setLayoutParams(sepParams);
            separador.setBackgroundColor(Color.parseColor("#E6E6E6"));

            tipologiasContainerDetalle.addView(fila);
            tipologiasContainerDetalle.addView(separador);
        }
    }

    /**
     * Agrega miniaturas de las fotos del proyecto en el scroll horizontal.
     * Si no hay fotos, muestra el mensaje tvSinFotos.
     */
    private void renderizarFotos(List<String> uriStrings) {
        if (fotosContainerDetalle == null) return;
        fotosContainerDetalle.removeAllViews();

        if (uriStrings == null || uriStrings.isEmpty()) {
            if (tvSinFotos != null) tvSinFotos.setVisibility(View.VISIBLE);
            return;
        }

        if (tvSinFotos != null) tvSinFotos.setVisibility(View.GONE);

        List<String> imagenesValidas = new ArrayList<>();
        for (String uriStr : uriStrings) {
            if (uriStr != null && !uriStr.isEmpty()) imagenesValidas.add(uriStr);
        }

        for (int i = 0; i < imagenesValidas.size(); i++) {
            String uriStr = imagenesValidas.get(i);
            final int index = i;
            if (uriStr == null || uriStr.isEmpty()) continue;

            ImageView imgView = new ImageView(this);
            int size   = dpToPx(110);
            int margen = dpToPx(6);
            int radio  = dpToPx(8);
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(size, size);
            imgParams.setMargins(0, 0, margen, 0);
            imgView.setLayoutParams(imgParams);
            imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imgView.setClipToOutline(true);

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(radio);
            bg.setColor(0xFFE0E0E0);
            imgView.setBackground(bg);

            // Cargar desde ruta local o URL (File para rutas absolutas, String para URLs)
            if (uriStr.startsWith("/")) {
                Glide.with(this).load(new File(uriStr)).centerCrop().into(imgView);
            } else {
                Glide.with(this).load(uriStr).centerCrop().into(imgView);
            }
            imgView.setOnClickListener(v -> mostrarCarruselImagenes(imagenesValidas, index));
            fotosContainerDetalle.addView(imgView);
        }
    }

    /**
     * Muestra la lista de asesores como filas simples con avatar e iniciales.
     * Si la lista está vacía muestra "Sin asesores asignados".
     */
    private void renderizarAsesores(List<String> nombres) {
        if (asesoresContainerDetalle == null) return;
        asesoresContainerDetalle.removeAllViews();

        if (nombres == null || nombres.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("Sin asesores asignados");
            tv.setTextColor(getColor(R.color.neutral_medium));
            tv.setTextSize(12f);
            asesoresContainerDetalle.addView(tv);
            return;
        }

        for (String nombre : nombres) {
            // Fila por asesor
            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setGravity(android.view.Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams filaParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            filaParams.bottomMargin = dpToPx(8);
            fila.setLayoutParams(filaParams);

            // Avatar con iniciales
            TextView tvAvatar = new TextView(this);
            int avatarSize = dpToPx(40);
            LinearLayout.LayoutParams avatarParams =
                    new LinearLayout.LayoutParams(avatarSize, avatarSize);
            avatarParams.setMarginEnd(dpToPx(12));
            tvAvatar.setLayoutParams(avatarParams);
            tvAvatar.setGravity(android.view.Gravity.CENTER);
            tvAvatar.setTextColor(Color.WHITE);
            tvAvatar.setTextSize(14f);
            tvAvatar.setTypeface(null, android.graphics.Typeface.BOLD);
            // Iniciales: primeras letras de nombre y apellido
            String iniciales = obtenerIniciales(nombre);
            tvAvatar.setText(iniciales);
            GradientDrawable avatarBg = new GradientDrawable();
            avatarBg.setShape(GradientDrawable.OVAL);
            avatarBg.setColor(getColor(R.color.brand_deep_blue));
            tvAvatar.setBackground(avatarBg);

            fila.addView(tvAvatar);

            // Nombre completo
            TextView tvNombre = new TextView(this);
            tvNombre.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvNombre.setText(nombre);
            tvNombre.setTextColor(getColor(R.color.text_primary));
            tvNombre.setTextSize(13f);
            tvNombre.setTypeface(null, android.graphics.Typeface.BOLD);

            fila.addView(tvNombre);
            asesoresContainerDetalle.addView(fila);
        }
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    private void setupListeners() {
        ImageButton btnBack = findViewById(R.id.btnBackProject);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        ImageButton btnEdit = findViewById(R.id.btnEditProject);
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminEditarProyectoActivity.class);
                intent.putExtra("proyecto_id", proyectoId);
                startActivity(intent);
            });
        }

        ImageButton btnQr = findViewById(R.id.btnQrProject);
        if (btnQr != null) {
            btnQr.setOnClickListener(v -> mostrarDialogoQR());
        }
    }

    // ── Diálogo QR ────────────────────────────────────────────────────────────

    /**
     * Genera el QR del proyecto (a partir del ID) y lo muestra en un BottomSheetDialog.
     * Si el proyecto tenía QR guardado en disco, lo carga; de lo contrario lo genera al vuelo.
     */
    private void mostrarDialogoQR() {
        if (proyectoId == null) return;

        AdminProyecto proyecto = AdminProyectosRepository.getById(proyectoId);
        if (proyecto == null) return;

        Bitmap qrBitmap = null;

        // Intentar cargar desde la ruta guardada
        String qrPath = proyecto.getQrCode();
        boolean qrEsUrl = qrPath.startsWith("http://") || qrPath.startsWith("https://");
        if (!qrPath.isEmpty() && !qrEsUrl) {
            qrBitmap = BitmapFactory.decodeFile(qrPath);
        }

        // Si no hay archivo guardado, generar al vuelo
        if (qrBitmap == null && !qrEsUrl) {
            try {
                BarcodeEncoder encoder = new BarcodeEncoder();
                qrBitmap = encoder.encodeBitmap(
                        "inmobiliaria://proyecto/" + proyecto.getId(),
                        BarcodeFormat.QR_CODE, 512, 512);
            } catch (Exception e) {
                Toast.makeText(this, "Error generando el QR", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Construir el BottomSheetDialog
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
        tvTitle.setTextColor(getColor(R.color.text_primary));
        layout.addView(tvTitle);

        TextView tvSubtitle = new TextView(this);
        tvSubtitle.setText(proyecto.getNombre());
        tvSubtitle.setTextSize(13f);
        tvSubtitle.setGravity(Gravity.CENTER);
        tvSubtitle.setTextColor(getColor(R.color.neutral_dark));
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
        if (qrEsUrl) {
            Glide.with(this).load(qrPath).into(imgQr);
        } else {
            imgQr.setImageBitmap(qrBitmap);
        }
        layout.addView(imgQr);

        Button btnCerrar = new Button(this);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.topMargin = dpToPx(16);
        btnCerrar.setLayoutParams(btnParams);
        btnCerrar.setText("Cerrar");
        btnCerrar.setOnClickListener(v2 -> dialog.dismiss());
        layout.addView(btnCerrar);

        dialog.setContentView(layout);
        dialog.show();
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    /**
     * Obtiene las iniciales de un nombre completo.
     * Ejemplo: "Carlos Ruiz" → "CR", "Ana" → "AN"
     */
    private String obtenerIniciales(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) return "?";
        String[] partes = nombreCompleto.trim().split("\\s+");
        if (partes.length == 1) {
            String n = partes[0].toUpperCase(Locale.getDefault());
            return n.length() >= 2 ? n.substring(0, 2) : n;
        }
        return (String.valueOf(partes[0].charAt(0)) +
                String.valueOf(partes[1].charAt(0))).toUpperCase(Locale.getDefault());
    }

    /**
     * Establece el texto de un TextView de forma segura (null-safe).
     */
    private void setTextSafe(TextView tv, String valor) {
        if (tv != null && valor != null) tv.setText(valor);
    }

    private boolean esUrlRemota(String valor) {
        return valor != null && (valor.startsWith("http://") || valor.startsWith("https://"));
    }

    private void mostrarImagenFullscreen(String imagen) {
        if (imagen == null || imagen.isEmpty()) return;
        List<String> unica = new ArrayList<>();
        unica.add(imagen);
        mostrarCarruselImagenes(unica, 0);
    }

    private void mostrarCarruselImagenes(List<String> imagenes, int posicionInicial) {
        if (imagenes == null || imagenes.isEmpty()) return;

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        ViewPager2 viewPager = new ViewPager2(this);
        viewPager.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        viewPager.setAdapter(new ImagenFullscreenAdapter(imagenes));
        int posicionSegura = Math.max(0, Math.min(posicionInicial, imagenes.size() - 1));
        viewPager.setCurrentItem(posicionSegura, false);
        root.addView(viewPager);

        TextView contador = new TextView(this);
        contador.setTextColor(Color.WHITE);
        contador.setTextSize(13f);
        contador.setGravity(Gravity.CENTER);
        contador.setBackgroundColor(0x66000000);
        contador.setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4));
        FrameLayout.LayoutParams contadorParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        contadorParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        contadorParams.setMargins(0, 0, 0, dpToPx(24));
        contador.setLayoutParams(contadorParams);
        root.addView(contador);

        actualizarContadorImagen(contador, posicionSegura, imagenes.size());
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                actualizarContadorImagen(contador, position, imagenes.size());
            }
        });

        ImageButton btnCerrar = new ImageButton(this);
        int size = dpToPx(44);
        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(size, size);
        closeParams.gravity = Gravity.TOP | Gravity.END;
        closeParams.setMargins(0, dpToPx(16), dpToPx(16), 0);
        btnCerrar.setLayoutParams(closeParams);
        btnCerrar.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        btnCerrar.setColorFilter(Color.WHITE);
        btnCerrar.setBackgroundColor(Color.TRANSPARENT);
        btnCerrar.setContentDescription("Cerrar imagen");
        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        root.addView(btnCerrar);

        root.setOnClickListener(v -> dialog.dismiss());
        viewPager.setOnClickListener(v -> { });

        dialog.setContentView(root);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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

    private void actualizarContadorImagen(TextView contador, int position, int total) {
        if (contador == null) return;
        contador.setText(String.format(Locale.getDefault(), "%d / %d", position + 1, total));
    }

    private class ImagenFullscreenAdapter
            extends RecyclerView.Adapter<ImagenFullscreenAdapter.ImagenViewHolder> {

        private final List<String> imagenes;

        ImagenFullscreenAdapter(List<String> imagenes) {
            this.imagenes = imagenes;
        }

        @NonNull
        @Override
        public ImagenViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent,
                                                   int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(dpToPx(12), dpToPx(48), dpToPx(12), dpToPx(48));
            imageView.setBackgroundColor(Color.BLACK);
            return new ImagenViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ImagenViewHolder holder, int position) {
            String imagen = imagenes.get(position);
            Glide.with(AdminDetallesProyectoActivity.this)
                    .load(esUrlRemota(imagen) ? imagen : new File(imagen))
                    .fitCenter()
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return imagenes != null ? imagenes.size() : 0;
        }

        class ImagenViewHolder extends RecyclerView.ViewHolder {
            final ImageView imageView;

            ImagenViewHolder(@NonNull ImageView imageView) {
                super(imageView);
                this.imageView = imageView;
            }
        }
    }

    /** Convierte dp a px usando la densidad real del dispositivo. */
    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
