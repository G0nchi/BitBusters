package com.example.bitbusters.activities.cliente;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.ClientChatMessageAdapter;
import com.example.bitbusters.models.ClientMessage;
import com.example.bitbusters.utils.ClienteChatRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de conversación del cliente con un asesor.
 *
 * Recibe extras:
 *   EXTRA_CONTACTO  → nombre del asesor (para mostrar en header)
 *   EXTRA_CHAT_ID   → "{clienteUid}_{asesorId}" (ej. "uid123_asesor_ana_001")
 *   EXTRA_INITIALS  → iniciales del asesor
 *   EXTRA_COLOR     → color hex del avatar
 *
 * Usa ClienteChatRepository para leer/escribir en Firestore en tiempo real.
 */
public class ChatDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CONTACTO = "contacto";
    public static final String EXTRA_CHAT_ID  = "chat_id";
    public static final String EXTRA_INITIALS = "initials";
    public static final String EXTRA_COLOR    = "color";

    private EditText etMensaje;
    private RecyclerView rvMensajes;
    private ClientChatMessageAdapter messageAdapter;
    private final List<ClientMessage> messages = new ArrayList<>();
    private ClienteChatRepository chatRepo;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        etMensaje  = findViewById(R.id.etMensaje);
        rvMensajes = findViewById(R.id.rvMensajes);

        String nombreContacto = getIntent().getStringExtra(EXTRA_CONTACTO);
        chatId = getIntent().getStringExtra(EXTRA_CHAT_ID);

        if (nombreContacto != null) {
            ((TextView) findViewById(R.id.tvNombre)).setText(nombreContacto);
        } else {
            nombreContacto = "Asesor";
        }

        String initials = getIntent().getStringExtra(EXTRA_INITIALS);
        if (initials == null) initials = getInitials(nombreContacto);

        messageAdapter = new ClientChatMessageAdapter(messages, initials);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMensajes.setLayoutManager(layoutManager);
        rvMensajes.setAdapter(messageAdapter);

        chatRepo = new ClienteChatRepository();
        cargarMensajes();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnCall).setOnClickListener(v ->
            Toast.makeText(this, "Llamada disponible en próxima integración", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnCamera).setOnClickListener(v ->
            Toast.makeText(this, "Envío de imagen disponible en próxima integración", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnSend).setOnClickListener(v -> enviarMensaje());
    }

    private void cargarMensajes() {
        if (chatId == null || chatId.isEmpty()) return;

        chatRepo.loadMessages(chatId, new ClienteChatRepository.MessagesCallback() {
            @Override
            public void onMessages(List<ClientMessage> mensajes) {
                messages.clear();
                messages.addAll(mensajes);
                messageAdapter.notifyDataSetChanged();
                if (!messages.isEmpty()) {
                    rvMensajes.scrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("ChatDetailActivity", "Error cargando mensajes: " + error);
            }
        });
    }

    private void enviarMensaje() {
        String texto = etMensaje.getText().toString().trim();
        if (TextUtils.isEmpty(texto) || chatId == null) return;

        etMensaje.setText("");

        chatRepo.sendMessage(chatId, texto, new ClienteChatRepository.SendCallback() {
            @Override
            public void onSuccess() {
                // El SnapshotListener actualizará la lista automáticamente
            }

            @Override
            public void onError(String error) {
                etMensaje.setText(texto);
                Toast.makeText(ChatDetailActivity.this,
                    "Error al enviar: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getInitials(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 0) return "AS";
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatRepo != null) chatRepo.release();
    }
}
