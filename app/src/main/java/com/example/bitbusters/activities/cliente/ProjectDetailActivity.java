package com.example.bitbusters.activities.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;
import com.example.bitbusters.utils.ImageUrls;
import com.bumptech.glide.Glide;

public class ProjectDetailActivity extends AppCompatActivity {

    private static final String EXTRA_PROYECTO = "proyecto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        // Recibir datos del proyecto desde HomeActivity
        String nombreProyecto = getIntent().getStringExtra(EXTRA_PROYECTO);
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

        // Ver costos variadas
        findViewById(R.id.tvVerCostos).setOnClickListener(v -> {
            // TODO: expandir costos
        });

        // Agregar comentario
        findViewById(R.id.btnAgregarComentario).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddCommentActivity.class);
            intent.putExtra(EXTRA_PROYECTO, nombreProyecto);
            startActivity(intent);
        });

        // Botón Separar Inmueble (bottom bar)
        findViewById(R.id.btnSeparar).setOnClickListener(v -> {
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