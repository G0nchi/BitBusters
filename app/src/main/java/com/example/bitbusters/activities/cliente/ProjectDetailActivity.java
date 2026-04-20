package com.example.bitbusters.activities.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;
public class ProjectDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        // Recibir datos del proyecto desde HomeActivity
        String nombreProyecto = getIntent().getStringExtra("proyecto");
        if (nombreProyecto != null) {
            ((TextView) findViewById(R.id.tvNombreProyecto)).setText(nombreProyecto);
        }

        // Botón volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Botón compartir hero
        findViewById(R.id.btnCompartir).setOnClickListener(v -> {
            // TODO: compartir proyecto
        });

        // Botón favorito
        findViewById(R.id.btnFavorito).setOnClickListener(v -> {
            // TODO: guardar en favoritos (Firebase Lab 6)
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
            // TODO: abrir MapActivity con ubicación del proyecto
        });

        // Ver costos variadas
        findViewById(R.id.tvVerCostos).setOnClickListener(v -> {
            // TODO: expandir costos
        });

        // Agregar comentario
        findViewById(R.id.btnAgregarComentario).setOnClickListener(v -> {
            startActivity(new Intent(this, AddCommentActivity.class));
        });

        // Botón Separar Inmueble (bottom bar)
        findViewById(R.id.btnSeparar).setOnClickListener(v -> {
            // TODO: validar tarjeta registrada → flujo de separación
            // Recibe notificación de aprobación con timer de 10 minutos
        });
    }
}