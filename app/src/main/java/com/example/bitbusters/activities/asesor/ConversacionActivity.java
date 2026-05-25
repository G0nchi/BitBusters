package com.example.bitbusters.activities.asesor;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bitbusters.databinding.ActivityConversacionBinding;
import com.example.bitbusters.utils.ChatRepository;
import com.example.bitbusters.utils.MockChatRepository;

import java.util.List;

/**
 * Pantalla de conversación entre asesor y cliente.
 *
 * Usa el patrón Repository (Clase 08.2) para desacoplar la UI de la
 * fuente de datos:
 *  - Desarrollo: {@link MockChatRepository} (datos en memoria, sin red).
 *  - Producción:  {@link com.example.bitbusters.utils.FirestoreChatRepository}
 *                (Firebase Firestore con SnapshotListener en tiempo real).
 *
 * El send button ahora funciona: agrega el mensaje al adapter con animación
 * y notifica al repositorio para persistirlo.
 */
public class ConversacionActivity extends AppCompatActivity {

    public static final String EXTRA_CHAT_ID  = "extra_chat_id";
    public static final String EXTRA_NOMBRE   = "extra_nombre";
    public static final String EXTRA_INITIALS = "extra_initials";
    public static final String EXTRA_COLOR    = "extra_color";
    public static final String EXTRA_PROYECTO = "extra_proyecto";

    private ActivityConversacionBinding binding;
    private MensajeAdapter              mensajeAdapter;
    private ChatRepository              chatRepo;
    private String                      chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConversacionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Singleton: reutiliza el mismo cache aunque se abra/cierre la Activity.
        // Para producción: reemplazar por new FirestoreChatRepository()
        chatRepo = MockChatRepository.getInstance();
        chatId   = getIntent().getStringExtra(EXTRA_CHAT_ID);

        bindHeader();
        loadMessages();
        setupSendButton();
        binding.btnBack.setOnClickListener(v -> finish());
    }

    // ── Header dinámico ───────────────────────────────────────────────────────

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
            View iconRow = (View) binding.tvProyectoChat.getParent();
            if (iconRow != null) iconRow.setVisibility(View.GONE);
        }
    }

    // ── Carga de mensajes vía Repository ─────────────────────────────────────

    private void loadMessages() {
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        binding.rvMensajes.setLayoutManager(lm);

        chatRepo.loadMessages(chatId, new ChatRepository.MessagesCallback() {
            @Override
            public void onMessages(List<MensajeAdapter.Mensaje> mensajes) {
                if (mensajeAdapter == null) {
                    // Primera carga: crear adapter
                    mensajeAdapter = new MensajeAdapter(mensajes);
                    binding.rvMensajes.setAdapter(mensajeAdapter);
                } else {
                    // Actualización en tiempo real (Firestore SnapshotListener)
                    mensajeAdapter.updateMensajes(mensajes);
                }
                // Scroll al último mensaje
                binding.rvMensajes.scrollToPosition(mensajeAdapter.getItemCount() - 1);
            }

            @Override
            public void onError(String error) {
                // En producción: mostrar Snackbar de error
                android.util.Log.e("ConversacionActivity", "Error: " + error);
            }
        });
    }

    // ── Botón enviar ──────────────────────────────────────────────────────────

    private void setupSendButton() {
        binding.btnSend.setOnClickListener(v -> {
            String text = binding.etMensaje.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;

            // Limpiar campo inmediatamente para buena UX
            binding.etMensaje.setText("");

            chatRepo.sendMessage(chatId, text, new ChatRepository.SendCallback() {
                @Override
                public void onSuccess() {
                    // Agregar mensaje al adapter con animación
                    if (mensajeAdapter != null) {
                        String hora = new java.text.SimpleDateFormat(
                            "h:mm a", java.util.Locale.getDefault()
                        ).format(new java.util.Date());
                        mensajeAdapter.addMensaje(
                            new MensajeAdapter.Mensaje(text, hora, true));
                        binding.rvMensajes.scrollToPosition(
                            mensajeAdapter.getItemCount() - 1);
                    }
                }

                @Override
                public void onError(String error) {
                    // Restaurar texto si falla (importante en producción con Firebase)
                    binding.etMensaje.setText(text);
                    android.util.Log.e("ConversacionActivity", "Send error: " + error);
                }
            });
        });
    }

    // ── Ciclo de vida ─────────────────────────────────────────────────────────

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatRepo != null) chatRepo.release();  // libera SnapshotListeners de Firestore
        binding = null;
    }
}
