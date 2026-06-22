package com.example.bitbusters.activities.cliente;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.MensajesAdapter;
import com.example.bitbusters.models.Mensaje;
import com.example.bitbusters.repository.ChatRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {

    private static final int MAX_CHARS = 500;

    private EditText etMensaje;
    private RecyclerView rvMensajes;
    private View btnSend;
    private MensajesAdapter mensajesAdapter;

    private ChatRepository chatRepository;
    private ListenerRegistration mensajesListener;

    private String chatId;
    private String uidActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        chatId = getIntent().getStringExtra("chatId");
        String nombreAsesor = getIntent().getStringExtra("nombreAsesor");
        if (nombreAsesor == null) nombreAsesor = "Asesor";

        uidActual = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";

        chatRepository = new ChatRepository();

        etMensaje  = findViewById(R.id.etMensaje);
        rvMensajes = findViewById(R.id.rvMensajes);
        btnSend    = findViewById(R.id.btnSend);

        etMensaje.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_CHARS)});

        TextView tvNombre = findViewById(R.id.tvNombre);
        tvNombre.setText(nombreAsesor);

        String iniciales = obtenerIniciales(nombreAsesor);
        mensajesAdapter = new MensajesAdapter(uidActual, iniciales);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMensajes.setLayoutManager(layoutManager);
        rvMensajes.setAdapter(mensajesAdapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnCall).setOnClickListener(v ->
            Toast.makeText(this, "Llamada disponible en próxima integración", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnCamera).setOnClickListener(v ->
            Toast.makeText(this, "Envío de imagen disponible en próxima integración", Toast.LENGTH_SHORT).show());

        btnSend.setOnClickListener(v -> enviarMensaje());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (chatId != null && !chatId.isEmpty()) {
            iniciarListenerMensajes();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mensajesListener != null) {
            mensajesListener.remove();
            mensajesListener = null;
        }
    }

    private void iniciarListenerMensajes() {
        mensajesListener = chatRepository.escucharMensajes(chatId,
            new ChatRepository.MensajesListener() {
                @Override
                public void onMensajes(List<Mensaje> mensajes) {
                    mensajesAdapter.setMensajes(mensajes);
                    if (!mensajes.isEmpty()) {
                        rvMensajes.scrollToPosition(mensajes.size() - 1);
                    }
                }

                @Override
                public void onError(String mensaje) {
                    Toast.makeText(ChatDetailActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void enviarMensaje() {
        String texto = etMensaje.getText().toString().trim();
        if (TextUtils.isEmpty(texto)) return;
        if (chatId == null || chatId.isEmpty()) {
            Toast.makeText(this, "Error: chat no inicializado", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSend.setEnabled(false);
        etMensaje.setText("");

        chatRepository.enviarMensaje(chatId, uidActual, texto,
            () -> btnSend.setEnabled(true),
            error -> {
                etMensaje.setText(texto);
                btnSend.setEnabled(true);
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            });
    }

    private static String obtenerIniciales(String nombre) {
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length == 0) return "AS";
        if (partes.length == 1) return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        return (partes[0].substring(0, 1) + partes[1].substring(0, 1)).toUpperCase();
    }
}
