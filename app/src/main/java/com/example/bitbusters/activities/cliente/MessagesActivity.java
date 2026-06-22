package com.example.bitbusters.activities.cliente;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.ChatsAdapter;
import com.example.bitbusters.models.Chat;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista de chats del cliente.
 *
 * Muestra los 3 asesores de prueba registrados en Firestore:
 *   - asesor_ana_001    → Ana García
 *   - asesor_roberto_002 → Roberto Pérez
 *   - asesor_lucia_003  → Lucía Mendoza
 *
 * Al abrir un chat se pasa el asesorId para construir el chatId en Firestore:
 *   chatId = "{clienteUid}_{asesorId}"
 */
public class MessagesActivity extends AppCompatActivity {

    // IDs de documento en Firestore para los asesores de prueba
    public static final String ASESOR_ANA_ID      = "asesor_ana_001";
    public static final String ASESOR_ROBERTO_ID  = "asesor_roberto_002";
    public static final String ASESOR_LUCIA_ID    = "asesor_lucia_003";

    private RecyclerView recyclerViewChats;
    private ChatsAdapter chatsAdapter;
    private final List<Object> chatItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        recyclerViewChats = findViewById(R.id.recyclerViewChats);

        chatItems.addAll(buildAsesorChats());
        setupRecyclerView();

        TabLayout.Tab tabMensajes = tabLayout.getTabAt(1);
        if (tabMensajes != null) tabMensajes.select();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    startActivity(new Intent(MessagesActivity.this, NotificationsActivity.class));
                    finish();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /**
     * Construye la lista de chats con los 3 asesores de prueba.
     * El campo "id" del Chat se usa como asesorId para abrir Firestore.
     */
    private List<Chat> buildAsesorChats() {
        List<Chat> list = new ArrayList<>();
        list.add(new Chat(ASESOR_ANA_ID,     "Ana García",     "Hola, ¿en qué puedo ayudarte?",   "Ahora", "AG", "#26A69A", 0, true, "Asesora Inmobiliaria"));
        list.add(new Chat(ASESOR_ROBERTO_ID, "Roberto Pérez",  "Buenos días, ¿tienes alguna duda?", "Hoy",  "RP", "#5C6BC0", 0, true, "Asesor Inmobiliario"));
        list.add(new Chat(ASESOR_LUCIA_ID,   "Lucía Mendoza",  "¡Bienvenido! Escríbeme cuando quieras.", "Hoy", "LM", "#EC407A", 0, true, "Asesora Inmobiliaria"));
        return list;
    }

    private void setupRecyclerView() {
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(this));
        chatsAdapter = new ChatsAdapter(chatItems, chat -> abrirChat(chat));
        recyclerViewChats.setAdapter(chatsAdapter);
    }

    private void abrirChat(Chat chat) {
        String clienteUid = obtenerClienteUid();
        String chatId = clienteUid + "_" + chat.getId();

        Intent intent = new Intent(this, ChatDetailActivity.class);
        intent.putExtra(ChatDetailActivity.EXTRA_CONTACTO, chat.getName());
        intent.putExtra(ChatDetailActivity.EXTRA_CHAT_ID,  chatId);
        intent.putExtra(ChatDetailActivity.EXTRA_INITIALS, chat.getInitials());
        intent.putExtra(ChatDetailActivity.EXTRA_COLOR,    chat.getColorHex());
        startActivity(intent);
    }

    /** Obtiene el UID del cliente autenticado, o un ID de prueba si usó login mock. */
    private String obtenerClienteUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) return user.getUid();
        return "cliente_demo";
    }
}
