package com.example.bitbusters.activities.asesor;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bitbusters.adapters.MensajesAdapter;
import com.example.bitbusters.databinding.ActivityConversacionBinding;
import com.example.bitbusters.models.Mensaje;
import com.example.bitbusters.repository.ChatRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * Pantalla de conversación del Asesor con un Cliente.
 * Usa ChatRepository (repository package) y MensajesAdapter — misma arquitectura que el Cliente.
 */
public class ConversacionActivity extends AppCompatActivity {

    public static final String EXTRA_CHAT_ID       = "extra_chat_id";
    public static final String EXTRA_NOMBRE        = "extra_nombre";
    public static final String EXTRA_INITIALS      = "extra_initials";
    public static final String EXTRA_COLOR         = "extra_color";
    public static final String EXTRA_PROYECTO      = "extra_proyecto";

    private ActivityConversacionBinding binding;
    private MensajesAdapter             mensajesAdapter;
    private ChatRepository              chatRepository;
    private ListenerRegistration        mensajesListener;

    private String chatId;
    private String uidActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConversacionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        chatId    = getIntent().getStringExtra(EXTRA_CHAT_ID);
        uidActual = FirebaseAuth.getInstance().getCurrentUser() != null
                  ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        chatRepository = new ChatRepository();

        bindHeader();
        setupRecyclerView();
        setupSendButton();
        binding.btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (chatId != null && !chatId.isEmpty()) iniciarListenerMensajes();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mensajesListener != null) { mensajesListener.remove(); mensajesListener = null; }
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private void bindHeader() {
        String nombre   = getIntent().getStringExtra(EXTRA_NOMBRE);
        String initials = getIntent().getStringExtra(EXTRA_INITIALS);
        String color    = getIntent().getStringExtra(EXTRA_COLOR);
        String proyecto = getIntent().getStringExtra(EXTRA_PROYECTO);

        if (nombre   != null) binding.tvNombreChat.setText(nombre);
        if (initials != null) binding.tvInitialsChat.setText(initials);

        if (color != null) {
            try { binding.cvAvatarChat.setCardBackgroundColor(Color.parseColor(color)); }
            catch (IllegalArgumentException ignored) {}
        }

        if (proyecto != null && !proyecto.isEmpty()) {
            binding.tvProyectoChat.setVisibility(View.VISIBLE);
            binding.tvProyectoChat.setText(proyecto);
        } else {
            binding.tvProyectoChat.setVisibility(View.GONE);
        }
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        String initials = getIntent().getStringExtra(EXTRA_INITIALS);
        if (initials == null) initials = "CL";

        mensajesAdapter = new MensajesAdapter(uidActual, initials);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        binding.rvMensajes.setLayoutManager(lm);
        binding.rvMensajes.setAdapter(mensajesAdapter);
    }

    // ── Listener mensajes en tiempo real ─────────────────────────────────────

    private void iniciarListenerMensajes() {
        mensajesListener = chatRepository.escucharMensajes(chatId,
            new ChatRepository.MensajesListener() {
                @Override
                public void onMensajes(List<Mensaje> mensajes) {
                    mensajesAdapter.setMensajes(mensajes);
                    if (!mensajes.isEmpty()) {
                        binding.rvMensajes.scrollToPosition(mensajes.size() - 1);
                    }
                }

                @Override
                public void onError(String error) {
                    android.util.Log.e("ConversacionActivity", "Error: " + error);
                }
            });
    }

    // ── Botón enviar ──────────────────────────────────────────────────────────

    private void setupSendButton() {
        binding.btnSend.setOnClickListener(v -> {
            String text = binding.etMensaje.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;

            binding.etMensaje.setText("");
            binding.btnSend.setEnabled(false);

            chatRepository.enviarMensaje(chatId, uidActual, text,
                () -> binding.btnSend.setEnabled(true),
                error -> {
                    binding.etMensaje.setText(text);
                    binding.btnSend.setEnabled(true);
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
