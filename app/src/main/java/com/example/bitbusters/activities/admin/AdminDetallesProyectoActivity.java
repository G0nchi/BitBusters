package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;

import java.io.File;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bitbusters.R;
import com.example.bitbusters.data.AdminProyectosRepository;
import com.example.bitbusters.models.AdminProyecto;
import com.example.bitbusters.models.Tipologia;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.List;
import java.util.Locale;

/**
 * Pantalla de detalle de un proyecto del Administrador.
 *
 * Parte 4: Lee el "proyecto_id" del Intent, busca el proyecto en
 * AdminProyectosRepository y puebla dinámicamente todas las vistas.
 */
public class AdminDetallesProyectoActivity extends AppCompatActivity {

    // ── Vistas dinámicas ─────────────────────────────────────────────────────
    private TextView     tvNombreProyectoDetalle, tvUbicacionProyectoDetalle;
    private TextView     tvEstadoProyectoDetalle, tvFechaEntregaProyectoDetalle;
    private TextView     tvDescripcionDetalle, tvCostoSeparacionDetalle;
    private TextView     tvPrecioDesdeDetalle, tvNombreComercialDetalle;
    private TextView     tvDireccionDetalle, tvDistritoDetalle;
    private LinearLayout tipologiasContainerDetalle, asesoresContainerDetalle;
    private LinearLayout fotosContainerDetalle;
    private TextView     tvSinFotos;

    /** ID del proyecto actualmente mostrado; puede ser null para proyectos demo sin ID */
    private String proyectoId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_detalles_proyecto);

        initializeViews();
        cargarDatosProyecto();
        setupListeners();
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

    /**
     * Llena todas las vistas con los datos del proyecto.
     *
     * @param p Proyecto a mostrar.
     */
    private void poblarVistas(AdminProyecto p) {
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
                p.getNombreComercial().isEmpty() ? "—" : p.getNombreComercial());

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
                Glide.with(this).load(new File(tipImagePath)).centerCrop().into(imgTip);
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

        for (String uriStr : uriStrings) {
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
        if (!qrPath.isEmpty()) {
            qrBitmap = BitmapFactory.decodeFile(qrPath);
        }

        // Si no hay archivo guardado, generar al vuelo
        if (qrBitmap == null) {
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
        imgQr.setImageBitmap(qrBitmap);
        imgQr.setScaleType(ImageView.ScaleType.FIT_CENTER);
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

    /** Convierte dp a px usando la densidad real del dispositivo. */
    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
