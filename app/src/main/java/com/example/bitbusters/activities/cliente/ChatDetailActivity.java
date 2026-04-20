package com.example.bitbusters.activities.cliente;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;
public class ChatDetailActivity extends AppCompatActivity {

    private EditText etMensaje;
    private LinearLayout layoutMensajes;
    private ScrollView scrollMensajes;
    private String nombreContacto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        etMensaje     = findViewById(R.id.etMensaje);
        layoutMensajes = findViewById(R.id.layoutMensajes);
        scrollMensajes = findViewById(R.id.scrollMensajes);

        // Recibir nombre del contacto desde MessagesActivity
        nombreContacto = getIntent().getStringExtra("contacto");
        if (nombreContacto != null) {
            ((TextView) findViewById(R.id.tvNombre)).setText(nombreContacto);
        }

        // Botón volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Botón llamar
        findViewById(R.id.btnCall).setOnClickListener(v -> {
            // TODO: iniciar llamada
        });

        // Botón cámara
        findViewById(R.id.btnCamera).setOnClickListener(v -> {
            // TODO: abrir cámara o galería para enviar imagen
        });

        // Botón enviar mensaje
        findViewById(R.id.btnSend).setOnClickListener(v -> enviarMensaje());
    }

    private void enviarMensaje() {
        String texto = etMensaje.getText().toString().trim();
        if (TextUtils.isEmpty(texto)) return;

        // Crear burbuja de mensaje enviado dinámicamente
        TextView burbuja = new TextView(this);
        burbuja.setText(texto);
        burbuja.setTextSize(13f);
        burbuja.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
        burbuja.setBackground(getResources().getDrawable(R.drawable.bg_bubble_sent, getTheme()));
        burbuja.setPadding(dpToPx(14), dpToPx(10), dpToPx(14), dpToPx(10));
        burbuja.setMaxWidth(dpToPx(260));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = android.view.Gravity.END;
        params.setMargins(0, dpToPx(8), 0, dpToPx(4));
        burbuja.setLayoutParams(params);

        layoutMensajes.addView(burbuja);
        etMensaje.setText("");

        // Scroll automático al último mensaje
        scrollMensajes.post(() -> scrollMensajes.fullScroll(View.FOCUS_DOWN));

        // TODO: en Lab 6 enviar mensaje a Firebase Realtime Database
        // DatabaseReference ref = FirebaseDatabase.getInstance().getReference("chats").child(chatId);
        // ref.push().setValue(new Mensaje(texto, userId, System.currentTimeMillis()));
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}