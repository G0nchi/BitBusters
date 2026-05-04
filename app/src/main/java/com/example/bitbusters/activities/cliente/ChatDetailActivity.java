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
import com.example.bitbusters.data.ClientDataRepository;
import com.example.bitbusters.models.ClientMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {

    private EditText etMensaje;
    private RecyclerView rvMensajes;
    private ClientChatMessageAdapter messageAdapter;
    private final List<ClientMessage> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        etMensaje     = findViewById(R.id.etMensaje);
        rvMensajes = findViewById(R.id.rvMensajes);

        // Recibir nombre del contacto desde MessagesActivity
        String nombreContacto = getIntent().getStringExtra("contacto");
        if (nombreContacto != null) {
            ((TextView) findViewById(R.id.tvNombre)).setText(nombreContacto);
        } else {
            nombreContacto = "Asesor";
        }

        messages.addAll(ClientDataRepository.getConversation(nombreContacto));
        String initials = getInitials(nombreContacto);
        messageAdapter = new ClientChatMessageAdapter(messages, initials);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMensajes.setLayoutManager(layoutManager);
        rvMensajes.setAdapter(messageAdapter);

        // Botón volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Botón llamar
        findViewById(R.id.btnCall).setOnClickListener(v ->
            Toast.makeText(this, "Llamada disponible en proxima integracion", Toast.LENGTH_SHORT).show());

        // Botón cámara
        findViewById(R.id.btnCamera).setOnClickListener(v ->
            Toast.makeText(this, "Envio de imagen disponible en proxima integracion", Toast.LENGTH_SHORT).show());

        // Botón enviar mensaje
        findViewById(R.id.btnSend).setOnClickListener(v -> enviarMensaje());
    }

    private void enviarMensaje() {
        String texto = etMensaje.getText().toString().trim();
        if (TextUtils.isEmpty(texto)) return;

        messageAdapter.addMessage(new ClientMessage(texto, "Ahora", true));
        etMensaje.setText("");
        rvMensajes.scrollToPosition(messageAdapter.getItemCount() - 1);
    }

    private String getInitials(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 0) return "AS";
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }
}