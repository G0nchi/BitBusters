package com.example.bitbusters.activities.cliente;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;
import com.example.bitbusters.utils.ImageUrls;
import com.example.bitbusters.utils.NotificationHelper;
import com.bumptech.glide.Glide;

public class ProjectDetailActivity extends AppCompatActivity {

    private static final String EXTRA_PROYECTO = "proyecto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        // Crear el canal de notificaciones (necesario para lanzar la notificación de separación)
        NotificationHelper.crearCanal(this);

        // Resolver nombre del proyecto: extra normal o deep link (QR)
        String nombreProyecto = resolverNombreProyecto();
        if (nombreProyecto == null || nombreProyecto.isEmpty()) {
            Toast.makeText(this, "Proyecto no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (nombreProyecto != null) {
            ((TextView) findViewById(R.id.tvNombreProyecto)).setText(nombreProyecto);
            
            // Cargar imagen hero según el proyecto
            ImageView imgHero = findViewById(R.id.imgHero);
            int imageRes = obtenerImagenProyecto(nombreProyecto);
            if (imgHero != null) {
                Glide.with(this)
                        .load(imageRes)
                        .centerCrop()
                        .into(imgHero);
            }
            
            // Cargar avatar del asesor
            ImageView imgAsesor = findViewById(R.id.imgAsesor);
            if (imgAsesor != null) {
                Glide.with(this)
                        .load(ImageUrls.AVATAR_FABRI)
                        .centerCrop()
                        .into(imgAsesor);
            }
        }

        // Botón volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Botón Agendar Cita
        View btnAgendar = findViewById(R.id.btnAgendarCita);
        if (btnAgendar != null) {
            btnAgendar.setOnClickListener(v -> {
                Intent intent = new Intent(this, AgendaCitaActivity.class);
                intent.putExtra(EXTRA_PROYECTO, nombreProyecto);
                startActivity(intent);
            });
        }

        // Botón compartir hero
        findViewById(R.id.btnCompartir).setOnClickListener(v -> {
            // TODO: compartir proyecto
        });

        // Botón favorito
        findViewById(R.id.btnFavorito).setOnClickListener(v -> {
            // TODO: guardar en favoritos
        });

        // Botón Rentar
        findViewById(R.id.btnRentar).setOnClickListener(v -> {
            // TODO: flujo de renta
        });

        // Botón Comprar / Separar
        findViewById(R.id.btnComprar).setOnClickListener(v -> {
            // TODO: flujo de separación
        });

        // Botón QR
        findViewById(R.id.btnQR).setOnClickListener(v -> {
            // TODO: mostrar QR del proyecto
        });

        // Chat con asesor
        findViewById(R.id.btnChatAsesor).setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatDetailActivity.class);
            intent.putExtra("contacto", ((TextView) findViewById(R.id.tvNombreAsesor)).getText().toString());
            startActivity(intent);
        });

        // Ver mapa completo
        findViewById(R.id.cardMapa).setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewOnMapActivity.class);
            intent.putExtra(EXTRA_PROYECTO, nombreProyecto);
            startActivity(intent);
        });

        // "ver variadas" en Coste de vida → muestra desglose estimado de costos
        findViewById(R.id.tvVerCostos).setOnClickListener(v -> mostrarDesgloseCostos());

        // Agregar comentario
        findViewById(R.id.btnAgregarComentario).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddCommentActivity.class);
            intent.putExtra(EXTRA_PROYECTO, nombreProyecto);
            startActivity(intent);
        });

        // Botón Separar Inmueble (bottom bar)
        // Lanza notificación de separación pendiente antes de abrir PaymentMethodActivity
        // Al tocar la notificación, abre AgendaCitaActivity
        findViewById(R.id.btnSeparar).setOnClickListener(v -> {
            Intent intentAgenda = new Intent(this, AgendaCitaActivity.class);
            NotificationHelper.lanzarNotificacion(
                    this,
                    "Separación Pendiente",
                    "Tienes 10 minutos para completar el pago",
                    NotificationHelper.NOTIF_SEPARACION,
                    intentAgenda
            );
            startActivity(new Intent(this, PaymentMethodActivity.class));
        });

        View tvVerTodos = findViewById(R.id.tvVerTodos);
        if (tvVerTodos != null) {
            tvVerTodos.setOnClickListener(v -> {
                Intent intent = new Intent(this, ReviewsActivity.class);
                intent.putExtra(EXTRA_PROYECTO, nombreProyecto);
                startActivity(intent);
            });
        }
    }

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

        // Título
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

        // Datos ficticios de costos mensuales
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

        // Divisor
        android.view.View divisor = new android.view.View(this);
        divisor.setBackgroundColor(0xFFE0E0E0);
        android.widget.LinearLayout.LayoutParams divLp =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 2);
        divLp.topMargin = 12;
        divLp.bottomMargin = 20;
        divisor.setLayoutParams(divLp);
        root.addView(divisor);

        // Total
        android.widget.LinearLayout rowTotal = new android.widget.LinearLayout(this);
        rowTotal.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        android.widget.LinearLayout.LayoutParams totalRowLp =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        totalRowLp.bottomMargin = 40;
        rowTotal.setLayoutParams(totalRowLp);

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

        // Botón cerrar
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
        // Fuente 1: extra enviado por HomeActivity
        String nombre = getIntent().getStringExtra(EXTRA_PROYECTO);
        if (nombre != null && !nombre.isEmpty()) {
            Log.d("DetalleProyecto", "Proyecto cargado desde extra: " + nombre);
            return nombre;
        }
        // Fuente 2: deep link (inmobiliaria://proyecto/{id})
        Uri uri = getIntent().getData();
        if (uri != null) {
            Log.d("DeepLink", "URI recibida: " + uri);
            String segmento = uri.getLastPathSegment();
            if (segmento != null && !segmento.isEmpty()) {
                nombre = mapearIdANombre(segmento);
                Log.d("DetalleProyecto", "Proyecto cargado desde deep link, id=" + segmento + " → " + nombre);
                return nombre;
            }
        }
        return null;
    }

    /** Mapea un id numérico del QR al nombre de proyecto conocido por la app. */
    private String mapearIdANombre(String id) {
        switch (id) {
            case "1": return "Catalina Ventor";
            case "2": return "Residencial Park";
            case "3": return "Torre Miramar";
            case "4": return "Condominio Las Lomas";
            case "5": return "Catalina Sky";
            default: return id; // el QR ya contiene directamente el nombre
        }
    }

    private int obtenerImagenProyecto(String nombreProyecto) {
        if (nombreProyecto == null) return ImageUrls.HERO_TORRES_UNIDAS;
        switch (nombreProyecto) {
            case "Catalina Ventor":
                return ImageUrls.PROYECTO_CATALINA_VENTOR;
            case "Residencial Park":
            case "Residencial El Park":
                return ImageUrls.PROYECTO_RESIDENCIAL_PARK;
            case "Torre Miramar":
                return ImageUrls.PROYECTO_TORRE_MIRAMAR;
            case "Condominio Las Lomas":
                return ImageUrls.PROYECTO_CONDOMINIO_LOMAS;
            case "Catalina Sky":
                return ImageUrls.PROYECTO_CATALINA_SKY;
            default:
                return ImageUrls.HERO_TORRES_UNIDAS;
        }
    }
}